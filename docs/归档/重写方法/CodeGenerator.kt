/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  AndroidIDE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */

package android.zero.studio.kotlin.analysis.symbolic

import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

/**
 * <strong>CodeGenerator</strong> 负责将编译器描述符转换为可插入编辑器的源代码字符串。
 *
 * 主要用于实现“覆盖方法 (Override Methods)”等代码自动生成功能。
 *
 * <h3>工作流程线路图</h3>
 * <pre>
 * [OverridableMembers 列表]
 *    |
 *    +--[迭代成员描述符]
 *    |    |
 *    |    +--{分支: FunctionDescriptor}
 *    |    |    |-- 提取函数名、参数名、参数类型、返回值类型
 *    |    |    |-- 构建 'override fun' 签名
 *    |    |    |-- 插入 super 调用 (处理 Unit 返回值差异)
 *    |    |
 *    |    +--{分支: PropertyDescriptor}
 *    |         |-- 识别 Val/Var 类型
 *    |         |-- 构建属性签名
 *    |         |-- 生成 Getter (及对应的 Setter)
 *    |
 * [拼接后的代码块]
 * </pre>
 *
 * @author android_zero
 * @updated 2025.10.28: 适配 kotlin-compiler-2.2.0 API，优化了函数参数生成逻辑及属性重写模板。
 */
object CodeGenerator {

  /**
   * 根据可重写成员列表生成 Kotlin 代码块。
   *
   * @param members 由 PsiSymbolResolver 提取的可重写成员列表。
   * @return 格式化后的 Kotlin 代码字符串。
   */
  @JvmStatic
  fun generateOverrideMethods(members: List<OverridableMember>): String {
    val sb = StringBuilder()
    for (member in members) {
      val descriptor = member.descriptor

      // 处理函数重写
      if (descriptor is FunctionDescriptor) {
        sb.append("    override fun ${descriptor.name.asString()}(")

        // 生成参数列表: name: Type
        val params =
            descriptor.valueParameters.joinToString(", ") { "${it.name.asString()}: ${it.type}" }
        sb.append(params)

        // 处理返回值类型
        val returnType = descriptor.returnType
        sb.append("): $returnType {\n")
        sb.append("        // TODO: Implement ${descriptor.name.asString()}\n")

        // 准备 super 调用的参数名列表
        val callParams = descriptor.valueParameters.joinToString(", ") { it.name.asString() }

        if (returnType?.toString() != "Unit") {
          // 非 Unit 类型需要 return
          sb.append("        return super.${descriptor.name.asString()}($callParams)\n")
        } else {
          // Unit 类型直接调用
          sb.append("        super.${descriptor.name.asString()}($callParams)\n")
        }
        sb.append("    }\n\n")
      }
      // 处理属性重写 (getter/setter)
      else if (descriptor is PropertyDescriptor) {
        val isVar = descriptor.isVar
        val keyword = if (isVar) "var" else "val"
        val name = descriptor.name.asString()
        val type = descriptor.type

        sb.append("    override $keyword $name: $type\n")
        sb.append("        get() = super.$name\n")

        if (isVar) {
          sb.append("        set(value) {\n")
          sb.append("            super.$name = value\n")
          sb.append("        }\n")
        }
        sb.append("\n")
      }
    }
    return sb.toString()
  }
}
