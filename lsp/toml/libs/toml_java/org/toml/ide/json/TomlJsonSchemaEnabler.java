package org.toml.ide.json;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.jsonSchema.extension.JsonSchemaEnabler;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.ide.experiments.TomlExperiments;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlFileType;

/* compiled from: TomlJsonSchemaEnabler.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\u0018��2\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u001a\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\b\u0010\b\u001a\u0004\u0018\u00010\tH\u0016¨\u0006\n"}, d2 = {"Lorg/toml/ide/json/TomlJsonSchemaEnabler;", "Lcom/jetbrains/jsonSchema/extension/JsonSchemaEnabler;", "<init>", "()V", "isEnabledForFile", "", "file", "Lcom/intellij/openapi/vfs/VirtualFile;", "project", "Lcom/intellij/openapi/project/Project;", "intellij.toml.json"})
public final class TomlJsonSchemaEnabler implements JsonSchemaEnabler {
    public boolean isEnabledForFile(@NotNull VirtualFile file, @Nullable Project project) {
        Intrinsics.checkNotNullParameter(file, "file");
        if (TomlExperiments.INSTANCE.isJsonSchemaEnabled()) {
            return file.getFileType() instanceof TomlFileType;
        }
        return false;
    }
}