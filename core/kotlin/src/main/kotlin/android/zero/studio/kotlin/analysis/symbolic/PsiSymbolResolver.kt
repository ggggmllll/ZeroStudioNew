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
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.CliBindingTrace
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import java.io.File

/**
 * Data class for overridable members.
 * Contains [insertOffset] to avoid passing PSI elements across module boundaries.
 */
data class OverridableMember(
    val signature: String,
    val sourceClass: String,
    val descriptor: DeclarationDescriptor,
    val insertOffset: Int
)

/**
 * Resolves symbols from source code for navigation and generation purposes.
 *
 * @author android_zero
 */
object PsiSymbolResolver {

    private const val TAG = "PsiSymbolResolver"

    /**
     * Parses a file to extract a list of symbols for the "Go to Symbol" feature.
     */
    fun parseFileSymbols(
        fileName: String,
        code: String,
        classpaths: Set<File>
    ): List<SymbolInfo> {
        val disposable = Disposer.newDisposable()
        val symbols = mutableListOf<SymbolInfo>()

        try {
            val configuration = createConfiguration(classpaths)
            val environment = KotlinCoreEnvironment.createForProduction(
                disposable,
                configuration,
                EnvironmentConfigFiles.JVM_CONFIG_FILES
            )

            val project = environment.project
            val isKotlin = fileName.endsWith(".kt", ignoreCase = true) || fileName.endsWith(".kts", ignoreCase = true)

            if (isKotlin) {
                val virtualFile = LightVirtualFile(fileName, org.jetbrains.kotlin.idea.KotlinFileType.INSTANCE, code)
                val ktFile = PsiManager.getInstance(project).findFile(virtualFile) as? KtFile
                ktFile?.let { extractKotlinSymbols(it, code, symbols) }
            } else {
                val virtualFile = LightVirtualFile(fileName, JavaFileType.INSTANCE, code)
                val psiFile = PsiManager.getInstance(project).findFile(virtualFile) as? PsiJavaFile
                psiFile?.let { extractJavaSymbols(it, code, symbols) }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing symbols", e)
        } finally {
            Disposer.dispose(disposable)
        }

        return symbols.sortedBy { it.line }
    }

    /**
     * Finds overridable members for the "Override Methods" feature.
     */
    fun findOverridableMembers(
        fileName: String,
        code: String,
        cursorOffset: Int,
        classpaths: Set<File>
    ): List<OverridableMember> {
        val disposable = Disposer.newDisposable()
        try {
            val configuration = createConfiguration(classpaths)
            val environment = KotlinCoreEnvironment.createForProduction(
                disposable,
                configuration,
                EnvironmentConfigFiles.JVM_CONFIG_FILES
            )
            
            val project = environment.project
            val virtualFile = LightVirtualFile(fileName, org.jetbrains.kotlin.idea.KotlinFileType.INSTANCE, code)
            val ktFile = PsiManager.getInstance(project).findFile(virtualFile) as? KtFile ?: return emptyList()

            val analyzer = TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
                project = project,
                files = listOf(ktFile),
                trace = CliBindingTrace(project),
                configuration = configuration,
                packagePartProvider = { scope -> environment.createPackagePartProvider(scope) }
            )
            
            val bindingContext = analyzer.bindingContext
            val elementAtCursor = ktFile.findElementAt(cursorOffset) ?: return emptyList()
            val containingClass = PsiTreeUtil.getParentOfType(elementAtCursor, KtClassOrObject::class.java) ?: return emptyList()
            
            // Calculate insert offset: At the end of the class body, before the closing brace '}'
            val body = containingClass.body
            val insertOffset = body?.rBrace?.textOffset ?: (containingClass.textOffset + containingClass.textLength - 1)

            val classDescriptor = bindingContext[BindingContext.CLASS, containingClass] ?: return emptyList()

            val allMembers = mutableSetOf<String>()
            classDescriptor.unsubstitutedMemberScope.getContributedDescriptors().forEach { member ->
                if (member is CallableMemberDescriptor) {
                     allMembers.add(getSignature(member))
                }
            }
            
            val overridableMembers = mutableListOf<OverridableMember>()
            classDescriptor.typeConstructor.supertypes.forEach { supertype ->
                val superDescriptor = supertype.constructor.declarationDescriptor as? ClassDescriptor ?: return@forEach
                superDescriptor.unsubstitutedMemberScope.getContributedDescriptors().forEach { member ->
                     if (member is CallableMemberDescriptor && (member.modality == Modality.OPEN || member.modality == Modality.ABSTRACT)) {
                         if (member.visibility != DescriptorVisibilities.PRIVATE && member.kind != CallableMemberDescriptor.Kind.SYNTHESIZED) {
                            val signature = getSignature(member)
                             if (!allMembers.contains(signature)) {
                                 overridableMembers.add(OverridableMember(
                                    signature = signature,
                                    sourceClass = superDescriptor.fqNameSafe.asString(),
                                    descriptor = member,
                                    insertOffset = insertOffset
                                ))
                             }
                        }
                    }
                }
            }
            return overridableMembers.distinctBy { it.signature }.sortedBy { it.signature }

        } catch (e: Exception) {
            Log.e(TAG, "Error finding overridable members", e)
            return emptyList()
        } finally {
            Disposer.dispose(disposable)
        }
    }

