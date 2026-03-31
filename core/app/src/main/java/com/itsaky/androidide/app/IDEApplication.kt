/*
 * This file is part of AndroidIDE.
 *
 * AndroidIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AndroidIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.itsaky.androidide.app

import android.content.Intent
import android.net.Uri
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.blankj.utilcode.util.ThrowableUtils.getFullStackTrace
import com.google.android.material.color.DynamicColors
import com.itsaky.androidide.BuildConfig
import com.itsaky.androidide.activities.CrashHandlerActivity
import com.itsaky.androidide.activities.editor.IDELogcatReader
import com.itsaky.androidide.buildinfo.BuildInfo
import com.itsaky.androidide.editor.schemes.IDEColorSchemeProvider
import com.itsaky.androidide.eventbus.events.preferences.PreferenceChangeEvent
import com.itsaky.androidide.events.AppEventsIndex
import com.itsaky.androidide.events.EditorEventsIndex
import com.itsaky.androidide.events.LspApiEventsIndex
import com.itsaky.androidide.events.LspJavaEventsIndex
import com.itsaky.androidide.managers.ToolsManager
import com.itsaky.androidide.preferences.internal.DevOpsPreferences
import com.itsaky.androidide.preferences.internal.GeneralPreferences
import com.itsaky.androidide.resources.localization.LocaleProvider
import com.itsaky.androidide.syntax.colorschemes.SchemeAndroidIDE
import com.itsaky.androidide.treesitter.TreeSitter
import com.itsaky.androidide.ui.themes.IDETheme
import com.itsaky.androidide.ui.themes.IThemeManager
import com.itsaky.androidide.utils.Environment
import com.itsaky.androidide.utils.RecyclableObjectPool
import com.itsaky.androidide.utils.VMUtils
import com.itsaky.androidide.utils.flashError
import com.termux.app.TermuxApplication
import com.termux.shared.reflection.ReflectionUtils
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import java.lang.Thread.UncaughtExceptionHandler
import kotlin.system.exitProcess
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.slf4j.LoggerFactory

/**
 * Main Application class for AndroidIDE. Initializes global environment, tools, and event bus.
 *
 * @author android_zero
 */
class IDEApplication : TermuxApplication() {

  private var uncaughtExceptionHandler: UncaughtExceptionHandler? = null
  private var ideLogcatReader: IDELogcatReader? = null
  
