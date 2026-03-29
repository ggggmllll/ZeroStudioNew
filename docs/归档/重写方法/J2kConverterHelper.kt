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
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.LightVirtualFile
import com.itsaky.androidide.utils.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.idea.j2k.J2kPostProcessor
import org.jetbrains.kotlin.j2k.ConverterSettings
import org.jetbrains.kotlin.j2k.EmptyJavaToKotlinServices
import org.jetbrains.kotlin.j2k.OldJavaToKotlinConverter

/**
 * <strong>J2kConverterHelper</strong> 专门用于处理 Java 源代码到 Kotlin 源代码的自动化转换。
 *
 * 基于 <b>kotlin-compiler-2.2.0</b> 核心库实现，集成了复杂的 PSI 解析环境初始化与转译逻辑。
 *
 * <h3>核心功能与工作流</h3>
 * <pre>
 * 1. [Pre-Flight Check] -> 备份原文件、注入系统属性以规避 Android 兼容性问题。
 * 2. [Env Setup] -> 创建 CompilerConfiguration，注入类路径及 CommonConfiguration 键。
 * 3. [Core Init] -> 初始化 KotlinCoreEnvironment (IntelliJ 平台基础)。
 * 4. [PSI Load] -> 构造虚拟文件系统 (VFS)，将源码解析为抽象语法树 (PSI)。
 * 5. [Convert] -> 调用 OldJavaToKotlinConverter 执行核心转译算法。
 * 6. [Post-Process] -> 清理生成的冗余符号。
 * 7. [Cleanup] -> 显式销毁环境 Disposable 句柄。
 * </pre>
 *
 * @author android_zero
 * @updated 2025.10.28: 针对 kotlin-compiler-2.2.0 全面重做，解决了 Android 14/15 上的 Unsafe.copyMemory 崩溃难题。
 */
object J2kConverterHelper {

  private const val TAG = "J2kConverterHelper"

  /**
   * 转译操作的结果封装。
   *
   * @property kotlinCode 成功时返回转换后的代码，失败则为 null。
   * @property backupPath 本次转换自动创建的备份文件路径。
   */
  data class ConversionResult(val kotlinCode: String?, val backupPath: String?)

  /**
   * 将 Java 源代码字符串转换为 Kotlin。
   *
   * @param javaFile 对应的物理 Java 文件，用于备份和上下文识别。
   * @param javaCode 待转换的源码。
   * @param classpaths 编译该文件所需的依赖库路径集合。
   * @return [ConversionResult] 转换状态及结果。
   */
  @JvmStatic
  fun convert(javaFile: File, javaCode: String, classpaths: Set<File>): ConversionResult {
    // 1. 自动执行文件备份以防万一
    val backupPath = backupFile(javaFile)

    // 创建 Disposable。在 IntelliJ 平台架构中，环境必须依附于 Disposable。
    val disposable = Disposer.newDisposable("AndroidIDE_J2K_Context")

    try {
      // 2. 关键：修正环境冲突。
      // Android 虚拟机的 sun.misc.Unsafe 实现不完整，缺少 copyMemory 等方法。
      // 必须在初始化 KotlinCoreEnvironment 之前屏蔽相关组件的 Unsafe 优化。
      System.setProperty("idea.io.use.nio2", "false")
      System.setProperty("io.netty.noUnsafe", "true")

      // 3. 编译器配置初始化 (针对 2.2.0 优化)
      val configuration =
          CompilerConfiguration().apply {
            // 使用 CommonConfigurationKeys 替代已弃用的 CLI 键
            put(
                CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY,
                PrintingMessageCollector(System.err, MessageRenderer.PLAIN_FULL_PATHS, false),
            )

            // 加载用户项目类路径
            addJvmClasspathRoots(classpaths.toList())

            // 加载 Android SDK 环境支持
            if (Environment.ANDROID_JAR != null && Environment.ANDROID_JAR.exists()) {
              addJvmClasspathRoots(listOf(Environment.ANDROID_JAR))
            }

            // 明确 JDK 根目录，保证核心类库解析准确
            System.getProperty("java.home")?.let { put(JVMConfigurationKeys.JDK_HOME, File(it)) }
          }

      // 4. 创建 Kotlin 核心编译环境
      // 警告：如果在此处发生 NoSuchMethodException (Unsafe)，会被下方的 Error 捕获块处理
      val environment =
          KotlinCoreEnvironment.createForProduction(
              disposable,
              configuration,
              EnvironmentConfigFiles.JVM_CONFIG_FILES,
          )
      val project = environment.project

      // 5. 将文本内容载入内存中的虚拟文件
      val virtualFile = LightVirtualFile(javaFile.name, JavaFileType.INSTANCE, javaCode)
      val psiFile = PsiManager.getInstance(project).findFile(virtualFile) as? PsiJavaFile

      if (psiFile == null) {
        Log.e(TAG, "PSI 解析失败: 无法构建 Java 文件模型。")
        return ConversionResult(null, backupPath)
      }

      // 6. 调用 J2K 转换引擎
      val settings = ConverterSettings.defaultSettings
      // 适配 2.2.0 的 OldJavaToKotlinConverter 调用规范
      val converter = OldJavaToKotlinConverter(project, settings, EmptyJavaToKotlinServices)

      // 后处理器配置：不开启自动格式化，以减少 UI 线程阻塞并提高转换兼容性
      val postProcessor = J2kPostProcessor(formatCode = false)
      val progressIndicator = EmptyProgressIndicator()

      val filesResult =
          converter.filesToKotlin(
              files = listOf(psiFile),
              postProcessor = postProcessor,
              progress = progressIndicator,
          )

      val convertedText = filesResult.results.firstOrNull()

      return ConversionResult(convertedText, backupPath)
    } catch (e: Exception) {
      Log.e(TAG, "J2K 转换逻辑异常: ${e.message}", e)
      return ConversionResult(null, backupPath)
    } catch (err: Error) {
      // 重要：捕获 NoSuchMethodError 或 StaticInitializerError
      // 这些通常是由于 IntelliJ 的 Unsafe 容器在 Android 上初始化失败导致的。
      Log.e(TAG, "底层环境致命错误 (Unsafe/IntelliJ 兼容性问题): ${err.message}", err)
      return ConversionResult(null, backupPath)
    } finally {
      // 8. 彻底释放环境。2.2.0 环境如果不 Dispose，会造成内存泄露及大量线程挂起。
      Disposer.dispose(disposable)
    }
  }

  /**
   * 备份文件到本地隐藏目录。
   *
   * @param file 原始文件。
   * @return 备份文件的绝对路径，若失败则返回 null。
   */
  private fun backupFile(file: File): String? {
    return try {
      val backupDir = File(file.parentFile, ".backup")
      if (!backupDir.exists()) {
        backupDir.mkdirs()
      }

      val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
      val backupName = "${file.nameWithoutExtension}_$timestamp.java.bak"
      val backupFile = File(backupDir, backupName)

      file.copyTo(backupFile, overwrite = true)
      backupFile.absolutePath
    } catch (e: Exception) {
      Log.e(TAG, "创建备份失败: ${file.absolutePath}", e)
      null
    }
  }
}
