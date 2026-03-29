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

import android.util.Log
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.util.Disposer
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiPackageStatement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.LightVirtualFile
import com.itsaky.androidide.resources.R
import com.itsaky.androidide.utils.Environment
import java.io.File
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.CliBindingTrace
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

/**
 * 数据类：表示可重写的成员。 用于将编译器内部的 Descriptor 转换为 IDE UI 可用的数据模型。
 *
 * @property signature 成员签名字符串。
 * @property sourceClass 来源类名。
 * @property descriptor 原始声明描述符。
 * @property insertOffset 建议的代码插入位置偏移量。
 */
data class OverridableMember(
    val signature: String,
    val sourceClass: String,
    val descriptor: DeclarationDescriptor,
    val insertOffset: Int,
)

/**
 * <strong>PsiSymbolResolver</strong> 负责从源代码（Java/Kotlin）中提取符号信息并分析成员继承关系。
 *
 * 该类是“跳转到符号”和“重写方法”功能的核心引擎。基于 <b>kotlin-compiler-2.2.0</b>。
 *
 * <h3>工作流程线路图</h3>
 * <pre>
 * [输入源码]
 *    |
 *    +--[环境初始化]--> 注入 Unsafe 兼容属性 -> 创建 KotlinCoreEnvironment (IntelliJ Project)
 *    |
 *    +--[文件载入]--> 创建 LightVirtualFile -> 映射为 PSI 树 (KtFile/PsiJavaFile)
 *    |
 *    +--[逻辑分支]
 *         |
 *         +--[符号提取]--> 遍历 PSI 树 -> 收集 Package/Class/Method/Property -> 转换为 SymbolInfo
 *         |
 *         +--[重写分析]--> 调用 TopDownAnalyzer -> 建立解析上下文 (BindingContext) ->
 *                         获取类描述符 -> 递归父类 Scope -> 过滤可重写成员
 * [输出结果]
 * </pre>
 *
 * @author android_zero
 * @updated 2025.10.28: 适配 kotlin-compiler-2.2.0，修复了在 Android 平台上的 Unsafe 环境初始化崩溃。
 */
object PsiSymbolResolver {

  private const val TAG = "PsiSymbolResolver"

  /**
   * 解析给定源码文件并提取其中的符号列表。
   *
   * @param fileName 文件名（带扩展名）。
   * @param code 源码文本。
   * @param classpaths 编译所需的依赖路径。
   * @return 排序后的 [SymbolInfo] 列表。
   */
  @JvmStatic
  fun parseFileSymbols(fileName: String, code: String, classpaths: Set<File>): List<SymbolInfo> {
    val disposable = Disposer.newDisposable("SymbolResolver_ParseFile")
    val symbols = mutableListOf<SymbolInfo>()

    try {
      // 设置底层环境属性，防止 Unsafe 方法缺失崩溃
      patchEnvironmentProperties()

      val configuration = createConfiguration(classpaths)
      val environment =
          KotlinCoreEnvironment.createForProduction(
              disposable,
              configuration,
              EnvironmentConfigFiles.JVM_CONFIG_FILES,
          )

      val project = environment.project
      val isKotlin =
          fileName.endsWith(".kt", ignoreCase = true) ||
              fileName.endsWith(".kts", ignoreCase = true)

      if (isKotlin) {
        val virtualFile = LightVirtualFile(fileName, KotlinFileType.INSTANCE, code)
        val ktFile = PsiManager.getInstance(project).findFile(virtualFile) as? KtFile
        ktFile?.let { extractKotlinSymbols(it, code, symbols) }
      } else {
        val virtualFile = LightVirtualFile(fileName, JavaFileType.INSTANCE, code)
        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) as? PsiJavaFile
        psiFile?.let { extractJavaSymbols(it, code, symbols) }
      }
    } catch (e: Exception) {
      Log.e(TAG, "解析符号时发生异常: ${e.message}", e)
    } catch (err: Error) {
      Log.e(TAG, "解析符号时发生底层错误 (Unsafe/JVM): ${err.message}", err)
    } finally {
      Disposer.dispose(disposable)
    }

