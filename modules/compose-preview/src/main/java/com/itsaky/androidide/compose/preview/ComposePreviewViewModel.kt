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

package com.itsaky.androidide.compose.preview

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsaky.androidide.compose.preview.compiler.CompileDiagnostic
import com.itsaky.androidide.compose.preview.data.repository.CompilationException
import com.itsaky.androidide.compose.preview.data.repository.ComposePreviewRepository
import com.itsaky.androidide.compose.preview.data.repository.ComposePreviewRepositoryImpl
import com.itsaky.androidide.compose.preview.data.repository.InitializationResult
import com.itsaky.androidide.compose.preview.domain.PreviewSourceParser
import com.itsaky.androidide.compose.preview.domain.model.ParsedPreviewSource
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory

sealed class PreviewState {
  data object Idle : PreviewState()

  data object Initializing : PreviewState()

  data object Compiling : PreviewState()

  data object Empty : PreviewState()

  data object Building : PreviewState()

  data class Ready(
      val dexFile: File,
      val className: String,
      val previewConfigs: List<PreviewConfig>,
      val runtimeDex: File?,
      val projectDexFiles: List<File> = emptyList(),
  ) : PreviewState()

  data class Error(val message: String, val diagnostics: List<CompileDiagnostic> = emptyList()) :
      PreviewState()

  data class NeedsBuild(val modulePath: String, val variantName: String = "debug") : PreviewState()
}

enum class DisplayMode {
  ALL,
  SINGLE,
}

data class PreviewConfig(
    val functionName: String,
    val heightDp: Int? = null,
    val widthDp: Int? = null,
)

/**
 * 预览容器的状态与热重载调度器。
 *
 * @author android_zero
 */
