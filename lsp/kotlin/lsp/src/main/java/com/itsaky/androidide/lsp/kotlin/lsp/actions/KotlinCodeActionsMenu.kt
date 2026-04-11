package com.itsaky.androidide.lsp.kotlin.lsp.actions

import com.itsaky.androidide.actions.ActionItem
import com.itsaky.androidide.lsp.actions.IActionsMenuProvider
import com.itsaky.androidide.lsp.kotlin.lsp.actions.common.CommentAction
import com.itsaky.androidide.lsp.kotlin.lsp.actions.common.FindReferencesAction
import com.itsaky.androidide.lsp.kotlin.lsp.actions.common.GoToDefinitionAction
import com.itsaky.androidide.lsp.kotlin.lsp.actions.common.JavaToKotlinAction
import com.itsaky.androidide.lsp.kotlin.lsp.actions.common.RefreshKotlinClasspathAction
import com.itsaky.androidide.lsp.kotlin.lsp.actions.common.UncommentAction

object KotlinCodeActionsMenu : IActionsMenuProvider {
  override val actions: List<ActionItem> =
      listOf(
          CommentAction(),
          UncommentAction(),
          GoToDefinitionAction(),
          FindReferencesAction(),
          JavaToKotlinAction(),
          RefreshKotlinClasspathAction(),
      )
}
