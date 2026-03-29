/*
 *  This file is part of AndroidIDE.
 *  @author android_zero
 */

package com.itsaky.androidide.editor.language.treesitter

import android.content.Context
import com.itsaky.androidide.editor.language.newline.TSBracketsHandler
import com.itsaky.androidide.editor.language.newline.TSCStyleBracketsHandler
import com.itsaky.androidide.treesitter.markdown.TSLanguageMarkdown
import io.github.rosemoe.sora.lang.Language.INTERRUPTION_LEVEL_SLIGHT
import io.github.rosemoe.sora.util.MyCharacter

/**
 * Tree Sitter language specification for Markdown. Supports both Block (Structure) and Inline
 * (Styling) parsing.
 *
 * @author android_zero
 */
class MarkdownLanguage(
    context: Context,
    tsLanguage: com.itsaky.androidide.treesitter.TSLanguage,
    languageId: String,
) : TreeSitterLanguage(context, tsLanguage, languageId) {

  companion object {
    const val TS_TYPE_MD = "md"

    const val TS_TYPE_INLINE = "markdown_inline"

    const val EXT_MARKDOWN = "markdown"
    const val EXT_MKD = "mkd"
    const val EXT_MKDN = "mkdn"
    const val EXT_MDOWN = "mdown"
    const val EXT_MDWN = "mdwn"
    const val EXT_MDTXT = "mdtxt"

    /** [FACTORY_BLOCK] 用于解析 Markdown 的整体结构（标题、代码块、列表等）。 这是用户打开 .md 文件时使用的主解析器。 */
    @JvmField
    val FACTORY_BLOCK = Factory { context ->
      MarkdownLanguage(context, TSLanguageMarkdown.getInstance(), EXT_MARKDOWN)
    }

    /**
     * [FACTORY_INLINE] 用于解析 Markdown 的行内元素（加粗、斜体、链接等）。 这个解析器通常不直接用于打开文件，而是通过 injections.scm 被注入到
     * Block 解析器中。
     */
    @JvmField
    val FACTORY_INLINE = Factory { context ->
      MarkdownLanguage(context, TSLanguageMarkdown.getInlineInstance(), TS_TYPE_INLINE)
    }
  }

  override fun checkIsCompletionChar(c: Char): Boolean {
    // 允许字母、数字、点，以及 Markdown 特殊符号触发补全
    return MyCharacter.isJavaIdentifierPart(c) ||
        c == '#' ||
        c == '-' ||
        c == '[' ||
        c == '(' ||
        c == ':'
  }

  override fun getInterruptionLevel(): Int {
    return INTERRUPTION_LEVEL_SLIGHT
  }

  override fun createNewlineHandlers(): Array<TSBracketsHandler> {
    return arrayOf(TSCStyleBracketsHandler(this))
  }
}
