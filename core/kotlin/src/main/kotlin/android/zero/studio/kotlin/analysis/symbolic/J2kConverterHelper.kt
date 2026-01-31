package android.zero.studio.kotlin.analysis.symbolic

import android.util.Log
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.LightVirtualFile
import com.itsaky.androidide.utils.Environment
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.j2k.ConverterSettings
import org.jetbrains.kotlin.j2k.EmptyJavaToKotlinServices
import org.jetbrains.kotlin.j2k.OldJavaToKotlinConverter
import org.jetbrains.kotlin.idea.j2k.J2kPostProcessor 
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helper object to handle Java to Kotlin conversion.
 * Integrates file backup and environment setup.
 *
 * @author android_zero
 */
object J2kConverterHelper {

    private const val TAG = "J2kConverterHelper"

    data class ConversionResult(
        val kotlinCode: String?,
        val backupPath: String?
    )

    /**
     * Converts a Java source code string to Kotlin.
     */
    fun convert(javaFile: File, javaCode: String, classpaths: Set<File>): ConversionResult {
        // 1. Backup
        val backupPath = backupFile(javaFile)
        
        val disposable = Disposer.newDisposable()
        try {
            // 2. Configure Environment
            val configuration = CompilerConfiguration().apply {
                put(
                    CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
                    PrintingMessageCollector(System.err, MessageRenderer.PLAIN_FULL_PATHS, false)
                )
                addJvmClasspathRoots(classpaths.toList())
                if (Environment.ANDROID_JAR != null && Environment.ANDROID_JAR.exists()) {
                    addJvmClasspathRoots(listOf(Environment.ANDROID_JAR))
                }
                System.getProperty("java.home")?.let { put(JVMConfigurationKeys.JDK_HOME, File(it)) }
            }

            val environment = KotlinCoreEnvironment.createForProduction(
                disposable,
                configuration,
                EnvironmentConfigFiles.JVM_CONFIG_FILES
            )
            val project = environment.project

            // 3. Create PSI File
            val virtualFile = LightVirtualFile(javaFile.name, com.intellij.ide.highlighter.JavaFileType.INSTANCE, javaCode)
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile) as? PsiJavaFile

            if (psiFile == null) {
                Log.e(TAG, "Failed to parse Java PSI file.")
                return ConversionResult(null, backupPath)
            }

            // 4. Run J2K Converter
            val settings = ConverterSettings.defaultSettings
            val converter = OldJavaToKotlinConverter(project, settings, EmptyJavaToKotlinServices)
            
            val postProcessor = J2kPostProcessor(formatCode = false) 
            val progressIndicator = EmptyProgressIndicator()

            val filesResult = converter.filesToKotlin(
                files = listOf(psiFile),
                postProcessor = postProcessor,
                progress = progressIndicator
            )

            val convertedText = filesResult.results.firstOrNull()
            
            return ConversionResult(convertedText, backupPath)

        } catch (e: Exception) {
            Log.e(TAG, "J2K Conversion failed", e)
            return ConversionResult(null, backupPath)
        } finally {
            Disposer.dispose(disposable)
        }
    }

    /**
     * Backs up the given file.
     * Uses a local logic to avoid dependency on specific Environment fields if they differ.
     */
    private fun backupFile(file: File): String? {
        try {
            // Use the parent directory's .backup folder or similar
            val backupDir = File(file.parentFile, ".backup")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupName = "${file.nameWithoutExtension}_$timestamp.java.bak"
            val backupFile = File(backupDir, backupName)

            file.copyTo(backupFile, overwrite = true)
            return backupFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to backup file: ${file.absolutePath}", e)
            return null
        }
    }
}