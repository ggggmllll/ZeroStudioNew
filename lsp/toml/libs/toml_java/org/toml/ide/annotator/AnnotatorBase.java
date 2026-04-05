package org.toml.ide.annotator;

import com.intellij.concurrency.ConcurrentCollectionFactory;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import java.util.Set;
import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.toml.lang.lexer._TomlLexer;

/* compiled from: AnnotatorBase.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"�� \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0003\b&\u0018�� \u000b2\u00020\u0001:\u0001\u000bB\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0016\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tJ\u0018\u0010\n\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH$¨\u0006\f"}, d2 = {"Lorg/toml/ide/annotator/AnnotatorBase;", "Lcom/intellij/lang/annotation/Annotator;", "<init>", "()V", "annotate", "", "element", "Lcom/intellij/psi/PsiElement;", "holder", "Lcom/intellij/lang/annotation/AnnotationHolder;", "annotateInternal", "Companion", "intellij.toml.core"})
public abstract class AnnotatorBase implements Annotator {
    @NotNull
    public static final Companion Companion = new Companion(null);
    @NotNull
    private static final Set<Class<? extends AnnotatorBase>> enabledAnnotators;

    protected abstract void annotateInternal(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder);

    public final void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        Intrinsics.checkNotNullParameter(element, "element");
        Intrinsics.checkNotNullParameter(holder, "holder");
        if (!ApplicationManager.getApplication().isUnitTestMode() || enabledAnnotators.contains(getClass())) {
            annotateInternal(element, holder);
        }
    }

    /* compiled from: AnnotatorBase.kt */
    @Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��(\n\u0002\u0018\u0002\n\u0002\u0010��\n\u0002\b\u0003\n\u0002\u0010#\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n��\b\u0086\u0003\u0018��2\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J \u0010\b\u001a\u00020\t2\u000e\u0010\n\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00070\u00062\u0006\u0010\u000b\u001a\u00020\fH\u0007R\u001c\u0010\u0004\u001a\u0010\u0012\f\u0012\n\u0012\u0006\b\u0001\u0012\u00020\u00070\u00060\u0005X\u0082\u0004¢\u0006\u0002\n��¨\u0006\r"}, d2 = {"Lorg/toml/ide/annotator/AnnotatorBase$Companion;", "", "<init>", "()V", "enabledAnnotators", "", "Ljava/lang/Class;", "Lorg/toml/ide/annotator/AnnotatorBase;", "enableAnnotator", "", "annotatorClass", "parentDisposable", "Lcom/intellij/openapi/Disposable;", "intellij.toml.core"})
    /* loaded from: 2026040502522302a7da3a-171e-4c6c-a1b5-134e29fd6e0f.jar:org/toml/ide/annotator/AnnotatorBase$Companion.class */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker $constructor_marker) {
            this();
        }

        private Companion() {
        }

        @TestOnly
        public final void enableAnnotator(@NotNull Class<? extends AnnotatorBase> cls, @NotNull Disposable parentDisposable) {
            Intrinsics.checkNotNullParameter(cls, "annotatorClass");
            Intrinsics.checkNotNullParameter(parentDisposable, "parentDisposable");
            AnnotatorBase.enabledAnnotators.add(cls);
            Disposer.register(parentDisposable, () -> {
                enableAnnotator$lambda$0(r1);
            });
        }

        private static final void enableAnnotator$lambda$0(Class $annotatorClass) {
            AnnotatorBase.enabledAnnotators.remove($annotatorClass);
        }
    }

    static {
        Set<Class<? extends AnnotatorBase>> createConcurrentSet = ConcurrentCollectionFactory.createConcurrentSet();
        Intrinsics.checkNotNullExpressionValue(createConcurrentSet, "createConcurrentSet(...)");
        enabledAnnotators = createConcurrentSet;
    }
}