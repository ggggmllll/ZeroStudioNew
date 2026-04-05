package org.toml.ide.json;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.jsonSchema.extension.JsonLikePsiWalker;
import com.jetbrains.jsonSchema.extension.JsonLikePsiWalkerFactory;
import com.jetbrains.jsonSchema.impl.JsonSchemaObject;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlFile;

/* compiled from: TomlJsonLikePsiWalkerFactory.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\u0018��2\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0016J\u0012\u0010\b\u001a\u00020\t2\b\u0010\n\u001a\u0004\u0018\u00010\u000bH\u0016¨\u0006\f"}, d2 = {"Lorg/toml/ide/json/TomlJsonLikePsiWalkerFactory;", "Lcom/jetbrains/jsonSchema/extension/JsonLikePsiWalkerFactory;", "<init>", "()V", "handles", "", "element", "Lcom/intellij/psi/PsiElement;", "create", "Lcom/jetbrains/jsonSchema/extension/JsonLikePsiWalker;", "schemaObject", "Lcom/jetbrains/jsonSchema/impl/JsonSchemaObject;", "intellij.toml.json"})
public final class TomlJsonLikePsiWalkerFactory implements JsonLikePsiWalkerFactory {
    public boolean handles(@NotNull PsiElement element) {
        Intrinsics.checkNotNullParameter(element, "element");
        return element.getContainingFile() instanceof TomlFile;
    }

    @NotNull
    public JsonLikePsiWalker create(@Nullable JsonSchemaObject schemaObject) {
        return TomlJsonPsiWalker.INSTANCE;
    }
}