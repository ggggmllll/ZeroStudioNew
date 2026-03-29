package android.zero.studio.images.preview.ui

import android.app.Dialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.zero.studio.images.preview.ThorVG
import android.zero.studio.images.preview.databinding.DialogImageDetailsBinding
import android.zero.studio.images.preview.utils.ImageFileUtils
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File
import kotlinx.coroutines.launch

class FileInfoBottomSheet : BottomSheetDialogFragment() {

  private var _binding: DialogImageDetailsBinding? = null
  private val binding
    get() = _binding!!

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
    dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
    return dialog
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {
    _binding = DialogImageDetailsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val path = arguments?.getString(ARG_PATH) ?: return
    val w = arguments?.getInt(ARG_WIDTH) ?: 0
    val h = arguments?.getInt(ARG_HEIGHT) ?: 0
    val file = File(path)

    loadMetadata(file, w, h)
    loadHistogram(file)

    // Copy listeners
    setupCopyListener(binding.tvPath, "Path")
    setupCopyListener(binding.tvMd5, "MD5")
    setupCopyListener(binding.tvSha256, "SHA-256")
  }

  private fun loadMetadata(file: File, w: Int, h: Int) {
    lifecycleScope.launch {
      val meta = ImageFileUtils.getMetadata(file, w, h)

      binding.tvName.text = meta.name
      binding.tvPath.text = meta.path
      binding.tvSize.text = meta.size
      binding.tvDate.text = meta.date
      binding.tvResolution.text = meta.resolution
      binding.tvMd5.text = meta.md5
      binding.tvSha256.text = meta.sha256
    }
  }

  private fun loadHistogram(file: File) {
    lifecycleScope.launch {
      // For histogram, we need a bitmap.
      // If vector, this is tricky as we need to render it.
      // For now, let's try decoding standard images.
      // If it's a vector, we might skip or need the parent fragment to pass the bitmap (memory
      // intensive).
      // Optimization: Decode a small version for histogram.

      val ext = file.extension.lowercase()
      if (ext in listOf("png", "jpg", "jpeg", "webp", "gif")) {
        try {
          val opts = BitmapFactory.Options()
          opts.inSampleSize = 4 // Downsample for speed
          val bmp = BitmapFactory.decodeFile(file.absolutePath, opts)
          if (bmp != null) {
            val histData = ThorVG().getHistogram(bmp)
            binding.histogramView.setData(histData)
            bmp.recycle()
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }
      } else {
        // Vector histogram not implemented in this simplified flow
        // without rendering context sharing.
        binding.histogramView.visibility = View.GONE
        binding.tvHistogramLabel.visibility = View.GONE
      }
    }
  }

  private fun setupCopyListener(view: View, label: String) {
    view.setOnClickListener {
      val text = (view as? android.widget.TextView)?.text?.toString() ?: ""
      val clipboard =
          requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE)
              as android.content.ClipboardManager
      val clip = android.content.ClipData.newPlainText(label, text)
      clipboard.setPrimaryClip(clip)
      Toast.makeText(context, "$label copied", Toast.LENGTH_SHORT).show()
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  companion object {
    private const val ARG_PATH = "arg_path"
    private const val ARG_WIDTH = "arg_w"
    private const val ARG_HEIGHT = "arg_h"

    fun newInstance(path: String, width: Int, height: Int): FileInfoBottomSheet {
      return FileInfoBottomSheet().apply {
        arguments =
            Bundle().apply {
              putString(ARG_PATH, path)
              putInt(ARG_WIDTH, width)
              putInt(ARG_HEIGHT, height)
            }
      }
    }
  }
}
