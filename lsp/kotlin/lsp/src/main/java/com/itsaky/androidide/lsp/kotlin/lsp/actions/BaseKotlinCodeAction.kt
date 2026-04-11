package com.itsaky.androidide.lsp.kotlin.lsp.actions

import android.content.Context
import android.graphics.drawable.Drawable
import com.itsaky.androidide.actions.ActionData
import com.itsaky.androidide.actions.ActionItem
import com.itsaky.androidide.actions.EditorActionItem
import com.itsaky.androidide.actions.hasRequiredData
import com.itsaky.androidide.actions.markInvisible
import com.itsaky.androidide.actions.requireFile
import com.itsaky.androidide.lsp.kotlin.lsp.KotlinLspServer
import com.itsaky.androidide.resources.R
import java.io.File

abstract class BaseKotlinCodeAction : EditorActionItem {

  override var visible: Boolean = true
  override var enabled: Boolean = true
  override var icon: Drawable? = null
  override var requiresUIThread: Boolean = false
  override var location: ActionItem.Location = ActionItem.Location.EDITOR_CODE_ACTIONS

  protected abstract val titleTextRes: Int

  override fun prepare(data: ActionData) {
    super.prepare(data)
    if (!data.hasRequiredData(Context::class.java, KotlinLspServer::class.java, File::class.java)) {
      markInvisible()
      return
    }

    if (titleTextRes != -1) {
      label = data[Context::class.java]!!.getString(titleTextRes)
    }

    val file = data.requireFile().toPath().toString()
    visible = file.endsWith(".kt") || file.endsWith(".kts") || file.endsWith(".java")
    enabled = visible
  }
}
