package android.zero.studio.lsp.clang

import com.itsaky.androidide.progress.ICancelChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Centralized request polling/cancellation logic for clangd native requests. */
class ClangdRequestDispatcher(
    private val pollIntervalMs: Long = 20L,
) {

  suspend fun await(
      requestId: Long,
      timeoutMs: Long,
      cancelChecker: ICancelChecker,
  ): String? =
      withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start <= timeoutMs) {
          cancelChecker.abortIfCancelled()
          val result = ClangdNativeBridge.nativeGetResult(requestId)
          if (result != null) {
            return@withContext result
          }
          delay(pollIntervalMs)
        }
        ClangdNativeBridge.nativeNotifyRequestTimeout(requestId)
        return@withContext null
      }

  fun cancel(requestId: Long) {
    ClangdNativeBridge.nativeCancelRequestInternal(requestId)
  }
}
