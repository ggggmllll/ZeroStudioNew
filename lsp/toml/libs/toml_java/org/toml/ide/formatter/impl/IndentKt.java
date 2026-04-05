package org.toml.ide.formatter.impl;

import com.intellij.formatting.Indent;
import com.intellij.lang.ASTNode;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.ide.formatter.TomlFmtBlock;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlElementTypes;

/* compiled from: indent.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = 2, xi = 48, d1 = {"��\u0014\n��\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0003\u001a\u0014\u0010��\u001a\u0004\u0018\u00010\u0001*\u00020\u00022\u0006\u0010\u0003\u001a\u00020\u0004\u001a\u0010\u0010\u0005\u001a\u00020\u00012\u0006\u0010\u0006\u001a\u00020\u0004H\u0002¨\u0006\u0007"}, d2 = {"computeIndent", "Lcom/intellij/formatting/Indent;", "Lorg/toml/ide/formatter/TomlFmtBlock;", "child", "Lcom/intellij/lang/ASTNode;", "getArrayIndent", "node", "intellij.toml.core"})
public final class IndentKt {
    @Nullable
    public static final Indent computeIndent(@NotNull TomlFmtBlock $this$computeIndent, @NotNull ASTNode child) {
        Intrinsics.checkNotNullParameter($this$computeIndent, "<this>");
        Intrinsics.checkNotNullParameter(child, "child");
        return Intrinsics.areEqual($this$computeIndent.getNode().getElementType(), TomlElementTypes.ARRAY) ? getArrayIndent(child) : Indent.getNoneIndent();
    }

    private static final Indent getArrayIndent(ASTNode node) {
        if (!UtilsKt.isArrayDelimiter(node)) {
            Indent normalIndent = Indent.getNormalIndent();
            Intrinsics.checkNotNullExpressionValue(normalIndent, "getNormalIndent(...)");
            return normalIndent;
        }
        Indent noneIndent = Indent.getNoneIndent();
        Intrinsics.checkNotNullExpressionValue(noneIndent, "getNoneIndent(...)");
        return noneIndent;
    }
}