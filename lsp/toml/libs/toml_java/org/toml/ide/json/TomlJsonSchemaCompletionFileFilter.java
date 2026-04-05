package org.toml.ide.json;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import java.util.Collection;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import org.jetbrains.annotations.NotNull;
import org.toml.lang.lexer._TomlLexer;

/* compiled from: TomlJsonSchemaCompletionFileFilter.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��\u0018\n\u0002\u0018\u0002\n\u0002\u0010��\n��\n\u0002\u0010\u000b\n��\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018�� \u00062\u00020\u0001:\u0001\u0006J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&ø\u0001��\u0082\u0002\u0006\n\u0004\b!0\u0001¨\u0006\u0007À\u0006\u0001"}, d2 = {"Lorg/toml/ide/json/TomlJsonSchemaCompletionFileFilter;", "", "shouldCompleteInFile", "", "file", "Lcom/intellij/psi/PsiFile;", "Companion", "intellij.toml.json"})
public interface TomlJsonSchemaCompletionFileFilter {
    @NotNull
    public static final Companion Companion = Companion.$$INSTANCE;

    boolean shouldCompleteInFile(@NotNull PsiFile psiFile);

    /* compiled from: TomlJsonSchemaCompletionFileFilter.kt */
    @Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��$\n\u0002\u0018\u0002\n\u0002\u0010��\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n��\n\u0002\u0018\u0002\n��\b\u0086\u0003\u0018��2\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fR\u0017\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005¢\u0006\b\n��\u001a\u0004\b\u0007\u0010\b¨\u0006\r"}, d2 = {"Lorg/toml/ide/json/TomlJsonSchemaCompletionFileFilter$Companion;", "", "<init>", "()V", "EP_NAME", "Lcom/intellij/openapi/extensions/ExtensionPointName;", "Lorg/toml/ide/json/TomlJsonSchemaCompletionFileFilter;", "getEP_NAME", "()Lcom/intellij/openapi/extensions/ExtensionPointName;", "shouldCompleteInFile", "", "file", "Lcom/intellij/psi/PsiFile;", "intellij.toml.json"})
    @SourceDebugExtension({"SMAP\nTomlJsonSchemaCompletionFileFilter.kt\nKotlin\n*S Kotlin\n*F\n+ 1 TomlJsonSchemaCompletionFileFilter.kt\norg/toml/ide/json/TomlJsonSchemaCompletionFileFilter$Companion\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,14:1\n1734#2,3:15\n*S KotlinDebug\n*F\n+ 1 TomlJsonSchemaCompletionFileFilter.kt\norg/toml/ide/json/TomlJsonSchemaCompletionFileFilter$Companion\n*L\n10#1:15,3\n*E\n"})
    /* loaded from: 2026040502522302a7da3a-171e-4c6c-a1b5-134e29fd6e0f.jar:org/toml/ide/json/TomlJsonSchemaCompletionFileFilter$Companion.class */
    public static final class Companion {
        static final /* synthetic */ Companion $$INSTANCE = new Companion();
        @NotNull
        private static final ExtensionPointName<TomlJsonSchemaCompletionFileFilter> EP_NAME = ExtensionPointName.Companion.create("org.toml.ide.json.tomlJsonSchemaCompletionFileFilter");

        private Companion() {
        }

        @NotNull
        public final ExtensionPointName<TomlJsonSchemaCompletionFileFilter> getEP_NAME() {
            return EP_NAME;
        }

        public final boolean shouldCompleteInFile(@NotNull PsiFile file) {
            Intrinsics.checkNotNullParameter(file, "file");
            Iterable $this$all$iv = EP_NAME.getExtensionsIfPointIsRegistered();
            if (($this$all$iv instanceof Collection) && ((Collection) $this$all$iv).isEmpty()) {
                return true;
            }
            for (Object element$iv : $this$all$iv) {
                TomlJsonSchemaCompletionFileFilter it = (TomlJsonSchemaCompletionFileFilter) element$iv;
                if (!it.shouldCompleteInFile(file)) {
                    return false;
                }
            }
            return true;
        }
    }
}