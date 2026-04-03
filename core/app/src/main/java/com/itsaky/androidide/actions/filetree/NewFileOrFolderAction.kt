package com.itsaky.androidide.actions.filetree

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itsaky.androidide.R.layout
import com.itsaky.androidide.actions.ActionData
import com.itsaky.androidide.actions.getContext
import com.itsaky.androidide.actions.requireFile
import com.itsaky.androidide.resources.R
import com.itsaky.androidide.utils.DialogUtils
import com.itsaky.androidide.utils.flashError
import com.itsaky.androidide.utils.flashSuccess
import java.io.File
import java.io.IOException

/** @author android_zero */
class NewFileOrFolderAction(context: Context, override val order: Int) :
    BaseDirNodeAction(
        context = context,
        labelRes = R.string.action_create_file_folder,
        iconRes = R.drawable.ic_new_folder,
    ) {

  private val PREFS_NAME = "NewFileOrFolderActionPrefs"
  private val PREF_REMOVE_SPACES_CHECKED = "remove_spaces_checked"
  private val PREF_SELECTED_LIST_TYPE = "selected_list_type" // 0 for suffix, 1 for history
  private val PREF_HISTORY_LIST = "history_list_json"
  private val MAX_HISTORY_SIZE = 10
  private val gson = Gson()

  override val id: String = "ide.editor.fileTree.newFolderOrFile"

  override suspend fun execAction(data: ActionData) {
    val activityContext: Activity = data.getContext() as? Activity ?: return
    val currentDir = data.requireFile()
    val prefs: SharedPreferences =
        activityContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val dialogView =
        LayoutInflater.from(activityContext).inflate(layout.layout_dialog_fodle_orfile, null)
    val builder = DialogUtils.newMaterialDialogBuilder(activityContext)

    val editText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_name_input)
    val dropdownArrow = dialogView.findViewById<ImageButton>(R.id.dropdown_arrow)
    val checkboxRemoveSpaces = dialogView.findViewById<CheckBox>(R.id.checkbox_remove_spaces)
    val checkboxDotsToSlashes = dialogView.findViewById<CheckBox>(R.id.checkbox_dots_to_slashes)
    val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btn_cancel)
    val btnPaste = dialogView.findViewById<MaterialButton>(R.id.btn_paste)
    val btnFile = dialogView.findViewById<MaterialButton>(R.id.btn_file)
    val btnFolder = dialogView.findViewById<MaterialButton>(R.id.btn_folder)

    builder.setTitle(R.string.new_folder)
    builder.setMessage(R.string.msg_can_contain_slashesfile)
    builder.setView(dialogView)
    builder.setCancelable(false)

    val allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_./"
    editText.filters =
        arrayOf(
            InputFilter { source: CharSequence, start: Int, end: Int, _: Spanned, _: Int, _: Int ->
              for (i in start until end) {
                val char = source[i]
                if (!allowedChars.contains(char)) {
                  flashError(
                      activityContext.getString(R.string.msg_unsupported_characters, "'$char'")
                  )
                  return@InputFilter ""
                }
              }
              null
            }
        )

    checkboxRemoveSpaces.isChecked = prefs.getBoolean(PREF_REMOVE_SPACES_CHECKED, false)
    checkboxRemoveSpaces.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
      prefs.edit().putBoolean(PREF_REMOVE_SPACES_CHECKED, isChecked).apply()
    }
    checkboxDotsToSlashes.isChecked = false

    btnPaste.setOnClickListener { _: View ->
      val clipboard =
          activityContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
      if (clipboard.hasPrimaryClip() && clipboard.primaryClip?.itemCount ?: 0 > 0) {
        val pasteData = clipboard.primaryClip?.getItemAt(0)?.text
        if (pasteData != null) {
          editText.setText(pasteData)
          editText.text?.length?.let { editText.setSelection(it) }
          flashSuccess(R.string.msg_paste_successful)
        }
      } else {
        flashError(R.string.msg_clipboard_empty)
      }
    }

    dropdownArrow.setOnClickListener { view: View ->
      showSuffixHistoryPopup(activityContext, view, editText, prefs)
    }

    val dialog = builder.create()

    btnCancel.setOnClickListener { dialog.dismiss() }

    btnFile.setOnClickListener {
      dialog.dismiss()
      var inputName = editText.text.toString().trim()
      if (checkboxRemoveSpaces.isChecked) inputName = inputName.replace(" ", "")
      addHistoryEntry(prefs, inputName)
      handleCreation(activityContext, currentDir, inputName, true, false)
    }

    btnFolder.setOnClickListener {
      dialog.dismiss()
      var inputName = editText.text.toString().trim()
      if (checkboxRemoveSpaces.isChecked) inputName = inputName.replace(" ", "")
      addHistoryEntry(prefs, inputName)
      handleCreation(activityContext, currentDir, inputName, false, checkboxDotsToSlashes.isChecked)
    }

    dialog.show()
  }

  private fun handleCreation(
      context: Context,
      currentDir: File,
      inputName: String,
      isFileToCreate: Boolean,
      convertDotsToSlashes: Boolean,
  ) {
    if (inputName.isEmpty()) {
      flashError(R.string.msg_invalid_name)
      return
    }

    val processedPath: String
    if (isFileToCreate) {
      processedPath = inputName.replace("\\", "/").trimStart('/').trimEnd('/')
    } else {
      var tempPath = inputName.replace("\\", "/")
      if (convertDotsToSlashes) tempPath = tempPath.replace(".", "/")
      processedPath = tempPath.trimStart('/').trimEnd('/')
    }

    if (processedPath.isEmpty()) {
      flashError(R.string.msg_invalid_name)
      return
    }

    if (!isValidFileName(processedPath)) {
      flashError(
          context.getString(
              R.string.msg_unsupported_characters,
              getUnsupportedCharacters(processedPath),
          )
      )
      return
    }

    if (!currentDir.exists() || !currentDir.isDirectory) {
      flashError(context.getString(R.string.msg_root_folder_not_found, currentDir.absolutePath))
      return
    }

    if (isFileToCreate) {
      val lastSlashIndex = processedPath.lastIndexOf('/')
      val parentPathSegment =
          if (lastSlashIndex != -1) processedPath.substring(0, lastSlashIndex) else ""
      val fileName =
          if (lastSlashIndex != -1) processedPath.substring(lastSlashIndex + 1) else processedPath

      if (fileName.isEmpty()) {
        flashError(R.string.msg_invalid_name)
        return
      }
      val finalParentDir =
          if (parentPathSegment.isNotEmpty()) File(currentDir, parentPathSegment) else currentDir
      createFile(context, File(finalParentDir, fileName))
    } else {
      createFolder(context, File(currentDir, processedPath))
    }
  }

  private fun createFile(context: Context, targetFile: File) {
    try {
      val parentDir = targetFile.parentFile
      if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
        flashError(
            context.getString(
                R.string.msg_folder_creation_failed_for_parent,
                parentDir.absolutePath,
            )
        )
        return
      }

      if (targetFile.exists()) {
        flashError(R.string.msg_file_exists)
        return
      }

      if (targetFile.createNewFile()) {
        flashSuccess(R.string.msg_file_created)
        requestFileListing()
      } else {
        flashError(R.string.msg_file_creation_failed)
      }
    } catch (e: IOException) {
      flashError(
          context.getString(R.string.msg_file_creation_exception, e.localizedMessage ?: "未知错误")
      )
    }
  }

  private fun createFolder(context: Context, targetFolder: File) {
    if (targetFolder.exists()) {
      flashError(R.string.msg_folder_exists)
      return
    }

    try {
      if (targetFolder.mkdirs()) {
        flashSuccess(R.string.msg_folder_created)
        requestFileListing()
      } else {
        flashError(R.string.msg_folder_creation_failed)
      }
    } catch (e: IOException) {
      flashError(
          context.getString(R.string.msg_folder_creation_exception, e.localizedMessage ?: "未知错误")
      )
    }
  }

  private class ItemAdapter(
      private var items: List<String>,
      private val onItemClick: (String) -> Unit,
  ) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
      val textView: android.widget.TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
      return ItemViewHolder(
          LayoutInflater.from(parent.context)
              .inflate(android.R.layout.simple_list_item_1, parent, false)
              as android.widget.TextView
      )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
      val item = items[position]
      holder.textView.text = item
      holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<String>) {
      items = newItems
      notifyDataSetChanged()
    }
  }

  private fun addHistoryEntry(prefs: SharedPreferences, entry: String) {
    if (entry.isBlank()) return
    val historyList = getHistoryEntries(prefs).toMutableList()
    historyList.remove(entry)
    historyList.add(0, entry)
    while (historyList.size > MAX_HISTORY_SIZE) historyList.removeAt(historyList.size - 1)
    prefs.edit().putString(PREF_HISTORY_LIST, gson.toJson(historyList)).apply()
  }

  private fun getHistoryEntries(prefs: SharedPreferences): List<String> {
    val jsonString = prefs.getString(PREF_HISTORY_LIST, null)
    return if (jsonString != null)
        gson.fromJson(jsonString, object : TypeToken<List<String>>() {}.type)
    else emptyList()
  }

  private fun showSuffixHistoryPopup(
      context: Context,
      anchorView: View,
      editText: TextInputEditText,
      prefs: SharedPreferences,
  ) {
    val popupView = LayoutInflater.from(context).inflate(R.layout.layout_suffix_history_popup, null)
    val popupWindow =
        PopupWindow(popupView, anchorView.width * 2, RecyclerView.LayoutParams.WRAP_CONTENT, true)
    popupWindow.isOutsideTouchable = true
    popupWindow.isFocusable = true
    popupWindow.setBackgroundDrawable(null)

    val radioGroup = popupView.findViewById<RadioGroup>(R.id.radio_group_list_type)
    val radioSuffix = popupView.findViewById<RadioButton>(R.id.radio_suffix)
    val radioHistory = popupView.findViewById<RadioButton>(R.id.radio_history)
    val recyclerViewSuffix = popupView.findViewById<RecyclerView>(R.id.recycler_view_suffix)
    val recyclerViewHistory = popupView.findViewById<RecyclerView>(R.id.recycler_view_history)

    recyclerViewSuffix.layoutManager = LinearLayoutManager(context)
    recyclerViewHistory.layoutManager = LinearLayoutManager(context)

    val suffixList =
        listOf(
            ".txt",
            ".java",
            ".kt",
            ".xml",
            ".gradle",
            ".gradle.kts",
            ".md",
            ".html",
            ".css",
            ".js",
            ".json",
            ".py",
            ".c",
            ".cpp",
            ".h",
            ".sh",
            ".go",
            ".rs",
            ".rb",
            ".php",
            ".swift",
            ".dart",
            ".yml",
            ".gitignore",
            ".properties",
        )
    recyclerViewSuffix.adapter =
        ItemAdapter(suffixList) {
          editText.append(it)
          popupWindow.dismiss()
        }

    val historyList = getHistoryEntries(prefs).toMutableList()
    val historyAdapter =
        ItemAdapter(historyList) {
          editText.setText(it)
          editText.text?.length?.let { len -> editText.setSelection(len) }
          popupWindow.dismiss()
        }
    recyclerViewHistory.adapter = historyAdapter

    if (prefs.getInt(PREF_SELECTED_LIST_TYPE, 0) == 0) {
      radioSuffix.isChecked = true
      recyclerViewSuffix.visibility = View.VISIBLE
      recyclerViewHistory.visibility = View.GONE
    } else {
      radioHistory.isChecked = true
      recyclerViewSuffix.visibility = View.GONE
      recyclerViewHistory.visibility = View.VISIBLE
    }

    radioGroup.setOnCheckedChangeListener { _, checkedId ->
      if (checkedId == R.id.radio_suffix) {
        recyclerViewSuffix.visibility = View.VISIBLE
        recyclerViewHistory.visibility = View.GONE
        prefs.edit().putInt(PREF_SELECTED_LIST_TYPE, 0).apply()
      } else {
        historyAdapter.updateData(getHistoryEntries(prefs))
        recyclerViewSuffix.visibility = View.GONE
        recyclerViewHistory.visibility = View.VISIBLE
        prefs.edit().putInt(PREF_SELECTED_LIST_TYPE, 1).apply()
      }
    }

    popupWindow.showAsDropDown(anchorView)
  }

  private fun isValidFileName(fileName: String): Boolean = fileName.none {
    it in charArrayOf('\\', ':', '*', '?', '"', '<', '>', '|', '\u0000')
  }

  private fun getUnsupportedCharacters(fileName: String): String =
      fileName
          .filter { it in charArrayOf('\\', ':', '*', '?', '"', '<', '>', '|', '\u0000') }
          .toSet()
          .joinToString(" ") { "'$it'" }
}
