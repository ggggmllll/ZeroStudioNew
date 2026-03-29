package ru.zdevs.intellij.c.lang

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon
import ru.zdevs.intellij.c.Icons

class ClangdFileType : LanguageFileType(ClangdLanguage.INSTANCE) {

  override fun getName(): String = "clangd"

  override fun getDescription(): String = "Clangd file"

  override fun getDefaultExtension(): String = "c"

  override fun getIcon(): Icon = Icons.C

  companion object {
    val INSTANCE = ClangdFileType()
  }
}
