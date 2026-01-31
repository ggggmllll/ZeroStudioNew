package com.itsaky.androidide.actions.code.jumpsymbol

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.itsaky.androidide.resources.R
import com.itsaky.androidide.actions.ActionData
import com.itsaky.androidide.actions.EditorRelatedAction
import com.itsaky.androidide.projects.IProjectManager
import com.itsaky.androidide.projects.ModuleProject
import com.itsaky.androidide.tasks.launchAsyncWithProgress
import android.zero.studio.kotlin.analysis.symbolic.PsiSymbolResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * An action to parse the current file and display a bottom sheet with all symbols.
 *
 * @author android_zero
 */
class GoToSymbolAction(private val context: Context, override val order: Int) : EditorRelatedAction() {
    
    override val id: String = "ide.editor.cursor.go_to_symbol"

    init {
        label = context.getString(R.string.action_show_code_outline)
        icon = ContextCompat.getDrawable(context, R.drawable.ic_edit_code_outline_action)
    }

    override fun prepare(data: ActionData) {
        super.prepare(data)
        // Action should be available if editor exists
        val editor = data.getEditor()
        visible = editor != null
        enabled = visible
    }

    override suspend fun execAction(data: ActionData): Boolean {
        val editor = data.getEditor() ?: return false
        val activity = editor.context as? AppCompatActivity ?: return false
        val file = data.get(File::class.java) ?: return false

        val workspace = IProjectManager.getInstance().getWorkspace()
        val module = workspace?.findModuleForFile(file) as? ModuleProject
        val classpaths = module?.getCompileClasspaths() ?: emptySet()

        activity.lifecycleScope.launchAsyncWithProgress(
            configureFlashbar = { builder, _ ->
                builder.message(R.string.action_go_to_symbol_parsing)
            }
        ) { _, _ ->
            val code = editor.text.toString()
            
            val symbols = withContext(Dispatchers.IO) {
                PsiSymbolResolver.parseFileSymbols(file.name, code, classpaths)
            }

            withContext(Dispatchers.Main) {
                if (symbols.isNotEmpty()) {
                    val sheet = SymbolListBottomSheet(symbols) { symbol ->
                        // Navigate to symbol
                        editor.setSelection(symbol.line, 0) // Navigate to line start
                        editor.ensurePositionVisible(symbol.line, 0)
                    }
                    sheet.show(activity.supportFragmentManager, "SymbolListBottomSheet")
                } else {
                    com.itsaky.androidide.utils.flashInfo(context.getString(R.string.msg_no_symbols_found))
                }
            }
        }
        return true
    }
}