  private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineName("IDEAppScope"))

  init {
    // if (!VMUtils.isJvm()) {
      // TreeSitter.loadLibrary()
    // }

    RecyclableObjectPool.DEBUG = BuildConfig.DEBUG
  }

  @OptIn(DelicateCoroutinesApi::class)
  override fun onCreate() {
    // Environment.init(this)
    instance = this
    super.onCreate()
    
    applyPersistedLocale()

    uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, th -> handleCrash(thread, th) }

    applicationScope.launch {
      // 初始化环境目录
      Environment.init(this@IDEApplication)
      
      // 加载 TreeSitter JNI
      if (!VMUtils.isJvm()) {
        TreeSitter.loadLibrary()
        ToolsManager.init(this@IDEApplication, null)
      }

      // 反射与色彩方案加载
      ReflectionUtils.bypassHiddenAPIReflectionRestrictions()
      IDEColorSchemeProvider.init()
    }

    initEventBus()
    AppCompatDelegate.setDefaultNightMode(GeneralPreferences.uiMode)
    if (IThemeManager.getInstance().getCurrentTheme() == IDETheme.MATERIAL_YOU) {
      DynamicColors.applyToActivitiesIfAvailable(this)
    }
    EditorColorScheme.setDefault(SchemeAndroidIDE.newInstance(null))

    // Debugging 配置
    if (BuildConfig.DEBUG) {
      StrictMode.setVmPolicy(
          StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy()).penaltyLog().detectAll().build()
      )
      if (DevOpsPreferences.dumpLogs) {
        startLogcatReader()
      }
    }
  }

  /**
   * 在启动时恢复语言设置
   */
  private fun applyPersistedLocale() {
    val selectedLocaleKey = GeneralPreferences.selectedLocale
    if (selectedLocaleKey != null) {
      val locale = LocaleProvider.getLocale(selectedLocaleKey)
      if (locale != null) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale))
      }
    }
  }

  private fun initEventBus() {
    EventBus.builder()
        .addIndex(AppEventsIndex())
        .addIndex(EditorEventsIndex())
        .addIndex(LspApiEventsIndex())
        .addIndex(LspJavaEventsIndex())
        .installDefaultEventBus(true)

    EventBus.getDefault().register(this)

    AppCompatDelegate.setDefaultNightMode(GeneralPreferences.uiMode)

    if (IThemeManager.getInstance().getCurrentTheme() == IDETheme.MATERIAL_YOU) {
      DynamicColors.applyToActivitiesIfAvailable(this)
    }

    EditorColorScheme.setDefault(SchemeAndroidIDE.newInstance(null))

    ReflectionUtils.bypassHiddenAPIReflectionRestrictions()
    GlobalScope.launch { IDEColorSchemeProvider.init() }
  }

  fun showChangelog() {
    val intent = Intent(Intent.ACTION_VIEW)
    var version = BuildInfo.VERSION_NAME_SIMPLE
    if (!version.startsWith('v')) {
      version = "v${version}"
    }
    intent.data = Uri.parse("${BuildInfo.REPO_URL}/releases/tag/${version}")
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
      startActivity(intent)
    } catch (th: Throwable) {
      log.error("Unable to start activity to show changelog", th)
      flashError("Unable to start activity")
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  fun onPrefChanged(event: PreferenceChangeEvent) {
    val enabled = event.value as? Boolean?

    if (event.key == DevOpsPreferences.KEY_DEVOPTS_DEBUGGING_DUMPLOGS) {
      if (enabled == true) {
        startLogcatReader()
      } else {
        stopLogcatReader()
      }
    } else if (event.key == GeneralPreferences.UI_MODE) {
      val mode = GeneralPreferences.uiMode
      if (mode != AppCompatDelegate.getDefaultNightMode()) {
        AppCompatDelegate.setDefaultNightMode(mode)
      }
    } else if (event.key == GeneralPreferences.SELECTED_LOCALE) {

      val selectedLocale = GeneralPreferences.selectedLocale
      val localeListCompat =
          selectedLocale?.let { LocaleListCompat.create(LocaleProvider.getLocale(selectedLocale)) }
              ?: LocaleListCompat.getEmptyLocaleList()

      AppCompatDelegate.setApplicationLocales(localeListCompat)
    }
  }

  private fun handleCrash(thread: Thread, th: Throwable) {
    writeException(th)

    try {

      val intent = Intent()
      intent.action = CrashHandlerActivity.REPORT_ACTION
      intent.putExtra(CrashHandlerActivity.TRACE_KEY, getFullStackTrace(th))
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      startActivity(intent)
      if (uncaughtExceptionHandler != null) {
        uncaughtExceptionHandler!!.uncaughtException(thread, th)
      }

      exitProcess(1)
    } catch (error: Throwable) {
      log.error("Unable to show crash handler activity", error)
    }
  }

  private fun startLogcatReader() {
    if (ideLogcatReader != null) {
      // already started
      return
    }

    log.info("Starting logcat reader...")
    ideLogcatReader = IDELogcatReader().also { it.start() }
  }

  private fun stopLogcatReader() {
    log.info("Stopping logcat reader...")
    ideLogcatReader?.stop()
    ideLogcatReader = null
  }

  companion object {
    private val log = LoggerFactory.getLogger(IDEApplication::class.java)
    @JvmStatic
    lateinit var instance: IDEApplication
      private set
  }
}