@OptIn(FlowPreview::class)
class ComposePreviewViewModel(
    private val repository: ComposePreviewRepository = ComposePreviewRepositoryImpl(),
    private val sourceParser: PreviewSourceParser = PreviewSourceParser(),
) : ViewModel() {

  private val _previewState = MutableStateFlow<PreviewState>(PreviewState.Idle)
  val previewState: StateFlow<PreviewState> = _previewState.asStateFlow()

  private val _displayMode = MutableStateFlow(DisplayMode.ALL)
  val displayMode: StateFlow<DisplayMode> = _displayMode.asStateFlow()

  private val _selectedPreview = MutableStateFlow<String?>(null)
  val selectedPreview: StateFlow<String?> = _selectedPreview.asStateFlow()

  private val _availablePreviews = MutableStateFlow<List<String>>(emptyList())
  val availablePreviews: StateFlow<List<String>> = _availablePreviews.asStateFlow()

  private val sourceChanges = MutableSharedFlow<SourceUpdate>()

  private var currentSource: String = ""
  private var cachedFilePath: String = ""
  private var modulePath: String? = null
  private var variantName: String = "debug"
  private val isInitialized = AtomicBoolean(false)
  private var initializationDeferred = kotlinx.coroutines.CompletableDeferred<Unit>()
  private val initMutex = Mutex()

  private data class SourceUpdate(val source: String, val parsedSource: ParsedPreviewSource)

  init {
    viewModelScope.launch {
      // 热重载防抖：用户停止修改/保存 500ms 后才触发编译
      sourceChanges
          .debounce(DEBOUNCE_MS)
          .distinctUntilChanged { old, new -> old.source == new.source }
          .collect { update -> compilePreview(update.source, update.parsedSource) }
    }
  }

  fun initialize(context: Context, filePath: String) {
    if (!isInitialized.compareAndSet(false, true)) return

    cachedFilePath = filePath

    viewModelScope.launch {
      _previewState.value = PreviewState.Initializing

      repository
          .initialize(context, filePath)
          .onSuccess { result ->
            when (result) {
              is InitializationResult.Ready -> {
                modulePath = result.projectContext.modulePath
                variantName = result.projectContext.variantName
                initializationDeferred.complete(Unit)
                _previewState.value = PreviewState.Idle
                LOG.info(
                    "Hot-Reload Environment Ready, modulePath={}, variant={}",
                    modulePath,
                    variantName,
                )
              }
              is InitializationResult.NeedsBuild -> {
                // 此状态通常意味着项目中完全没有 R.class 或任何中间产物，需做一次增量即可
                modulePath = result.modulePath
                variantName = result.variantName
                initializationDeferred.complete(Unit)
                _previewState.value = PreviewState.NeedsBuild(result.modulePath, result.variantName)
              }
              is InitializationResult.Failed -> {
                isInitialized.set(false)
                initializationDeferred.complete(Unit)
                _previewState.value = PreviewState.Error(result.message)
              }
            }
          }
          .onFailure { error ->
            LOG.error("Hot-Reload Initialization failed", error)
            isInitialized.set(false)
            initializationDeferred.complete(Unit)
            _previewState.value = PreviewState.Error(error.message ?: "Initialization failed")
          }
    }
  }

  fun onSourceChanged(source: String) {
    currentSource = source
    val parsed = parseAndValidateSource(source) ?: return

    viewModelScope.launch { sourceChanges.emit(SourceUpdate(source, parsed)) }
  }

  fun compileNow(source: String) {
    currentSource = source
    val parsed = parseAndValidateSource(source) ?: return

    viewModelScope.launch { compilePreview(source, parsed) }
  }

  private fun parseAndValidateSource(source: String): ParsedPreviewSource? {
    if (_previewState.value is PreviewState.NeedsBuild) {
      LOG.debug("Skipping source processing - initial build required")
      return null
    }

    val parsed = sourceParser.parse(source)
    if (parsed == null) {
      _previewState.value = PreviewState.Error("Missing package declaration in source")
      return null
    }

    if (parsed.previewConfigs.isEmpty()) {
      _previewState.value = PreviewState.Empty
      return null
    }

    updateAvailablePreviews(parsed.previewConfigs)
    return parsed
  }

  private fun updateAvailablePreviews(configs: List<PreviewConfig>) {
    val functionNames = configs.map { it.functionName }
    _availablePreviews.value = functionNames
    if (_selectedPreview.value == null || !functionNames.contains(_selectedPreview.value)) {
      _selectedPreview.value = functionNames.first()
    }
  }

  private suspend fun compilePreview(source: String, parsed: ParsedPreviewSource) {
    initializationDeferred.await()

    if (!isInitialized.get()) {
      LOG.debug("Skipping compilePreview - initialization failed")
      return
    }

    if (_previewState.value is PreviewState.NeedsBuild) {
      LOG.debug("Skipping compilePreview - initial build required")
      return
    }

    _previewState.value = PreviewState.Compiling

    // 局部极速编译
    repository
        .compilePreview(source, parsed)
        .onSuccess { result ->
          _previewState.value =
              PreviewState.Ready(
                  dexFile = result.dexFile,
                  className = result.className,
                  previewConfigs = parsed.previewConfigs,
                  runtimeDex = result.runtimeDex,
                  projectDexFiles = result.projectDexFiles,
              )
        }
        .onFailure { error ->
          val diagnostics = if (error is CompilationException) error.diagnostics else emptyList()
          _previewState.value =
              PreviewState.Error(
                  message = error.message ?: "Compilation failed",
                  diagnostics = diagnostics,
              )
        }
  }

  fun setDisplayMode(mode: DisplayMode) {
    _displayMode.value = mode
  }

  fun toggleDisplayMode() {
    _displayMode.value =
        when (_displayMode.value) {
          DisplayMode.ALL -> DisplayMode.SINGLE
          DisplayMode.SINGLE -> DisplayMode.ALL
        }
  }

  fun selectPreview(functionName: String) {
    if (_availablePreviews.value.contains(functionName)) {
      _selectedPreview.value = functionName
    }
  }

  fun getModulePath(): String = modulePath ?: ""

  fun getVariantName(): String = variantName

  fun canTriggerBuild(): Boolean = !modulePath.isNullOrEmpty()

  fun setBuildingState() {
    _previewState.value = PreviewState.Building
  }

  fun setBuildFailed() {
    _previewState.value = PreviewState.Error("Hot-Reload Sync failed. Check IDE log for details.")
  }

  /** 接管构建后的刷新。 由于 AndroidIDE 的特殊性，我们在强制刷新时仅清空并重启内部状态， 而不再向系统外部丢巨大的 Gradle 任务，完全信赖我们改造后的局部编译流。 */
  fun refreshAfterBuild(context: Context) {
    viewModelScope.launch {
      initMutex.withLock {
        LOG.debug("refreshAfterBuild: Triggering forced hot-reload sync...")

        repository.reset()
        isInitialized.set(false)
        initializationDeferred = kotlinx.coroutines.CompletableDeferred()

        _previewState.value = PreviewState.Initializing

        repository
            .initialize(context, cachedFilePath)
            .onSuccess { result ->
              when (result) {
                is InitializationResult.Ready -> {
                  modulePath = result.projectContext.modulePath
                  variantName = result.projectContext.variantName
                  isInitialized.set(true)
                  initializationDeferred.complete(Unit)
                  if (currentSource.isNotBlank()) {
                    compileNow(currentSource)
                  } else {
                    _previewState.value = PreviewState.Idle
                  }
                }
                is InitializationResult.NeedsBuild -> {
                  modulePath = result.modulePath
                  variantName = result.variantName
                  isInitialized.set(true)
                  initializationDeferred.complete(Unit)
                  _previewState.value =
                      PreviewState.NeedsBuild(result.modulePath, result.variantName)
                }
                is InitializationResult.Failed -> {
                  initializationDeferred.complete(Unit)
                  LOG.error("refreshAfterBuild: sync failed - {}", result.message)
                  _previewState.value = PreviewState.Error(result.message)
                }
              }
            }
            .onFailure { error ->
              initializationDeferred.complete(Unit)
              LOG.error("refreshAfterBuild: sync failed", error)
              _previewState.value = PreviewState.Error(error.message ?: "Sync failed")
            }
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    repository.reset()
    LOG.debug("ComposePreviewViewModel cleared")
  }

  companion object {
    private val LOG = LoggerFactory.getLogger(ComposePreviewViewModel::class.java)
    private const val DEBOUNCE_MS = 500L
  }
}
