package org.toml.ide.folding;

import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import java.util.List;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlArray;
import org.toml.lang.psi.TomlArrayTable;
import org.toml.lang.psi.TomlInlineTable;
import org.toml.lang.psi.TomlRecursiveVisitor;
import org.toml.lang.psi.TomlTable;

/* compiled from: TomlFoldingBuilder.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0002\u0018��2\u00020\u0001B\u0015\u0012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003¢\u0006\u0004\b\u0005\u0010\u0006J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0016J\u0010\u0010\u000b\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\fH\u0016J\u0010\u0010\r\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u000eH\u0016J\u0010\u0010\u000f\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0010H\u0016J\u0010\u0010\u0011\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0012H\u0002J \u0010\u0013\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u00122\u0006\u0010\u0014\u001a\u00020\u00122\u0006\u0010\u0015\u001a\u00020\u0012H\u0002R\u0014\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003X\u0082\u0004¢\u0006\u0002\n��¨\u0006\u0016"}, d2 = {"Lorg/toml/ide/folding/TomlFoldingVisitor;", "Lorg/toml/lang/psi/TomlRecursiveVisitor;", "descriptors", "", "Lcom/intellij/lang/folding/FoldingDescriptor;", "<init>", "(Ljava/util/List;)V", "visitTable", "", "element", "Lorg/toml/lang/psi/TomlTable;", "visitArrayTable", "Lorg/toml/lang/psi/TomlArrayTable;", "visitInlineTable", "Lorg/toml/lang/psi/TomlInlineTable;", "visitArray", "Lorg/toml/lang/psi/TomlArray;", "fold", "Lcom/intellij/psi/PsiElement;", "foldChildren", "firstChild", "lastChild", "intellij.toml.core"})
final class TomlFoldingVisitor extends TomlRecursiveVisitor {
    @NotNull
    private final List<FoldingDescriptor> descriptors;

    public TomlFoldingVisitor(@NotNull List<FoldingDescriptor> list) {
        Intrinsics.checkNotNullParameter(list, "descriptors");
        this.descriptors = list;
    }

    @Override // org.toml.lang.psi.TomlVisitor
    public void visitTable(@NotNull TomlTable element) {
        Intrinsics.checkNotNullParameter(element, "element");
        if (!element.getEntries().isEmpty()) {
            PsiElement nextSibling = element.getHeader().getNextSibling();
            Intrinsics.checkNotNullExpressionValue(nextSibling, "getNextSibling(...)");
            PsiElement lastChild = element.getLastChild();
            Intrinsics.checkNotNullExpressionValue(lastChild, "getLastChild(...)");
            foldChildren(element, nextSibling, lastChild);
            super.visitTable(element);
        }
    }

    @Override // org.toml.lang.psi.TomlVisitor
    public void visitArrayTable(@NotNull TomlArrayTable element) {
        Intrinsics.checkNotNullParameter(element, "element");
        if (!element.getEntries().isEmpty()) {
            PsiElement nextSibling = element.getHeader().getNextSibling();
            Intrinsics.checkNotNullExpressionValue(nextSibling, "getNextSibling(...)");
            PsiElement lastChild = element.getLastChild();
            Intrinsics.checkNotNullExpressionValue(lastChild, "getLastChild(...)");
            foldChildren(element, nextSibling, lastChild);
            super.visitArrayTable(element);
        }
    }

    @Override // org.toml.lang.psi.TomlVisitor
    public void visitInlineTable(@NotNull TomlInlineTable element) {
        Intrinsics.checkNotNullParameter(element, "element");
        if (!element.getEntries().isEmpty()) {
            fold(element);
            super.visitInlineTable(element);
        }
    }

    @Override // org.toml.lang.psi.TomlVisitor
    public void visitArray(@NotNull TomlArray element) {
        Intrinsics.checkNotNullParameter(element, "element");
        if (!element.getElements().isEmpty()) {
            fold(element);
            super.visitArray(element);
        }
    }

    private final void fold(PsiElement element) {
        this.descriptors.add(new FoldingDescriptor(element.getNode(), element.getTextRange()));
    }

    private final void foldChildren(PsiElement element, PsiElement firstChild, PsiElement lastChild) {
        int start = firstChild.getTextRange().getStartOffset();
        int end = lastChild.getTextRange().getEndOffset();
        this.descriptors.add(new FoldingDescriptor(element.getNode(), new TextRange(start, end)));
    }
}