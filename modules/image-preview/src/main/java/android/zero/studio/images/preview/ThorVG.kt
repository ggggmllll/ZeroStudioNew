package android.zero.studio.images.preview

import android.graphics.Bitmap
import android.util.Log
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Kotlin wrapper for ThorVG Native Engine.
 * Handles lifecycle of the native context and provides methods to control rendering.
 *
 * @author android_zero
 */
class ThorVG : Closeable {

    private var nativeHandle: Long = 0
    private val isClosed = AtomicBoolean(false)

    // Image Information
    var width: Float = 0f
        private set
    var height: Float = 0f
        private set
    var totalFrames: Float = 0f
        private set
    var duration: Float = 0f
        private set
    
    val isAnimation: Boolean
        get() = totalFrames > 1.0f

    init {
        nativeHandle = nativeCreate()
    }

    /**
     * Load an image file (SVG, Lottie, PNG, JPG, WebP, etc).
     * @return true if successful.
     */
    fun load(path: String): Boolean {
        check(!isClosed.get()) { "ThorVG context is closed" }
        val success = nativeLoad(nativeHandle, path)
        if (success) {
            val info = nativeGetInfo(nativeHandle)
            if (info != null && info.size >= 4) {
                width = info[0]
                height = info[1]
                totalFrames = info[2]
                duration = info[3]
            }
        }
        return success
    }

    /**
     * Render current state to bitmap.
     * @param bitmap Mutable bitmap (Config.ARGB_8888).
     * @param backgroundColor ARGB color to fill background (0 for transparent).
     */
    fun render(bitmap: Bitmap, backgroundColor: Int = 0): Boolean {
        check(!isClosed.get()) { "ThorVG context is closed" }
        return nativeRender(nativeHandle, bitmap, backgroundColor)
    }

    /**
     * Seek animation to specific progress.
     * @param progress 0.0 ~ 1.0
     */
    fun seek(progress: Float): Boolean {
        check(!isClosed.get()) { "ThorVG context is closed" }
        if (!isAnimation) return false
        return nativeSeek(nativeHandle, progress)
    }

    /**
     * Calculate color histogram (Peak Map) from a Bitmap.
     * Static utility, doesn't require instance handle.
     */
    fun getHistogram(bitmap: Bitmap): IntArray? {
        return nativeGetHistogram(bitmap)
    }

    override fun close() {
        if (isClosed.compareAndSet(false, true)) {
            nativeDestroy(nativeHandle)
            nativeHandle = 0
        }
    }

    protected fun finalize() {
        if (!isClosed.get()) {
            close()
        }
    }

    // --- Native Methods ---
    private external fun nativeCreate(): Long
    private external fun nativeDestroy(handle: Long)
    private external fun nativeLoad(handle: Long, path: String): Boolean
    private external fun nativeRender(handle: Long, bitmap: Bitmap, bgColor: Int): Boolean
    private external fun nativeSeek(handle: Long, progress: Float): Boolean
    private external fun nativeGetInfo(handle: Long): FloatArray? // [w, h, frames, duration]
    
    // Static util (passed bitmap directly)
    private external fun nativeGetHistogram(bitmap: Bitmap): IntArray?

    companion object {
        init {
            try {
                System.loadLibrary("image-preview")
                nativeInit()
            } catch (e: UnsatisfiedLinkError) {
                Log.e("ThorVG", "Failed to load native library", e)
            }
        }

        @JvmStatic
        private external fun nativeInit()
        @JvmStatic
        private external fun nativeTerm()
    }
}