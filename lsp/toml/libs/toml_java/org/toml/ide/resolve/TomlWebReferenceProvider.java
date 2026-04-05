package org.toml.ide.resolve;

import com.intellij.openapi.paths.GlobalPathReferenceProvider;
import com.intellij.openapi.paths.WebReference;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ProcessingContext;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlLiteral;
import org.toml.lang.psi.ext.TomlLiteralKind;
import org.toml.lang.psi.ext.TomlLiteralKt;

/* compiled from: TomlReferenceContributor.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n��\n\u0002\u0018\u0002\n��\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0002\u0018��2\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0016J#\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\t2\u0006\u0010\u000b\u001a\u00020\u00072\u0006\u0010\f\u001a\u00020\rH\u0016¢\u0006\u0002\u0010\u000e¨\u0006\u000f"}, d2 = {"Lorg/toml/ide/resolve/TomlWebReferenceProvider;", "Lcom/intellij/psi/PsiReferenceProvider;", "<init>", "()V", "acceptsTarget", "", "target", "Lcom/intellij/psi/PsiElement;", "getReferencesByElement", "", "Lcom/intellij/psi/PsiReference;", "element", "context", "Lcom/intellij/util/ProcessingContext;", "(Lcom/intellij/psi/PsiElement;Lcom/intellij/util/ProcessingContext;)[Lcom/intellij/psi/PsiReference;", "intellij.toml.core"})
final class TomlWebReferenceProvider extends PsiReferenceProvider {
    public boolean acceptsTarget(@NotNull PsiElement target) {
        Intrinsics.checkNotNullParameter(target, "target");
        return false;
    }

    @NotNull
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        Intrinsics.checkNotNullParameter(element, "element");
        Intrinsics.checkNotNullParameter(context, "context");
        TomlLiteral tomlLiteral = element instanceof TomlLiteral ? (TomlLiteral) element : null;
        TomlLiteralKind kind = tomlLiteral != null ? TomlLiteralKt.getKind(tomlLiteral) : null;
        TomlLiteralKind.String string = kind instanceof TomlLiteralKind.String ? (TomlLiteralKind.String) kind : null;
        if (string == null) {
            PsiReference[] psiReferenceArr = PsiReference.EMPTY_ARRAY;
            Intrinsics.checkNotNullExpressionValue(psiReferenceArr, "EMPTY_ARRAY");
            return psiReferenceArr;
        }
        TomlLiteralKind.String kind2 = string;
        if (!element.textContains(':')) {
            PsiReference[] psiReferenceArr2 = PsiReference.EMPTY_ARRAY;
            Intrinsics.checkNotNullExpressionValue(psiReferenceArr2, "EMPTY_ARRAY");
            return psiReferenceArr2;
        }
        String textValue = kind2.getValue();
        if (textValue == null) {
            PsiReference[] psiReferenceArr3 = PsiReference.EMPTY_ARRAY;
            Intrinsics.checkNotNullExpressionValue(psiReferenceArr3, "EMPTY_ARRAY");
            return psiReferenceArr3;
        } else if (GlobalPathReferenceProvider.isWebReferenceUrl(textValue)) {
            return new PsiReference[]{new WebReference(element, textValue)};
        } else {
            PsiReference[] psiReferenceArr4 = PsiReference.EMPTY_ARRAY;
            Intrinsics.checkNotNull(psiReferenceArr4);
            return psiReferenceArr4;
        }
    }
}