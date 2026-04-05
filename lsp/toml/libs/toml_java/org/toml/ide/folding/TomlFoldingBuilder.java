package org.toml.ide.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import java.util.List;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlElementTypes;
import org.toml.lang.psi.TomlFile;

/* compiled from: TomlFoldingBuilder.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n��\n\u0002\u0010!\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0010\u000b\n��\n\u0002\u0010\u000e\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018��2\u00020\u00012\u00020\u0002B\u0007¢\u0006\u0004\b\u0003\u0010\u0004J.\u0010\u0005\u001a\u00020\u00062\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\u0014J\u0018\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0015H\u0014J\u0010\u0010\u0016\u001a\u00020\u000f2\u0006\u0010\u0012\u001a\u00020\u0013H\u0014¨\u0006\u0017"}, d2 = {"Lorg/toml/ide/folding/TomlFoldingBuilder;", "Lcom/intellij/lang/folding/CustomFoldingBuilder;", "Lcom/intellij/openapi/project/DumbAware;", "<init>", "()V", "buildLanguageFoldRegions", "", "descriptors", "", "Lcom/intellij/lang/folding/FoldingDescriptor;", "root", "Lcom/intellij/psi/PsiElement;", "document", "Lcom/intellij/openapi/editor/Document;", "quick", "", "getLanguagePlaceholderText", "", "node", "Lcom/intellij/lang/ASTNode;", "range", "Lcom/intellij/openapi/util/TextRange;", "isRegionCollapsedByDefault", "intellij.toml.core"})
public final class TomlFoldingBuilder extends CustomFoldingBuilder implements DumbAware {
    protected void buildLanguageFoldRegions(@NotNull List<FoldingDescriptor> list, @NotNull PsiElement root, @NotNull Document document, boolean quick) {
        Intrinsics.checkNotNullParameter(list, "descriptors");
        Intrinsics.checkNotNullParameter(root, "root");
        Intrinsics.checkNotNullParameter(document, "document");
        if (root instanceof TomlFile) {
            TomlFoldingVisitor visitor = new TomlFoldingVisitor(list);
            ((TomlFile) root).accept(visitor);
        }
    }

    @NotNull
    protected String getLanguagePlaceholderText(@NotNull ASTNode node, @NotNull TextRange range) {
        Intrinsics.checkNotNullParameter(node, "node");
        Intrinsics.checkNotNullParameter(range, "range");
        return Intrinsics.areEqual(node.getElementType(), TomlElementTypes.ARRAY) ? "[...]" : "{...}";
    }

    protected boolean isRegionCollapsedByDefault(@NotNull ASTNode node) {
        Intrinsics.checkNotNullParameter(node, "node");
        return false;
    }
}