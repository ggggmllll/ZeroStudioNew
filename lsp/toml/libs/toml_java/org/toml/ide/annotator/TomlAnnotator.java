package org.toml.ide.annotator;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.SyntaxTraverser;
import com.intellij.psi.tree.IElementType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import org.jetbrains.annotations.NotNull;
import org.toml.TomlBundle;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlArray;
import org.toml.lang.psi.TomlElementTypes;
import org.toml.lang.psi.TomlInlineTable;
import org.toml.lang.psi.TomlKeyValue;
import org.toml.lang.psi.ext.PsiElementKt;

/* compiled from: TomlAnnotator.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\u0018��2\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0018\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH\u0014¨\u0006\n"}, d2 = {"Lorg/toml/ide/annotator/TomlAnnotator;", "Lorg/toml/ide/annotator/AnnotatorBase;", "<init>", "()V", "annotateInternal", "", "element", "Lcom/intellij/psi/PsiElement;", "holder", "Lcom/intellij/lang/annotation/AnnotationHolder;", "intellij.toml.core"})
@SourceDebugExtension({"SMAP\nTomlAnnotator.kt\nKotlin\n*S Kotlin\n*F\n+ 1 TomlAnnotator.kt\norg/toml/ide/annotator/TomlAnnotator\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,58:1\n808#2,11:59\n1755#2,3:70\n*S KotlinDebug\n*F\n+ 1 TomlAnnotator.kt\norg/toml/ide/annotator/TomlAnnotator\n*L\n29#1:59,11\n30#1:70,3\n*E\n"})
public final class TomlAnnotator extends AnnotatorBase {
    @Override // org.toml.ide.annotator.AnnotatorBase
    protected void annotateInternal(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        boolean z;
        Intrinsics.checkNotNullParameter(element, "element");
        Intrinsics.checkNotNullParameter(holder, "holder");
        if (element instanceof TomlInlineTable) {
            SyntaxTraverser psiTraverser = SyntaxTraverser.psiTraverser(element);
            Function1 function1 = TomlAnnotator::annotateInternal$lambda$0;
            Iterable expand = psiTraverser.expand((v1) -> {
                return annotateInternal$lambda$1(r1, v1);
            });
            Intrinsics.checkNotNullExpressionValue(expand, "expand(...)");
            Iterable $this$filterIsInstance$iv = expand;
            Collection destination$iv$iv = new ArrayList();
            for (Object element$iv$iv : $this$filterIsInstance$iv) {
                if (element$iv$iv instanceof PsiWhiteSpace) {
                    destination$iv$iv.add(element$iv$iv);
                }
            }
            Iterable whiteSpaces = (List) destination$iv$iv;
            Iterable $this$any$iv = whiteSpaces;
            if (!($this$any$iv instanceof Collection) || !((Collection) $this$any$iv).isEmpty()) {
                Iterator it = $this$any$iv.iterator();
                while (true) {
                    if (it.hasNext()) {
                        Object element$iv = it.next();
                        PsiWhiteSpace it2 = (PsiWhiteSpace) element$iv;
                        if (it2.textContains('\n')) {
                            z = true;
                            break;
                        }
                    } else {
                        z = false;
                        break;
                    }
                }
            } else {
                z = false;
            }
            if (z) {
                holder.newAnnotation(HighlightSeverity.ERROR, TomlBundle.INSTANCE.message("inspection.toml.message.inline.tables.on.single.line", new Object[0])).create();
            }
        }
        PsiElement parent = element.getParent();
        if (Intrinsics.areEqual(PsiElementKt.getElementType(element), TomlElementTypes.COMMA) && (parent instanceof TomlInlineTable)) {
            int textOffset = element.getTextOffset();
            TomlKeyValue tomlKeyValue = (TomlKeyValue) CollectionsKt.lastOrNull(((TomlInlineTable) parent).getEntries());
            if (textOffset > (tomlKeyValue != null ? tomlKeyValue.getTextOffset() : 0)) {
                final String message = TomlBundle.INSTANCE.message("intention.toml.name.remove.trailing.comma", new Object[0]);
                LocalQuickFix localQuickFix = new LocalQuickFix() { // from class: org.toml.ide.annotator.TomlAnnotator$annotateInternal$fix$1
                    public String getFamilyName() {
                        return message;
                    }

                    public void applyFix(Project project, ProblemDescriptor descriptor) {
                        Intrinsics.checkNotNullParameter(project, "project");
                        Intrinsics.checkNotNullParameter(descriptor, "descriptor");
                        PsiElement psiElement = descriptor.getPsiElement();
                        if (psiElement != null) {
                            psiElement.delete();
                        }
                    }
                };
                ProblemDescriptor problemDescriptor = InspectionManager.getInstance(element.getProject()).createProblemDescriptor(element, message, localQuickFix, ProblemHighlightType.ERROR, true);
                Intrinsics.checkNotNullExpressionValue(problemDescriptor, "createProblemDescriptor(...)");
                holder.newAnnotation(HighlightSeverity.ERROR, TomlBundle.INSTANCE.message("inspection.toml.message.trailing.commas.in.inline.tables", new Object[0])).newLocalQuickFix(localQuickFix, problemDescriptor).registerFix().create();
            }
        }
    }

    private static final boolean annotateInternal$lambda$0(PsiElement it) {
        return !(it instanceof TomlArray);
    }

    private static final boolean annotateInternal$lambda$1(Function1 $tmp0, Object p0) {
        return ((Boolean) $tmp0.invoke(p0)).booleanValue();
    }
}