package android.zero.studio.images.preview.utils

import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ImageMetadata(
    val name: String,
    val path: String,
    val size: String,
    val date: String,
    val resolution: String,
    val md5: String,
    val sha256: String,
)

object ImageFileUtils {

  suspend fun getMetadata(file: File, width: Int, height: Int): ImageMetadata =
      withContext(Dispatchers.IO) {
        val size = formatSize(file.length())
        val date =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date(file.lastModified()))
        val resolution = "${width}x${height}"
        val md5 = calculateHash(file, "MD5")
        val sha256 = calculateHash(file, "SHA-256")

        ImageMetadata(
            name = file.name,
            path = file.absolutePath,
            size = size,
            date = date,
            resolution = resolution,
            md5 = md5,
            sha256 = sha256,
        )
      }

  private fun formatSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(
        "%.1f %s",
        size / Math.pow(1024.0, digitGroups.toDouble()),
        units[digitGroups],
    )
  }

  private fun calculateHash(file: File, algorithm: String): String {
    return try {
      val digest = MessageDigest.getInstance(algorithm)
      val inputStream = file.inputStream()
      val buffer = ByteArray(8192)
      var bytesRead: Int
      while (inputStream.read(buffer).also { bytesRead = it } != -1) {
        digest.update(buffer, 0, bytesRead)
      }
      inputStream.close()
      bytesToHex(digest.digest())
    } catch (e: Exception) {
      "Error"
    }
  }

  private fun bytesToHex(bytes: ByteArray): String {
    val sb = StringBuilder()
    for (b in bytes) {
      sb.append(String.format("%02x", b))
    }
    return sb.toString()
  }

  // Check if file is supported by ThorVG/Our Loader
  fun isSupportedImage(file: File): Boolean {
    val ext = file.extension.lowercase()
    return when (ext) {
      "svg",
      "xml",
      "json",
      "lot",
      "lottie",
      "png",
      "jpg",
      "jpeg",
      "webp",
      "gif" -> true
      else -> false
    }
  }
}
