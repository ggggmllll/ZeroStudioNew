package org.toml.ide.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.toml.ide.colors.TomlColor;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlElementTypes;

/* compiled from: TomlHighlightingAnnotator.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\u0018��2\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0018\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH\u0014¨\u0006\n"}, d2 = {"Lorg/toml/ide/annotator/TomlHighlightingAnnotator;", "Lorg/toml/ide/annotator/AnnotatorBase;", "<init>", "()V", "annotateInternal", "", "element", "Lcom/intellij/psi/PsiElement;", "holder", "Lcom/intellij/lang/annotation/AnnotationHolder;", "intellij.toml.core"})
public final class TomlHighlightingAnnotator extends AnnotatorBase {
    @Override // org.toml.ide.annotator.AnnotatorBase
    protected void annotateInternal(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        TomlColor tomlColor;
        Intrinsics.checkNotNullParameter(element, "element");
        Intrinsics.checkNotNullParameter(holder, "holder");
        if (holder.isBatchMode()) {
            return;
        }
        IElementType elementType = element.getNode().getElementType();
        if (Intrinsics.areEqual(elementType, TomlElementTypes.NUMBER)) {
            tomlColor = TomlColor.NUMBER;
        } else if (Intrinsics.areEqual(elementType, TomlElementTypes.DATE_TIME)) {
            tomlColor = TomlColor.DATE;
        } else if (!Intrinsics.areEqual(elementType, TomlElementTypes.BARE_KEY)) {
            return;
        } else {
            tomlColor = TomlColor.KEY;
        }
        TomlColor color = tomlColor;
        HighlightSeverity severity = ApplicationManager.getApplication().isUnitTestMode() ? color.getTestSeverity() : HighlightSeverity.INFORMATION;
        holder.newSilentAnnotation(severity).textAttributes(color.getTextAttributesKey()).create();
    }
}