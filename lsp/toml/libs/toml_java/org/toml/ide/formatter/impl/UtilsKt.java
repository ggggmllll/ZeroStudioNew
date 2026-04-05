package org.toml.ide.formatter.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlElementTypes;

/* compiled from: utils.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = 2, xi = 48, d1 = {"��\u001a\n��\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\f\u0010\u0005\u001a\u00020\u0006*\u0004\u0018\u00010\u0007\u001a\n\u0010\b\u001a\u00020\u0006*\u00020\u0007\"\u0016\u0010��\u001a\u00070\u0001¢\u0006\u0002\b\u0002¢\u0006\b\n��\u001a\u0004\b\u0003\u0010\u0004¨\u0006\t"}, d2 = {"ARRAY_DELIMITERS", "Lcom/intellij/psi/tree/TokenSet;", "Lorg/jetbrains/annotations/NotNull;", "getARRAY_DELIMITERS", "()Lcom/intellij/psi/tree/TokenSet;", "isWhitespaceOrEmpty", "", "Lcom/intellij/lang/ASTNode;", "isArrayDelimiter", "intellij.toml.core"})
public final class UtilsKt {
    @NotNull
    private static final TokenSet ARRAY_DELIMITERS;

    @NotNull
    public static final TokenSet getARRAY_DELIMITERS() {
        return ARRAY_DELIMITERS;
    }

    static {
        TokenSet create = TokenSet.create(new IElementType[]{TomlElementTypes.L_BRACKET, TomlElementTypes.R_BRACKET});
        Intrinsics.checkNotNullExpressionValue(create, "create(...)");
        ARRAY_DELIMITERS = create;
    }

    public static final boolean isWhitespaceOrEmpty(@Nullable ASTNode $this$isWhitespaceOrEmpty) {
        return $this$isWhitespaceOrEmpty == null || $this$isWhitespaceOrEmpty.getTextLength() == 0 || Intrinsics.areEqual($this$isWhitespaceOrEmpty.getElementType(), TokenType.WHITE_SPACE);
    }

    public static final boolean isArrayDelimiter(@NotNull ASTNode $this$isArrayDelimiter) {
        Intrinsics.checkNotNullParameter($this$isArrayDelimiter, "<this>");
        return ARRAY_DELIMITERS.contains($this$isArrayDelimiter.getElementType());
    }
}