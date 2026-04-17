package com.itsaky.androidide.actions.lsp.kotlin

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.itsaky.androidide.actions.*
import com.itsaky.androidide.lsp.kotlin.actions.ConvertJavaToKotlinAction
import com.itsaky.androidide.lsp.kotlin.actions.KotlinFindReferencesAction
import com.itsaky.androidide.lsp.kotlin.actions.KotlinGoToDefinitionAction
import com.itsaky.androidide.lsp.kotlin.actions.KotlinHoverAction
import com.itsaky.androidide.lsp.kotlin.actions.KotlinRenameAction
import com.itsaky.androidide.lsp.kotlin.actions.KotlinShowCodeActions
import com.itsaky.androidide.lsp.kotlin.actions.RefreshBazelClasspathAction
import com.itsaky.androidide.resources.R

/**
 * @param context The application context, used for retrieving resources.
 * @param order The order of this action in menus or toolbars.
 */
class kotlinLspActionMenu(context: Context, override val order: Int) :
    EditorRelatedAction(), ActionMenu {

  override val children: MutableSet<ActionItem> = mutableSetOf()
  override val id: String = "ide.editor.code.text.code_menu"

  /**
   * Initializes the menu action by setting its label and icon, and registering all its child
   * actions.
   */
  init {
    label = context.getString(R.string.edit)
    icon = ContextCompat.getDrawable(context, R.drawable.ic_code)
    addAction(KotlinGoToDefinitionAction)
    addAction(KotlinFindReferencesAction)
    addAction(KotlinHoverAction)
    addAction(KotlinRenameAction)
    addAction(KotlinShowCodeActions)
    addAction(RefreshBazelClasspathAction)
    addAction(ConvertJavaToKotlinAction)
  }

  override fun prepare(data: ActionData) {
    super<EditorRelatedAction>.prepare(data)
    super<ActionMenu>.prepare(data)

    if (!visible) {
      return
    }

    if (data.getEditor() == null) {
      markInvisible()
      return
    }

    // The "Edit" menu itself should be visible if an editor exists.
    // Child actions will handle their own enabled state.
    enabled = true
  }

  /**
   * Executes the action. For an [ActionMenu], this method is a no-op as the framework is
   * responsible for displaying the sub-menu.
   *
   * @return `true` to indicate the action was handled.
   */
  override suspend fun execAction(data: ActionData): Boolean {
    Log.d(
        "EditorEditLineMenuAction",
        "execAction called. Framework should handle sub-menu display.",
    )
    return true
  }
}
