package org.toml.ide.formatter;

import com.intellij.formatting.ASTBlock;
import com.intellij.formatting.Alignment;
import com.intellij.formatting.FormattingContext;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.formatting.FormattingModelProvider;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Wrap;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.lang.lexer._TomlLexer;

/* compiled from: TomlFormattingModelBuilder.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��2\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0010\b\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018�� \u00102\u00020\u0001:\u0001\u0010B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J&\u0010\u0004\u001a\u0004\u0018\u00010\u00052\b\u0010\u0006\u001a\u0004\u0018\u00010\u00072\u0006\u0010\b\u001a\u00020\t2\b\u0010\n\u001a\u0004\u0018\u00010\u000bH\u0016J\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\u0016¨\u0006\u0011"}, d2 = {"Lorg/toml/ide/formatter/TomlFormattingModelBuilder;", "Lcom/intellij/formatting/FormattingModelBuilder;", "<init>", "()V", "getRangeAffectingIndent", "Lcom/intellij/openapi/util/TextRange;", "file", "Lcom/intellij/psi/PsiFile;", "offset", "", "elementAtOffset", "Lcom/intellij/lang/ASTNode;", "createModel", "Lcom/intellij/formatting/FormattingModel;", "formattingContext", "Lcom/intellij/formatting/FormattingContext;", "Companion", "intellij.toml.core"})
public final class TomlFormattingModelBuilder implements FormattingModelBuilder {
    @NotNull
    public static final Companion Companion = new Companion(null);

    @Nullable
    public TextRange getRangeAffectingIndent(@Nullable PsiFile file, int offset, @Nullable ASTNode elementAtOffset) {
        return null;
    }

    @NotNull
    public FormattingModel createModel(@NotNull FormattingContext formattingContext) {
        Intrinsics.checkNotNullParameter(formattingContext, "formattingContext");
        CodeStyleSettings settings = formattingContext.getCodeStyleSettings();
        Intrinsics.checkNotNullExpressionValue(settings, "getCodeStyleSettings(...)");
        PsiElement element = formattingContext.getPsiElement();
        Intrinsics.checkNotNullExpressionValue(element, "getPsiElement(...)");
        TomlFmtContext ctx = TomlFmtContext.Companion.create(settings);
        Companion companion = Companion;
        ASTNode node = element.getNode();
        Intrinsics.checkNotNullExpressionValue(node, "getNode(...)");
        FormattingModel createFormattingModelForPsiFile = FormattingModelProvider.createFormattingModelForPsiFile(element.getContainingFile(), companion.createBlock(node, null, Indent.getNoneIndent(), null, ctx), settings);
        Intrinsics.checkNotNullExpressionValue(createFormattingModelForPsiFile, "createFormattingModelForPsiFile(...)");
        return createFormattingModelForPsiFile;
    }

    /* compiled from: TomlFormattingModelBuilder.kt */
    @Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��0\n\u0002\u0018\u0002\n\u0002\u0010��\n\u0002\b\u0003\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\b\u0086\u0003\u0018��2\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J4\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\b\u0010\b\u001a\u0004\u0018\u00010\t2\b\u0010\n\u001a\u0004\u0018\u00010\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\r2\u0006\u0010\u000e\u001a\u00020\u000f¨\u0006\u0010"}, d2 = {"Lorg/toml/ide/formatter/TomlFormattingModelBuilder$Companion;", "", "<init>", "()V", "createBlock", "Lcom/intellij/formatting/ASTBlock;", "node", "Lcom/intellij/lang/ASTNode;", "alignment", "Lcom/intellij/formatting/Alignment;", "indent", "Lcom/intellij/formatting/Indent;", "wrap", "Lcom/intellij/formatting/Wrap;", "ctx", "Lorg/toml/ide/formatter/TomlFmtContext;", "intellij.toml.core"})
    /* loaded from: 2026040502522302a7da3a-171e-4c6c-a1b5-134e29fd6e0f.jar:org/toml/ide/formatter/TomlFormattingModelBuilder$Companion.class */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker $constructor_marker) {
            this();
        }

        private Companion() {
        }

        @NotNull
        public final ASTBlock createBlock(@NotNull ASTNode node, @Nullable Alignment alignment, @Nullable Indent indent, @Nullable Wrap wrap, @NotNull TomlFmtContext ctx) {
            Intrinsics.checkNotNullParameter(node, "node");
            Intrinsics.checkNotNullParameter(ctx, "ctx");
            return new TomlFmtBlock(node, alignment, indent, wrap, ctx);
        }
    }
}