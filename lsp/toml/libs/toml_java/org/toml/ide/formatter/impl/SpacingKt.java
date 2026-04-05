package org.toml.ide.formatter.impl;

import com.intellij.formatting.Block;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.ide.formatter.TomlFmtContext;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlElementTypes;

/* compiled from: spacing.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = 2, xi = 48, d1 = {"�� \n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n��\u001a\u000e\u0010��\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003\u001a&\u0010\u0004\u001a\u0004\u0018\u00010\u0005*\u00020\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\u00062\u0006\u0010\b\u001a\u00020\u00062\u0006\u0010\t\u001a\u00020\n¨\u0006\u000b"}, d2 = {"createSpacingBuilder", "Lcom/intellij/formatting/SpacingBuilder;", "commonSettings", "Lcom/intellij/psi/codeStyle/CommonCodeStyleSettings;", "computeSpacing", "Lcom/intellij/formatting/Spacing;", "Lcom/intellij/formatting/Block;", "child1", "child2", "ctx", "Lorg/toml/ide/formatter/TomlFmtContext;", "intellij.toml.core"})
public final class SpacingKt {
    @NotNull
    public static final SpacingBuilder createSpacingBuilder(@NotNull CommonCodeStyleSettings commonSettings) {
        Intrinsics.checkNotNullParameter(commonSettings, "commonSettings");
        SpacingBuilder spaceIf = new SpacingBuilder(commonSettings).after(TomlElementTypes.COMMA).spacing(1, 1, 0, true, 0).before(TomlElementTypes.COMMA).spaceIf(false).around(TomlElementTypes.EQ).spacing(1, 1, 0, true, 0).after(TomlElementTypes.L_BRACKET).spaceIf(false).before(TomlElementTypes.R_BRACKET).spaceIf(false).after(TomlElementTypes.L_CURLY).spaceIf(true).before(TomlElementTypes.R_CURLY).spaceIf(true).aroundInside(TomlElementTypes.DOT, TokenSet.create(new IElementType[]{TomlElementTypes.KEY, TomlElementTypes.TABLE_HEADER})).spaceIf(false);
        Intrinsics.checkNotNullExpressionValue(spaceIf, "spaceIf(...)");
        return spaceIf;
    }

    @Nullable
    public static final Spacing computeSpacing(@NotNull Block $this$computeSpacing, @Nullable Block child1, @NotNull Block child2, @NotNull TomlFmtContext ctx) {
        Intrinsics.checkNotNullParameter($this$computeSpacing, "<this>");
        Intrinsics.checkNotNullParameter(child2, "child2");
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        return ctx.getSpacingBuilder().getSpacing($this$computeSpacing, child1, child2);
    }
}