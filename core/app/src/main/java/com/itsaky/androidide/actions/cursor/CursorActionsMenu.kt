package com.itsaky.androidide.actions.cursor

import android.content.Context
import com.itsaky.androidide.resources.R
import com.itsaky.androidide.actions.ActionData
import com.itsaky.androidide.actions.ActionItem
import com.itsaky.androidide.actions.ActionMenu
import com.itsaky.androidide.actions.EditorRelatedAction
import com.itsaky.androidide.actions.getEditor
import com.itsaky.androidide.actions.markInvisible

/**
 * An [ActionMenu] that consolidates all cursor movement operations for the text editor.
 * This action serves as a container for a sub-menu under the title "Cursor".
 */
class CursorActionsMenu(context: Context, override val order: Int) : EditorRelatedAction(), ActionMenu {


    override val id: String = "ide.editor.cursor.menu"
    
    override val children: MutableSet<ActionItem> = mutableSetOf()

    init {
        label = context.getString(R.string.action_group_cursor_title)
        // icon = ContextCompat.getDrawable(context, R.drawable.ic_cursor_move)
      var order = 0

        // Add all child cursor actions here, grouped logically
        // Basic Movement
        addAction(MoveUpAction(context, order++))
        addAction(MoveDownAction(context, order++))
        addAction(MoveLeftAction(context, order++))
        addAction(MoveRightAction(context, order++))

        // Line Boundaries
        addAction(GoToLineStartAction(context, order++))
        addAction(GoToLineEndAction(context, order++))

        // Word Boundaries
        addAction(GoToPreviousWordAction(context, order++))
        addAction(GoToNextWordAction(context, order++))
        
        // Page and Document Boundaries
        addAction(GoToPageUpAction(context, order++))
        addAction(GoToPageDownAction(context, order++))
        addAction(GoToDocumentStartAction(context, order++))
        addAction(GoToDocumentEndAction(context, order++))

        // Smart Navigation (The creative part)
        // addAction(GoToSymbolAction(context, 200))
    }

    override fun prepare(data: ActionData) {
        super<EditorRelatedAction>.prepare(data)
        super<ActionMenu>.prepare(data)

        if (data.getEditor() == null) {
            markInvisible()
        } else {
            visible = true
            enabled = true
        }
    }

    override suspend fun execAction(data: ActionData): Boolean {
        // This is a menu, so it doesn't perform an action itself.
        return true
    }
}