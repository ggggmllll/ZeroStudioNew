package com.itsaky.androidide.editor.language.treesitter

import android.content.Context
import com.itsaky.androidide.editor.language.treesitter.TreeSitterLanguage.Factory
import com.itsaky.androidide.treesitter.php.TSLanguagePhp
import io.github.rosemoe.sora.lang.Language.INTERRUPTION_LEVEL_SLIGHT
import io.github.rosemoe.sora.widget.SymbolPairMatch

/**
 * Tree Sitter language specification for PHP & PHP_ONLY.
 *
 * 采用单类多实例模式，动态桥接混合解析器 (php) 和纯代码解析器 (php_only)。
 *
 * @author android_zero
 */
open class PhpLanguage(
    context: Context,
    lang: com.itsaky.androidide.treesitter.TSLanguage,
    langType: String,
) : TreeSitterLanguage(context, lang, langType) {

  companion object {

    // 解析器类型标识
    const val TS_TYPE_PHP = "php"
    const val TS_TYPE_PHP_ONLY = "php_only"

    // 常见的 PHP 文件扩展名
    const val TS_EXT_PHP3 = "php3"
    const val TS_EXT_PHP4 = "php4"
    const val TS_EXT_PHP5 = "php5"
    const val TS_EXT_PHP7 = "php7"
    const val TS_EXT_PHP8 = "php8"
    const val TS_EXT_PHTML = "phtml"

    // 标准 PHP 混合解析工厂 (处理带有 <?php 的文件)
    @JvmField val FACTORY = Factory { PhpLanguage(it, TSLanguagePhp.getInstance(), TS_TYPE_PHP) }

    // 纯 PHP 代码解析工厂 (常用于代码片段注入，不需要 <?php 标签)
    @JvmField
    val PHP_ONLY_FACTORY = Factory {
      PhpLanguage(it, TSLanguagePhp.getPhpOnlyInstance(), TS_TYPE_PHP_ONLY)
    }
  }

  override fun getInterruptionLevel(): Int {
    return INTERRUPTION_LEVEL_SLIGHT
  }

  override fun getSymbolPairs(): SymbolPairMatch {
    return SymbolPairMatch().apply {
      putPair('(', SymbolPairMatch.SymbolPair("(", ")"))
      putPair('[', SymbolPairMatch.SymbolPair("[", "]"))
      putPair('{', SymbolPairMatch.SymbolPair("{", "}"))
      putPair('"', SymbolPairMatch.SymbolPair("\"", "\""))
      putPair('\'', SymbolPairMatch.SymbolPair("'", "'"))
    }
  }
}