    return symbols.sortedBy { it.line }
  }

  /**
   * 查找当前光标位置所属类中可以被重写的成员。
   *
   * @param fileName 文件名。
   * @param code 源码文本。
   * @param cursorOffset 当前光标在文本中的偏移量。
   * @param classpaths 依赖路径。
   * @return [OverridableMember] 列表。
   */
  @JvmStatic
  fun findOverridableMembers(
      fileName: String,
      code: String,
      cursorOffset: Int,
      classpaths: Set<File>,
  ): List<OverridableMember> {
    val disposable = Disposer.newDisposable("SymbolResolver_FindOverride")
    try {
      patchEnvironmentProperties()

      val configuration = createConfiguration(classpaths)
      val environment =
          KotlinCoreEnvironment.createForProduction(
              disposable,
              configuration,
              EnvironmentConfigFiles.JVM_CONFIG_FILES,
          )

      val project = environment.project
      val virtualFile = LightVirtualFile(fileName, KotlinFileType.INSTANCE, code)
      val ktFile =
          PsiManager.getInstance(project).findFile(virtualFile) as? KtFile ?: return emptyList()

      // 执行全量符号分析以获取描述符
      val analyzer =
          TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
              project = project,
              files = listOf(ktFile),
              trace = CliBindingTrace(project),
              configuration = configuration,
              packagePartProvider = { scope -> environment.createPackagePartProvider(scope) },
          )

      val bindingContext = analyzer.bindingContext
      val elementAtCursor = ktFile.findElementAt(cursorOffset) ?: return emptyList()
      val containingClass =
          PsiTreeUtil.getParentOfType(elementAtCursor, KtClassOrObject::class.java)
              ?: return emptyList()

      // 计算插入点：大括号起始处
      val body = containingClass.body
      val insertOffset =
          body?.lBrace?.textOffset?.let { it + 1 }
              ?: (containingClass.textOffset + containingClass.textLength - 1)

      val classDescriptor =
          bindingContext[BindingContext.CLASS, containingClass] ?: return emptyList()

      // 记录当前类已有的成员签名，避免重复重写
      val currentMemberSignatures = mutableSetOf<String>()
      classDescriptor.unsubstitutedMemberScope.getContributedDescriptors().forEach { member ->
        if (member is CallableMemberDescriptor) {
          currentMemberSignatures.add(getSignature(member))
        }
      }

      val overridableMembers = mutableListOf<OverridableMember>()
      // 递归分析父类及接口
      classDescriptor.typeConstructor.supertypes.forEach { supertype ->
        val superDescriptor =
            supertype.constructor.declarationDescriptor as? ClassDescriptor ?: return@forEach
        superDescriptor.unsubstitutedMemberScope.getContributedDescriptors().forEach { member ->
          if (
              member is CallableMemberDescriptor &&
                  (member.modality == Modality.OPEN || member.modality == Modality.ABSTRACT)
          ) {
            if (
                member.visibility != DescriptorVisibilities.PRIVATE &&
                    member.kind != CallableMemberDescriptor.Kind.SYNTHESIZED
            ) {
              val signature = getSignature(member)
              if (!currentMemberSignatures.contains(signature)) {
                overridableMembers.add(
                    OverridableMember(
                        signature = signature,
                        sourceClass = superDescriptor.fqNameSafe.asString(),
                        descriptor = member,
                        insertOffset = insertOffset,
                    )
                )
              }
            }
          }
        }
      }
      return overridableMembers.distinctBy { it.signature }.sortedBy { it.signature }
    } catch (e: Exception) {
      Log.e(TAG, "查找可重写成员时发生异常: ${e.message}", e)
      return emptyList()
    } catch (err: Error) {
      Log.e(TAG, "查找可重写成员时发生底层错误: ${err.message}", err)
      return emptyList()
    } finally {
      Disposer.dispose(disposable)
    }
  }

  /** 针对 Android 平台修正在加载 IntelliJ 组件前需要的系统属性。 */
  private fun patchEnvironmentProperties() {
    System.setProperty("idea.io.use.nio2", "false")
    System.setProperty("io.netty.noUnsafe", "true")
  }

  private fun createConfiguration(classpaths: Set<File>): CompilerConfiguration {
    return CompilerConfiguration().apply {
      // 适配 2.2.0：使用 CommonConfigurationKeys
      put(
          CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY,
          PrintingMessageCollector(System.err, MessageRenderer.PLAIN_FULL_PATHS, false),
      )

      // 注入 Android.jar
      if (Environment.ANDROID_JAR != null && Environment.ANDROID_JAR.exists()) {
        addJvmClasspathRoots(listOf(Environment.ANDROID_JAR))
      }

      System.getProperty("java.home")?.let { put(JVMConfigurationKeys.JDK_HOME, File(it)) }
      addJvmClasspathRoots(classpaths.toList())
    }
  }

  private fun extractKotlinSymbols(ktFile: KtFile, code: String, symbols: MutableList<SymbolInfo>) {
    ktFile.accept(
        object : KtTreeVisitorVoid() {
          override fun visitPackageDirective(directive: KtPackageDirective) {
            val name = directive.qualifiedName
            if (name.isNotEmpty()) {
              val line = getLineNumber(code, directive.textOffset)
              symbols.add(SymbolInfo(name, "Package", "package $name", line, R.drawable.ic_package))
            }
            super.visitPackageDirective(directive)
          }

          override fun visitClassOrObject(classOrObject: KtClassOrObject) {
            val name = classOrObject.name ?: "[Anonymous]"
            val line = getLineNumber(code, classOrObject.textOffset)

            val kind =
                when {
                  classOrObject is org.jetbrains.kotlin.psi.KtObjectDeclaration -> "Object"
                  classOrObject is org.jetbrains.kotlin.psi.KtClass &&
                      classOrObject.isInterface() -> "Interface"
                  classOrObject is org.jetbrains.kotlin.psi.KtClass && classOrObject.isEnum() ->
                      "Enum"
                  else -> "Class"
                }
            val icon =
                when (kind) {
                  "Interface" -> R.drawable.ic_symbol_isymbol
                  "Enum" -> R.drawable.ic_symbol_esymbol
                  "Object" -> R.drawable.ic_symbol_osymbol
                  else -> R.drawable.ic_symbol_csymbol
                }
            symbols.add(SymbolInfo(name, kind, getKotlinClassSignature(classOrObject), line, icon))
            super.visitClassOrObject(classOrObject)
          }

          override fun visitNamedFunction(function: KtNamedFunction) {
            val name = function.name ?: "[Anonymous]"
            val line = getLineNumber(code, function.textOffset)
            symbols.add(
                SymbolInfo(
                    name,
                    "Method",
                    getKotlinFunctionSignature(function),
                    line,
                    R.drawable.ic_symbol_method,
                )
            )
            super.visitNamedFunction(function)
          }

          override fun visitProperty(property: KtProperty) {
            val name = property.name ?: "[Unnamed]"
            val line = getLineNumber(code, property.textOffset)
            symbols.add(
                SymbolInfo(
                    name,
                    "Property",
                    getKotlinPropertySignature(property),
                    line,
                    R.drawable.ic_symbol_psymbol,
                )
            )
            super.visitProperty(property)
          }
        }
    )
  }

  private fun extractJavaSymbols(
      psiFile: PsiJavaFile,
      code: String,
      symbols: MutableList<SymbolInfo>,
  ) {
    psiFile.accept(
        object : JavaRecursiveElementVisitor() {
          override fun visitPackageStatement(statement: PsiPackageStatement) {
            val name = statement.packageName
            if (!name.isNullOrEmpty()) {
              val line = getLineNumber(code, statement.textOffset)
              symbols.add(SymbolInfo(name, "Package", "package $name", line, R.drawable.ic_package))
            }
            super.visitPackageStatement(statement)
          }

          override fun visitClass(aClass: PsiClass) {
            val name = aClass.name ?: "[Anonymous]"
            val line = getLineNumber(code, aClass.textOffset)
            val kind =
                when {
                  aClass.isInterface -> "Interface"
                  aClass.isEnum -> "Enum"
                  else -> "Class"
                }
            val icon =
                when (kind) {
                  "Interface" -> R.drawable.ic_symbol_isymbol
                  "Enum" -> R.drawable.ic_symbol_esymbol
                  else -> R.drawable.ic_symbol_csymbol
                }
            symbols.add(SymbolInfo(name, kind, getJavaClassSignature(aClass), line, icon))
            super.visitClass(aClass)
          }

          override fun visitMethod(method: PsiMethod) {
            val name = method.name
            val line = getLineNumber(code, method.textOffset)
            symbols.add(
                SymbolInfo(
                    name,
                    "Method",
                    getJavaMethodSignature(method),
                    line,
                    R.drawable.ic_symbol_method,
                )
            )
            super.visitMethod(method)
          }

          override fun visitField(field: PsiField) {
            val name = field.name
            val line = getLineNumber(code, field.textOffset)
            symbols.add(
                SymbolInfo(
                    name,
                    "Field",
                    getJavaFieldSignature(field),
                    line,
                    R.drawable.ic_symbol_fsymbol,
                )
            )
            super.visitField(field)
          }
        }
    )
  }

  // --- 签名辅助方法 ---

  private fun getSignature(descriptor: DeclarationDescriptor): String {
    return when (descriptor) {
      is FunctionDescriptor -> {
        val params = descriptor.valueParameters.joinToString(", ") { "${it.name}: ${it.type}" }
        "${descriptor.name.asString()}($params): ${descriptor.returnType}"
      }
      is PropertyDescriptor -> {
        val keyword = if (descriptor.isVar) "var" else "val"
        "$keyword ${descriptor.name.asString()}: ${descriptor.type}"
      }
      else -> descriptor.name.asString()
    }
  }

  private fun getKotlinClassSignature(ktClass: KtClassOrObject): String {
    val name = ktClass.name ?: ""
    return "class $name"
  }

  private fun getKotlinFunctionSignature(function: KtNamedFunction): String {
    val params =
        function.valueParameters.joinToString(", ") {
          "${it.name}: ${it.typeReference?.text ?: "Any"}"
        }
    val returnType = function.typeReference?.text?.let { ": $it" } ?: ""
    return "fun ${function.name}($params)$returnType"
  }

  private fun getKotlinPropertySignature(property: KtProperty): String {
    val keyword = if (property.isVar) "var" else "val"
    val type = property.typeReference?.text?.let { ": $it" } ?: ""
    return "$keyword ${property.name}$type"
  }

  private fun getJavaClassSignature(psiClass: PsiClass): String {
    return psiClass.name ?: ""
  }

  private fun getJavaMethodSignature(method: PsiMethod): String {
    val params =
        method.parameterList.parameters.joinToString(", ") {
          "${it.type.presentableText} ${it.name}"
        }
    val returnType = method.returnType?.presentableText ?: "void"
    return "$returnType ${method.name}($params)"
  }

  private fun getJavaFieldSignature(field: PsiField): String {
    return "${field.type.presentableText} ${field.name}"
  }

  private fun getLineNumber(text: String, offset: Int): Int {
    if (offset < 0) return 0
    val effectiveOffset = offset.coerceAtMost(text.length)
    var line = 0
    for (i in 0 until effectiveOffset) {
      if (text[i] == '\n') {
        line++
      }
    }
    return line
  }
}
