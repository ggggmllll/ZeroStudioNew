/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  AndroidIDE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.itsaky.androidide.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroPageTransformerType
import com.itsaky.androidide.R
import com.itsaky.androidide.R.string
import com.itsaky.androidide.app.configuration.IDEBuildConfigProvider
import com.itsaky.androidide.fragments.onboarding.GreetingFragment
import com.itsaky.androidide.fragments.onboarding.OdSdkToolInstallFragment
import com.itsaky.androidide.fragments.onboarding.OnboardingInfoFragment
import com.itsaky.androidide.fragments.onboarding.PermissionsFragment
import com.itsaky.androidide.preferences.internal.prefManager
import com.itsaky.androidide.repository.sdkmanager.SdkChecker
import com.itsaky.androidide.ui.themes.IThemeManager
import com.itsaky.androidide.utils.*
import com.termux.shared.android.PackageUtils
import com.termux.shared.markdown.MarkdownUtils
import com.termux.shared.termux.TermuxConstants

/**
 * 引导页与环境初始化 Activity
 *
 * @author Akash Yadav
 * @author android_zero
 */
class OnboardingActivity : AppIntro2() {

  companion object {
    private const val TAG = "OnboardingActivity"
    private const val KEY_ARCHCONFIG_WARNING_IS_SHOWN = "ide.archConfig.experimentalWarning.isShown"
  }

  @SuppressLint("SourceLockedOrientationActivity")
  override fun onCreate(savedInstanceState: Bundle?) {
    // 在 onCreate 的最前端应用主题，避免因为配置改变导致闪烁
    IThemeManager.getInstance().applyTheme(this)

    // 强制竖屏，避免引导页旋转导致重建问题
    try {
      requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    } catch (e: Exception) {
      // 某些设备或特定情况下锁定方向可能会抛出异常（如 Translucent Activity），忽略即可
    }

    super.onCreate(savedInstanceState)

    // 沉浸式状态栏设置
    WindowCompat.getInsetsController(this.window, this.window.decorView).apply {
      isAppearanceLightStatusBars = !isSystemInDarkMode()
      isAppearanceLightNavigationBars = !isSystemInDarkMode()
    }

    if (isAtLeastV()) {
      ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
        view.setBackgroundColor(resolveAttr(R.attr.colorSurface))
        insets
      }
    } else {
      @Suppress("DEPRECATION")
      window.statusBarColor = resolveAttr(R.attr.colorSurface)
    }

    // 核心检查：如果环境已经准备好（权限和SDK/JDK均满足），直接跳转并拦截后续视图加载
    if (tryNavigateToMainIfSetupIsCompleted()) {
      return
    }

    // 是否允许用户左右滑动切换引导页
    isWizardMode = false
    setSwipeLock(true) // 是否允许左右滑动切换页面，这个true表示禁用，这个运行滑动会影响sdk安装页面，所以需要禁用

    setTransformer(AppIntroPageTransformerType.Fade)
    setProgressIndicator()
    showStatusBar(true)
    isIndicatorEnabled = true

    // 添加欢迎页
    addSlide(GreetingFragment())

    // 检查用户是否为主用户
    if (!PackageUtils.isCurrentUserThePrimaryUser(this)) {
      val errorMessage =
          getString(
              string.bootstrap_error_not_primary_user_message,
              MarkdownUtils.getMarkdownCodeForString(TermuxConstants.TERMUX_PREFIX_DIR_PATH, false),
          )
      addSlide(
          OnboardingInfoFragment.newInstance(
              getString(string.title_unsupported_user),
              errorMessage,
              R.drawable.ic_alert,
              ContextCompat.getColor(this, R.color.color_error),
          )
      )
      return
    }

    // 检查是否安装在外部 SD 卡
    if (isInstalledOnSdCard()) {
      val errorMessage =
          getString(
              string.bootstrap_error_installed_on_portable_sd,
              MarkdownUtils.getMarkdownCodeForString(TermuxConstants.TERMUX_PREFIX_DIR_PATH, false),
          )
      addSlide(
          OnboardingInfoFragment.newInstance(
              getString(string.title_install_location_error),
              errorMessage,
              R.drawable.ic_alert,
              ContextCompat.getColor(this, R.color.color_error),
          )
      )
      return
    }

    // 检查设备架构支持情况
    if (!checkDeviceSupported()) {
      return
    }

