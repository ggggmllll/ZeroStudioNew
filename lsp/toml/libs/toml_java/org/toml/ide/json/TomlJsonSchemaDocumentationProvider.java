package org.toml.ide.json;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.jsonSchema.ide.JsonSchemaService;
import com.jetbrains.jsonSchema.impl.JsonSchemaDocumentationProvider;
import com.jetbrains.jsonSchema.impl.JsonSchemaObject;
import kotlin.Metadata;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import org.toml.lang.lexer._TomlLexer;

/* compiled from: TomlJsonSchemaDocumentationProvider.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"�� \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n��\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n��\u0018��2\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u001e\u0010\u0004\u001a\u0004\u0018\u00010\u00052\b\u0010\u0006\u001a\u0004\u0018\u00010\u00072\b\u0010\b\u001a\u0004\u0018\u00010\u0007H\u0017J\u001e\u0010\t\u001a\u0004\u0018\u00010\u00052\b\u0010\u0006\u001a\u0004\u0018\u00010\u00072\b\u0010\b\u001a\u0004\u0018\u00010\u0007H\u0017J\u001c\u0010\n\u001a\u0004\u0018\u00010\u00052\b\u0010\u0006\u001a\u0004\u0018\u00010\u00072\u0006\u0010\u000b\u001a\u00020\fH\u0003¨\u0006\r"}, d2 = {"Lorg/toml/ide/json/TomlJsonSchemaDocumentationProvider;", "Lcom/intellij/lang/documentation/DocumentationProvider;", "<init>", "()V", "getQuickNavigateInfo", "", "element", "Lcom/intellij/psi/PsiElement;", "originalElement", "generateDoc", "findSchemaAndGenerateDoc", "preferShort", "", "intellij.toml.json"})
public final class TomlJsonSchemaDocumentationProvider implements DocumentationProvider {
    @Nls
    @Nullable
    public String getQuickNavigateInfo(@Nullable PsiElement element, @Nullable PsiElement originalElement) {
        return findSchemaAndGenerateDoc(element, true);
    }

    @Nls
    @Nullable
    public String generateDoc(@Nullable PsiElement element, @Nullable PsiElement originalElement) {
        return findSchemaAndGenerateDoc(element, false);
    }

    @Nls
    private final String findSchemaAndGenerateDoc(PsiElement element, boolean preferShort) {
        JsonSchemaObject schema;
        if (element == null) {
            return null;
        }
        JsonSchemaService service = JsonSchemaService.Impl.get(element.getProject());
        PsiFile file = element.getContainingFile();
        if (file == null || (schema = service.getSchemaObject(file)) == null) {
            return null;
        }
        return JsonSchemaDocumentationProvider.generateDoc(element, schema, preferShort, (String) null);
    }
}