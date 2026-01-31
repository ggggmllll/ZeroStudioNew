// by android_zero
package com.itsaky.androidide.actions.menu

import android.content.Context
import androidx.core.content.ContextCompat
import com.itsaky.androidide.resources.R
import com.itsaky.androidide.actions.ActionData
import com.itsaky.androidide.actions.EditorRelatedAction

/**
 * 中文注释: 像软键盘删除键一样删除内容。如果有选区，则删除选区内容；如果没有选区，则删除光标前的一个字符。
 * English annotation: Delete content like the soft keyboard delete key. If there is a selection, delete the selection; otherwise, delete the character before the cursor.
 * @author android_zero
 */
class BackspaceContentAction(context: Context, override val order: Int) : EditorRelatedAction() {
    
    override val id: String = "ide.editor.content.delete"

    init {
        // 你可以在 strings.xml 中添加 string.delete_content 或者直接在这里硬编码测试
        // label = context.getString(R.string.delete_content) 
        label = context.getString(R.string.action_menu_edit_cursor_backspace)
        // 请替换为你想要的图标资源，例如 ic_backspace
        icon = ContextCompat.getDrawable(context, R.drawable.ic_edit_text_backspace) 
    }

    override suspend fun execAction(data: ActionData): Boolean {
        val editor = data.getEditor() ?: return false
        val context = data.get(Context::class.java) ?: return false
        return EditorLineOperations.deleteContentOrBackspace(editor, context)
    }
}