package org.toml.ide.intentions;

import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.lang.lexer._TomlLexer;

/* compiled from: TomlElementBaseIntentionAction.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��0\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n��\b&\u0018��*\u0004\b��\u0010\u00012\u00020\u0002B\u0007¢\u0006\u0004\b\u0003\u0010\u0004J'\u0010\u0005\u001a\u0004\u0018\u00018��2\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bH&¢\u0006\u0002\u0010\fJ%\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u000f\u001a\u00028��H&¢\u0006\u0002\u0010\u0010J!\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bH\u0086\u0002J\u001e\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b¨\u0006\u0013"}, d2 = {"Lorg/toml/ide/intentions/TomlElementBaseIntentionAction;", "Ctx", "Lcom/intellij/codeInsight/intention/BaseElementAtCaretIntentionAction;", "<init>", "()V", "findApplicableContext", "project", "Lcom/intellij/openapi/project/Project;", "editor", "Lcom/intellij/openapi/editor/Editor;", "element", "Lcom/intellij/psi/PsiElement;", "(Lcom/intellij/openapi/project/Project;Lcom/intellij/openapi/editor/Editor;Lcom/intellij/psi/PsiElement;)Ljava/lang/Object;", "invoke", "", "ctx", "(Lcom/intellij/openapi/project/Project;Lcom/intellij/openapi/editor/Editor;Ljava/lang/Object;)V", "isAvailable", "", "intellij.toml.core"})
public abstract class TomlElementBaseIntentionAction<Ctx> extends BaseElementAtCaretIntentionAction {
    @Nullable
    public abstract Ctx findApplicableContext(@NotNull Project project, @NotNull Editor editor, @NotNull PsiElement psiElement);

    public abstract void invoke(@NotNull Project project, @NotNull Editor editor, Ctx ctx);

    public final void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiElement element) {
        Intrinsics.checkNotNullParameter(project, "project");
        Intrinsics.checkNotNullParameter(editor, "editor");
        Intrinsics.checkNotNullParameter(element, "element");
        Ctx findApplicableContext = findApplicableContext(project, editor, element);
        if (findApplicableContext == null) {
            return;
        }
        if (startInWriteAction() && !IntentionPreviewUtils.isPreviewElement(element)) {
            TomlElementBaseIntentionActionKt.checkWriteAccessAllowed();
        }
        invoke(project, editor, (Editor) findApplicableContext);
    }

    public final boolean isAvailable(@NotNull Project project, @NotNull Editor editor, @NotNull PsiElement element) {
        Intrinsics.checkNotNullParameter(project, "project");
        Intrinsics.checkNotNullParameter(editor, "editor");
        Intrinsics.checkNotNullParameter(element, "element");
        TomlElementBaseIntentionActionKt.checkReadAccessAllowed();
        return findApplicableContext(project, editor, element) != null;
    }
}