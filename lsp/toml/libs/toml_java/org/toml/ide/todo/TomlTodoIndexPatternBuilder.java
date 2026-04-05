package org.toml.ide.todo;

import com.intellij.lexer.Lexer;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.search.IndexPatternBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.lang.lexer.TomlLexer;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.ElementTypesKt;
import org.toml.lang.psi.TomlFile;

/* compiled from: TomlTodoIndexPatternBuilder.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0010\b\n��\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0002\u0018��2\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0012\u0010\u0004\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0016J\u0012\u0010\b\u001a\u0004\u0018\u00010\t2\u0006\u0010\u0006\u001a\u00020\u0007H\u0016J\u0012\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\rH\u0016J\u0012\u0010\u000e\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\rH\u0016¨\u0006\u000f"}, d2 = {"Lorg/toml/ide/todo/TomlTodoIndexPatternBuilder;", "Lcom/intellij/psi/impl/search/IndexPatternBuilder;", "<init>", "()V", "getIndexingLexer", "Lcom/intellij/lexer/Lexer;", "file", "Lcom/intellij/psi/PsiFile;", "getCommentTokenSet", "Lcom/intellij/psi/tree/TokenSet;", "getCommentStartDelta", "", "tokenType", "Lcom/intellij/psi/tree/IElementType;", "getCommentEndDelta", "intellij.toml.core"})
final class TomlTodoIndexPatternBuilder implements IndexPatternBuilder {
    @Nullable
    public Lexer getIndexingLexer(@NotNull PsiFile file) {
        Intrinsics.checkNotNullParameter(file, "file");
        if (file instanceof TomlFile) {
            return new TomlLexer();
        }
        return null;
    }

    @Nullable
    public TokenSet getCommentTokenSet(@NotNull PsiFile file) {
        Intrinsics.checkNotNullParameter(file, "file");
        if (file instanceof TomlFile) {
            return ElementTypesKt.getTOML_COMMENTS();
        }
        return null;
    }

    public int getCommentStartDelta(@Nullable IElementType tokenType) {
        return ElementTypesKt.getTOML_COMMENTS().contains(tokenType) ? 1 : 0;
    }

    public int getCommentEndDelta(@Nullable IElementType tokenType) {
        return 0;
    }
}