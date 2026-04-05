package org.toml.ide.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.IElementType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import org.jetbrains.annotations.NotNull;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlLiteral;
import org.toml.lang.psi.TomlVisitor;
import org.toml.lang.psi.ext.TomlLiteralKind;
import org.toml.lang.psi.ext.TomlLiteralKt;

/* compiled from: TomlUnresolvedReferenceInspection.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0010\u000b\n��\n\u0002\u0010\u0002\n��\n\u0002\u0018\u0002\n��\u0018��2\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0018\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH\u0016J\u0018\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u0006\u001a\u00020\u0007H\u0002¨\u0006\u000e"}, d2 = {"Lorg/toml/ide/inspections/TomlUnresolvedReferenceInspection;", "Lcom/intellij/codeInspection/LocalInspectionTool;", "<init>", "()V", "buildVisitor", "Lcom/intellij/psi/PsiElementVisitor;", "holder", "Lcom/intellij/codeInspection/ProblemsHolder;", "isOnTheFly", "", "checkReference", "", "element", "Lcom/intellij/psi/PsiElement;", "intellij.toml.core"})
@SourceDebugExtension({"SMAP\nTomlUnresolvedReferenceInspection.kt\nKotlin\n*S Kotlin\n*F\n+ 1 TomlUnresolvedReferenceInspection.kt\norg/toml/ide/inspections/TomlUnresolvedReferenceInspection\n+ 2 _Arrays.kt\nkotlin/collections/ArraysKt___ArraysKt\n+ 3 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,34:1\n3829#2:35\n4344#2,2:36\n1863#3,2:38\n*S KotlinDebug\n*F\n+ 1 TomlUnresolvedReferenceInspection.kt\norg/toml/ide/inspections/TomlUnresolvedReferenceInspection\n*L\n30#1:35\n30#1:36,2\n31#1:38,2\n*E\n"})
public final class TomlUnresolvedReferenceInspection extends LocalInspectionTool {
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        Intrinsics.checkNotNullParameter(holder, "holder");
        return new TomlVisitor() { // from class: org.toml.ide.inspections.TomlUnresolvedReferenceInspection$buildVisitor$1
            @Override // org.toml.lang.psi.TomlVisitor
            public void visitLiteral(TomlLiteral element) {
                Intrinsics.checkNotNullParameter(element, "element");
                if (TomlLiteralKt.getKind(element) instanceof TomlLiteralKind.String) {
                    TomlUnresolvedReferenceInspection.this.checkReference(element, holder);
                }
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void checkReference(PsiElement element, ProblemsHolder holder) {
        PsiReference[] references = element.getReferences();
        Intrinsics.checkNotNullExpressionValue(references, "getReferences(...)");
        PsiReference[] psiReferenceArr = references;
        Collection destination$iv$iv = new ArrayList();
        for (PsiReference it : psiReferenceArr) {
            if (it.resolve() == null) {
                destination$iv$iv.add(it);
            }
        }
        Iterable $this$forEach$iv = (List) destination$iv$iv;
        for (Object element$iv : $this$forEach$iv) {
            PsiReference it2 = (PsiReference) element$iv;
            holder.registerProblem(it2, ProblemsHolder.unresolvedReferenceMessage(it2), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
        }
    }
}