package android.zero.studio.lsp.clang

import java.util.concurrent.CopyOnWriteArraySet

/** Thin static bridge over JNI methods exported by simple_lsp_jni.cpp. */
object ClangdNativeBridge {

  fun interface DiagnosticsListener {
    fun onDiagnostics(fileUri: String, diagnostics: List<ClangDiagnosticItem>)
  }

  fun interface HealthListener {
    fun onHealth(type: String, message: String)
  }

  private val diagnosticsListeners = CopyOnWriteArraySet<DiagnosticsListener>()
  private val healthListeners = CopyOnWriteArraySet<HealthListener>()

  init {
    System.loadLibrary("native_compiler")
    nativeOnLoad()
  }

  fun addDiagnosticsListener(listener: DiagnosticsListener) {
    diagnosticsListeners.add(listener)
  }

  fun removeDiagnosticsListener(listener: DiagnosticsListener) {
    diagnosticsListeners.remove(listener)
  }

  fun addHealthListener(listener: HealthListener) {
    healthListeners.add(listener)
  }

  fun removeHealthListener(listener: HealthListener) {
    healthListeners.remove(listener)
  }

  @JvmStatic
  fun handleNativeDiagnostics(fileUri: String, diagnostics: List<ClangDiagnosticItem>) {
    diagnosticsListeners.forEach { it.onDiagnostics(fileUri, diagnostics) }
  }

  @JvmStatic
  fun handleNativeHealthEvent(type: String, message: String) {
    healthListeners.forEach { it.onHealth(type, message) }
  }

  @JvmStatic private external fun nativeOnLoad(): Int

  @JvmStatic
  external fun nativeInitialize(clangdPath: String, workDir: String, completionLimit: Int): Boolean

  @JvmStatic external fun nativeShutdown()

  @JvmStatic external fun nativeIsInitialized(): Boolean

  @JvmStatic external fun nativeRequestHover(fileUri: String, line: Int, character: Int): Long

  @JvmStatic
  external fun nativeRequestCompletion(
      fileUri: String,
      line: Int,
      character: Int,
      triggerCharacter: String?,
  ): Long

  @JvmStatic external fun nativeRequestDefinition(fileUri: String, line: Int, character: Int): Long

  @JvmStatic
  external fun nativeRequestReferences(
      fileUri: String,
      line: Int,
      character: Int,
      includeDeclaration: Boolean,
  ): Long

  @JvmStatic external fun nativeGetResult(requestId: Long): String?

  @JvmStatic external fun nativeDidOpen(fileUri: String, content: String, languageId: String?)

  @JvmStatic external fun nativeDidChange(fileUri: String, content: String, version: Int)

  @JvmStatic external fun nativeDidClose(fileUri: String)

  @JvmStatic external fun nativeCancelRequestInternal(requestId: Long)

  @JvmStatic external fun nativeNotifyRequestTimeout(requestId: Long)
}