    // 如果权限未全部满足，则显示权限请求页
    if (!checkAllPermissionsGranted()) {
      addSlide(PermissionsFragment.newInstance(this))
    }

    // 如果 SDK 或 JDK 工具链未安装，添加环境配置 Fragment
    if (!checkToolsIsInstalled()) {
      addSlide(OdSdkToolInstallFragment.newInstance(this))
    }
  }

  override fun onResume() {
    super.onResume()
    // 每次恢复到前台时，检查是否可以前往 MainActivity
    tryNavigateToMainIfSetupIsCompleted()
  }

  override fun onDonePressed(currentFragment: Fragment?) {
    if (!IDEBuildConfigProvider.getInstance().supportsCpuAbi()) {
      finishAffinity()
      return
    }

    if (!checkToolsIsInstalled() && currentFragment is OdSdkToolInstallFragment) {
      flashError(getString(string.msg_install_tools))
      return
    }

    tryNavigateToMainIfSetupIsCompleted()
  }

  /** 提供给 OdSdkToolInstallFragment 或其它安装组件安装完成后的回调 */
  fun onSetupCompleted() {
    tryNavigateToMainIfSetupIsCompleted()
  }

  /** 严格检查所有必须的权限 */
  private fun checkAllPermissionsGranted(): Boolean {
    val context = this

    // 通知权限
    if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
      return false
    }

    // 安装未知应用权限 (Android 8.0+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      if (!context.packageManager.canRequestPackageInstalls()) {
        return false
      }
    }

    // 全文件管理 / 基础存储权限
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      if (!android.os.Environment.isExternalStorageManager()) {
        return false
      }
    } else {
      val legacyPerms =
          listOf(
              Manifest.permission.READ_EXTERNAL_STORAGE,
              Manifest.permission.WRITE_EXTERNAL_STORAGE,
          )
      if (
          legacyPerms.any {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
          }
      ) {
        return false
      }
    }

    return true
  }

  /** 统一调用下沉的 SdkChecker 进行环境工具检查 */
  private fun checkToolsIsInstalled(): Boolean {
    return SdkChecker.isEnvironmentReadySync()
  }

  /** 综合判定 Setup 是否完成 */
  private fun isSetupCompleted(): Boolean {
    return checkToolsIsInstalled() && checkAllPermissionsGranted()
  }

  /** 如果满足所有条件，跳转到主界面并销毁自己 */
  private fun tryNavigateToMainIfSetupIsCompleted(): Boolean {
    if (isSetupCompleted()) {
      startActivity(Intent(this, MainActivity::class.java))
      finish()
      return true
    }
    return false
  }

  private fun isInstalledOnSdCard(): Boolean {
    return PackageUtils.isAppInstalledOnExternalStorage(this) &&
        TermuxConstants.TERMUX_FILES_DIR_PATH !=
            filesDir.absolutePath.replace("^/data/user/0/".toRegex(), "/data/data/")
  }

  private fun checkDeviceSupported(): Boolean {
    val configProvider = IDEBuildConfigProvider.getInstance()

    if (!configProvider.supportsCpuAbi()) {
      addSlide(
          OnboardingInfoFragment.newInstance(
              getString(string.title_unsupported_device),
              getString(
                  string.msg_unsupported_device,
                  configProvider.cpuArch.abi,
                  configProvider.deviceArch.abi,
              ),
              R.drawable.ic_alert,
              ContextCompat.getColor(this, R.color.color_error),
          )
      )
      return false
    }

    // 实验性架构检查与警告
    if (configProvider.cpuArch != configProvider.deviceArch) {
      if (!archConfigExperimentalWarningIsShown()) {
        addSlide(
            OnboardingInfoFragment.newInstance(
                getString(string.title_experiment_flavor),
                getString(
                    string.msg_experimental_flavor,
                    configProvider.cpuArch.abi,
                    configProvider.deviceArch.abi,
                ),
                R.drawable.ic_alert,
                ContextCompat.getColor(this, R.color.color_warning),
            )
        )
        prefManager.putBoolean(KEY_ARCHCONFIG_WARNING_IS_SHOWN, true)
      }
    }
    return true
  }

  private fun archConfigExperimentalWarningIsShown(): Boolean {
    return prefManager.getBoolean(KEY_ARCHCONFIG_WARNING_IS_SHOWN, false)
  }
}
