package android.zero.studio.images.preview.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import android.zero.studio.images.preview.R
import android.zero.studio.images.preview.databinding.FragmentImagePreviewBinding
import android.zero.studio.images.preview.utils.ImageFileUtils
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.DialogFragment
import java.io.File

/**
 * The main entry point for Image Preview.
 * Implemented as a FullScreen DialogFragment.
 */
class ImagePreviewFragment : DialogFragment() {

    private var _binding: FragmentImagePreviewBinding? = null
    private val binding get() = _binding!!

    private var currentFile: File? = null
    private var fileList: List<File> = emptyList()
    
    // Background modes: Black -> White -> Gray -> Checkerboard(Simulated by Light Gray)
    private val bgColors = listOf(
        Color.BLACK,
        Color.WHITE,
        Color.DKGRAY,
        Color.LTGRAY // Representing transparency check
    )
    private var bgIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        
        arguments?.getString(ARG_PATH)?.let {
            currentFile = File(it)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(android.view.Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImagePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSystemBars()
        setupToolbar()
        loadFileContext() // Pre-load sibling files for navigation
        loadImage()

        // Background Switcher
        binding.btnToggleBg.setOnClickListener {
            bgIndex = (bgIndex + 1) % bgColors.size
            updateBackgroundColor()
        }

        // Info Button
        binding.btnInfo.setOnClickListener {
            currentFile?.let { file ->
                val w = binding.previewView.getThorVG().width.toInt()
                val h = binding.previewView.getThorVG().height.toInt()
                FileInfoBottomSheet.newInstance(file.absolutePath, w, h)
                    .show(childFragmentManager, "file_info")
            }
        }
        
        // Transparency Slider (Alpha of background color)
        binding.sliderOpacity.addOnChangeListener { _, value, _ ->
            val baseColor = bgColors[bgIndex]
            val alpha = (value * 255).toInt()
            val newColor = (alpha shl 24) or (baseColor and 0x00FFFFFF)
            binding.previewView.backgroundColorVal = newColor
        }

        // Swipe Navigation
        binding.previewView.onFileSwitchRequest = { direction ->
            navigateFile(direction)
        }
    }

    private fun setupSystemBars() {
        dialog?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        binding.toolbar.inflateMenu(R.menu.menu_image_preview)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_convert -> {
                    Toast.makeText(context, "Convert feature coming soon", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFileContext() {
        currentFile?.parentFile?.listFiles()?.let { files ->
            fileList = files.filter { 
                it.isFile && ImageFileUtils.isSupportedImage(it) 
            }.sorted()
        }
    }

    private fun loadImage() {
        val file = currentFile ?: return
        if (!file.exists()) {
            Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
            return
        }

        binding.toolbar.title = file.name
        binding.previewView.load(file.absolutePath)
        updateBackgroundColor()
    }
    
    private fun updateBackgroundColor() {
        val color = bgColors[bgIndex]
        // Apply slider alpha
        val alpha = (binding.sliderOpacity.value * 255).toInt()
        val finalColor = (alpha shl 24) or (color and 0x00FFFFFF)
        
        binding.previewContainer.setBackgroundColor(finalColor) // Set view container bg
        binding.previewView.backgroundColorVal = 0 // Clear native bg to let container show through or set native?
        // Actually, ThorVGImageView needs to know if it should clear pixels.
        // Let's set it on the view for native handling if specific render is needed, 
        // OR just set the View background if transparent.
        // Simple: Set View background. Native renders transparent pixels.
        binding.previewView.setBackgroundColor(finalColor) 
    }

    private fun navigateFile(direction: Int) {
        if (fileList.isEmpty()) return
        val currentIndex = fileList.indexOfFirst { it.name == currentFile?.name }
        if (currentIndex == -1) return

        val nextIndex = currentIndex + direction
        if (nextIndex in fileList.indices) {
            currentFile = fileList[nextIndex]
            loadImage()
        } else {
            // Bounce animation or toast
            if (direction < 0) Toast.makeText(context, "First file", Toast.LENGTH_SHORT).show()
            else Toast.makeText(context, "Last file", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PATH = "arg_file_path"

        fun newInstance(path: String): ImagePreviewFragment {
            return ImagePreviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PATH, path)
                }
            }
        }
    }
}