    private fun createConfiguration(classpaths: Set<File>): CompilerConfiguration {
        return CompilerConfiguration().apply {
            put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, PrintingMessageCollector(System.err, MessageRenderer.PLAIN_FULL_PATHS, false))
            System.getProperty("java.home")?.let { put(JVMConfigurationKeys.JDK_HOME, File(it)) }
            addJvmClasspathRoots(classpaths.toList())
        }
    }

    // --- Extraction Logic for Go To Symbol ---

    private fun extractKotlinSymbols(ktFile: KtFile, code: String, symbols: MutableList<SymbolInfo>) {
        ktFile.accept(object : KtTreeVisitorVoid() {
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
                
                val kind = when {
                    classOrObject is KtObjectDeclaration -> "Object"
                    classOrObject is org.jetbrains.kotlin.psi.KtClass && classOrObject.isInterface() -> "Interface"
                    classOrObject is org.jetbrains.kotlin.psi.KtClass && classOrObject.isEnum() -> "Enum"
                    else -> "Class"
                }
                val icon = when (kind) {
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
                symbols.add(SymbolInfo(name, "Method", getKotlinFunctionSignature(function), line, R.drawable.ic_symbol_method))
                super.visitNamedFunction(function)
            }

            override fun visitProperty(property: KtProperty) {
                val name = property.name ?: "[Unnamed]"
                val line = getLineNumber(code, property.textOffset)
                symbols.add(SymbolInfo(name, "Property", getKotlinPropertySignature(property), line, R.drawable.ic_symbol_psymbol))
                super.visitProperty(property)
            }
        })
    }

    private fun extractJavaSymbols(psiFile: PsiJavaFile, code: String, symbols: MutableList<SymbolInfo>) {
        psiFile.accept(object : JavaRecursiveElementVisitor() {
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
                val kind = when {
                    aClass.isInterface -> "Interface"
                    aClass.isEnum -> "Enum"
                    else -> "Class"
                }
                val icon = when (kind) {
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
                symbols.add(SymbolInfo(name, "Method", getJavaMethodSignature(method), line, R.drawable.ic_symbol_method))
                super.visitMethod(method)
            }

            override fun visitField(field: PsiField) {
                val name = field.name
                val line = getLineNumber(code, field.textOffset)
                symbols.add(SymbolInfo(name, "Field", getJavaFieldSignature(field), line, R.drawable.ic_symbol_fsymbol))
                super.visitField(field)
            }
        })
    }

    // --- Helper Methods ---

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
        val params = ktClass.primaryConstructor?.valueParameters?.joinToString(", ") { 
             "${it.name}: ${it.typeReference?.text ?: "Any"}" 
        } ?: ""
        val type = if (params.isNotEmpty()) "($params)" else ""
        return "class $name$type"
    }

    private fun getKotlinFunctionSignature(function: KtNamedFunction): String {
        val params = function.valueParameters.joinToString(", ") { 
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
        val params = method.parameterList.parameters.joinToString(", ") { 
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
        if (offset >= text.length) return text.lines().size - 1
        var line = 0
        for (i in 0 until offset) {
            if (text[i] == '\n') {
                line++
            }
        }
        return line
    }
}

/**
 * Helper object to generate code for overriding methods.
 */
object CodeGenerator {
    fun generateOverrideMethods(members: List<OverridableMember>): String {
        val sb = StringBuilder()
        for (member in members) {
            val descriptor = member.descriptor
            if (descriptor is FunctionDescriptor) {
                sb.append("    override fun ${descriptor.name}(")
                val params = descriptor.valueParameters.joinToString(", ") { "${it.name}: ${it.type}" }
                sb.append(params)
                sb.append("): ${descriptor.returnType} {\n")
                sb.append("        // TODO: Implement ${descriptor.name}\n")
                if (descriptor.returnType?.toString() != "Unit") {
                     sb.append("        return super.${descriptor.name}(")
                     val callParams = descriptor.valueParameters.joinToString(", ") { it.name.asString() }
                     sb.append(callParams)
                     sb.append(")\n")
                } else {
                    sb.append("        super.${descriptor.name}(")
                    val callParams = descriptor.valueParameters.joinToString(", ") { it.name.asString() }
                    sb.append(callParams)
                    sb.append(")\n")
                }
                sb.append("    }\n\n")
            } else if (descriptor is PropertyDescriptor) {
                val keyword = if (descriptor.isVar) "var" else "val"
                sb.append("    override $keyword ${descriptor.name}: ${descriptor.type}\n")
                sb.append("        get() = super.${descriptor.name}\n")
                if (descriptor.isVar) {
                    sb.append("        set(value) { super.${descriptor.name} = value }\n")
                }
                sb.append("\n")
            }
        }
        return sb.toString()
    }
}