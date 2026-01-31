package android.zero.studio.layouteditor.fragments.resources

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.zero.studio.layouteditor.ProjectFile
import android.zero.studio.layouteditor.R
import android.zero.studio.layouteditor.adapters.StringResourceAdapter
import android.zero.studio.layouteditor.adapters.models.ValuesItem
import android.zero.studio.layouteditor.databinding.FragmentResourcesBinding
import android.zero.studio.layouteditor.databinding.LayoutValuesItemDialogBinding
import android.zero.studio.layouteditor.managers.ProjectManager
import android.zero.studio.layouteditor.tools.ValuesResourceParser
import android.zero.studio.layouteditor.utils.NameErrorChecker
import android.zero.studio.layouteditor.utils.SBUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Fragment used to manage String resources.
 *
 * @author android_zero
 */
class StringFragment : Fragment() {

    // ViewBinding standard implementation to prevent memory leaks
    private var _binding: FragmentResourcesBinding? = null
    private val binding get() = _binding!!

    // Data source
    private val stringList = ArrayList<ValuesItem>()
    private lateinit var adapter: StringResourceAdapter
    private lateinit var project: ProjectFile

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResourcesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val manager = ProjectManager.instance
        val openedProject = manager.openedProject
        
        if (openedProject == null || openedProject !is ProjectFile) {
            showError("No project opened.")
            return
        }
        
        // Explicit cast to satisfy compiler
        project = openedProject as ProjectFile

        setupRecyclerView()
        loadStringsFromXMLAsync(project.stringsPath)
    }

    private fun setupRecyclerView() {
        adapter = StringResourceAdapter(project, stringList)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@StringFragment.adapter
            setHasFixedSize(true) // Optimization: Improves performance if item size is constant
        }
    }

    /**
     * Loads string resources asynchronously using Kotlin Coroutines.
     * Use [Dispatchers.IO] for file operations to keep the UI thread free.
     *
     * @param filePath The absolute path to strings.xml
     */
    private fun loadStringsFromXMLAsync(filePath: String) {
        // Show loading state if you have a ProgressBar in your layout
        // binding.progressBar.isVisible = true 

        viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                parseXmlInBackground(filePath)
            }

            // Fragment might be destroyed while IO was happening
            if (!isAdded || _binding == null) return@launch

            result.fold(
                onSuccess = { items ->
                    stringList.clear()
                    stringList.addAll(items)
                    adapter.notifyDataSetChanged()
                    
                    if (items.isEmpty()) {
                        // Optional: Show empty state
                        // SBUtils.make(binding.root, "No strings found.").setType(SBUtils.Type.INFO).show()
                    }
                },
                onFailure = { error ->
                    handleLoadError(error)
                }
            )
        }
    }

    /**
     * Performs the heavy lifting of IO and Parsing.
     * Using BufferedInputStream for better IO performance on large files.
     */
    private fun parseXmlInBackground(filePath: String): Result<List<ValuesItem>> {
        return try {
            val fileStream = FileInputStream(filePath)
            // Use buffered stream to minimize syscalls, critical for large XML files
            fileStream.buffered().use { bufferedStream ->
                val parser = ValuesResourceParser(bufferedStream, ValuesResourceParser.TAG_STRING)
                val items = parser.valuesList ?: emptyList()
                
                // Filter out invalid items immediately to ensure stability
                val validItems = items.filter { it.name.isNotEmpty() }
                Result.success(validItems)
            }
        } catch (e: FileNotFoundException) {
            Result.failure(e)
        } catch (e: Exception) {
            // Catch XML parsing errors or other IO exceptions
            Result.failure(e)
        }
    }

    private fun handleLoadError(error: Throwable) {
        val message = when (error) {
            is FileNotFoundException -> "strings.xml not found."
            is IOException -> "Error reading strings.xml: ${error.message}"
            else -> "Failed to parse strings: ${error.localizedMessage}"
        }
        showError(message)
    }

    /**
     * Displays the Add String dialog.
     * Uses ViewBinding for the dialog layout to avoid manual findViewById calls.
     */
    fun addString() {
        val context = requireContext()
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle(R.string.add) // Assuming "New String" or "Add" resource

        // Inflate dialog layout using ViewBinding
        val dialogBinding = LayoutValuesItemDialogBinding.inflate(layoutInflater)
        
        builder.setView(dialogBinding.root)

        builder.setPositiveButton(R.string.add) { _, _ ->
            val name = dialogBinding.textinputName.text.toString().trim()
            val value = dialogBinding.textinputValue.text.toString()

            if (name.isNotEmpty()) {
                val newItem = ValuesItem(name, value)
                performAddString(newItem)
            } else {
               showError(getString(R.string.msg_cannnot_empty))
            }
        }
        
        builder.setNegativeButton(R.string.cancel, null)

        val dialog = builder.create()
        dialog.show()

        // Real-time validation
        dialogBinding.textinputName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                NameErrorChecker.checkForValues(
                    s.toString(),
                    dialogBinding.textInputLayoutName,
                    dialog,
                    stringList
                )
            }
        })
        
        // Initial check
        NameErrorChecker.checkForValues(
            dialogBinding.textinputName.text.toString(),
            dialogBinding.textInputLayoutName,
            dialog,
            stringList
        )
    }

    private fun performAddString(item: ValuesItem) {
        stringList.add(item)
        // More efficient than notifyDataSetChanged for single insertions
        adapter.notifyItemInserted(stringList.size - 1)
        
        // Offload file writing to background thread
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Ensure generateStringsXml is thread-safe or synchronization is handled in adapter
                // Assuming adapter.generateStringsXml() writes to disk
                adapter.generateStringsXml()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                     showError("Failed to save XML: ${e.message}")
                }
            }
        }
    }

    private fun showError(message: String) {
        _binding?.root?.let { view ->
            SBUtils.make(view, message)
                .setFadeAnimation()
                .setType(SBUtils.Type.ERROR)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Prevent memory leaks
        _binding = null
    }
}