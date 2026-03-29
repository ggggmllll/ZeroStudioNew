package android.zero.studio.symbol

import androidx.annotation.DrawableRes
import com.itsaky.androidide.resources.R

/**
 * 表示代码中的一个符号（类、方法、变量、导入等）
 *
 * @author android_zero
 */
data class SymbolInfo(
    val name: String, // 符号名称 (例如: onCreate, MyClass)
    val signature: String, // 签名详情
    val line: Int, // 行号 (0-based)
    val type: SymbolType, // 符号类型
    val indentLevel: Int = 0, // 缩进级别
    val language: String = "java", // 编程语言 (小写扩展名，如 java, kt, xml, json)
) {
  @get:DrawableRes
  val iconRes: Int
    get() =
        when (language.lowercase()) {
          "xml" -> getXmlIcon()
          "json" -> getJsonIcon()
          else -> getGeneralIcon() // Java, Kotlin, C++, etc.
        }

  /** 通用编程语言图标 (Java, Kotlin, C++, Groovy 等) */
  private fun getGeneralIcon(): Int =
      when (type) {
        // 类 -> C (ic_symbol_csymbol)
        SymbolType.CLASS -> R.drawable.ic_symbol_csymbol

        // 接口 -> I (ic_symbol_isymbol)
        SymbolType.INTERFACE -> R.drawable.ic_symbol_isymbol

        // 枚举 -> E (ic_symbol_esymbol)
        SymbolType.ENUM -> R.drawable.ic_symbol_esymbol

        // 方法/函数/构造器 -> Method图标 (ic_symbol_method)
        SymbolType.METHOD,
        SymbolType.FUNCTION,
        SymbolType.CONSTRUCTOR -> R.drawable.ic_symbol_method

        // 字段 -> F (ic_symbol_fsymbol)
        SymbolType.FIELD -> R.drawable.ic_symbol_fsymbol

        // 变量/属性 -> P (ic_symbol_psymbol - Property)
        SymbolType.VARIABLE -> R.drawable.ic_symbol_psymbol

        // 导入语句 -> ic_files_import
        SymbolType.IMPORT -> R.drawable.ic_files_import

        // 包声明 -> ic_package
        SymbolType.PACKAGE -> R.drawable.ic_package

        // 结构体 (C/C++) -> S (ic_symbol_ssymbol)
        // 注意：如果你要在 SymbolType 添加 STRUCT 类型，这里可以匹配
        SymbolType.UNKNOWN -> R.drawable.ic_symbol_unknown
      }

  /** XML 专用图标 */
  private fun getXmlIcon(): Int =
      when (type) {
        // XML 标签 -> T (ic_symbol_tsymbol - Tag)
        SymbolType.CLASS -> R.drawable.ic_symbol_tsymbol

        // XML 属性 -> A (ic_symbol_asymbol - Attribute)
        SymbolType.FIELD,
        SymbolType.VARIABLE -> R.drawable.ic_symbol_asymbol

        // 命名空间/Schema -> ic_package
        SymbolType.PACKAGE -> R.drawable.ic_package

        else -> R.drawable.ic_file_type_xml
      }

  /** JSON 专用图标 */
  private fun getJsonIcon(): Int =
      when (type) {
        // JSON 对象 -> O (ic_symbol_osymbol - Object)
        SymbolType.CLASS -> R.drawable.ic_symbol_osymbol

        // JSON 数组 -> A (ic_symbol_asymbol - Array)
        SymbolType.INTERFACE -> R.drawable.ic_symbol_asymbol

        // JSON Key -> K (ic_symbol_ksymbol - Key)
        SymbolType.FIELD,
        SymbolType.VARIABLE -> R.drawable.ic_symbol_ksymbol

        else -> R.drawable.ic_file_type_json
      }

  val typeLetter: String
    get() =
        when (type) {
          SymbolType.CLASS -> "C"
          SymbolType.INTERFACE -> "I"
          SymbolType.METHOD,
          SymbolType.FUNCTION -> "M"
          SymbolType.FIELD -> "F"
          SymbolType.VARIABLE -> "V"
          SymbolType.IMPORT -> "Imp"
          SymbolType.PACKAGE -> "P"
          SymbolType.ENUM -> "E"
          SymbolType.CONSTRUCTOR -> "C"
          SymbolType.UNKNOWN -> "?"
        }
}

enum class SymbolType {
  CLASS,
  INTERFACE,
  METHOD,
  FUNCTION,
  FIELD,
  VARIABLE,
  IMPORT,
  PACKAGE,
  ENUM,
  CONSTRUCTOR,
  UNKNOWN,
}
