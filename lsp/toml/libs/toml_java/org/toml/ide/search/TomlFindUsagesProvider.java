package org.toml.ide.search;

import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.lang.lexer._TomlLexer;

/* compiled from: TomlFindUsagesProvider.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n��\n\u0002\u0010\u000b\n��\n\u0002\u0018\u0002\n��\n\u0002\u0010\u000e\n\u0002\b\u0005\u0018��2\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\n\u0010\u0004\u001a\u0004\u0018\u00010\u0005H\u0016J\u0010\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH\u0016J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\b\u001a\u00020\tH\u0016J\u0010\u0010\f\u001a\u00020\u000b2\u0006\u0010\b\u001a\u00020\tH\u0016J\u0010\u0010\r\u001a\u00020\u000b2\u0006\u0010\b\u001a\u00020\tH\u0016J\u0018\u0010\u000e\u001a\u00020\u000b2\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u000f\u001a\u00020\u0007H\u0016¨\u0006\u0010"}, d2 = {"Lorg/toml/ide/search/TomlFindUsagesProvider;", "Lcom/intellij/lang/findUsages/FindUsagesProvider;", "<init>", "()V", "getWordsScanner", "Lcom/intellij/lang/cacheBuilder/WordsScanner;", "canFindUsagesFor", "", "element", "Lcom/intellij/psi/PsiElement;", "getHelpId", "", "getType", "getDescriptiveName", "getNodeText", "useFullName", "intellij.toml.core"})
public final class TomlFindUsagesProvider implements FindUsagesProvider {
    @Nullable
    public WordsScanner getWordsScanner() {
        return null;
    }

    public boolean canFindUsagesFor(@NotNull PsiElement element) {
        Intrinsics.checkNotNullParameter(element, "element");
        return false;
    }

    @NotNull
    public String getHelpId(@NotNull PsiElement element) {
        Intrinsics.checkNotNullParameter(element, "element");
        return "reference.dialogs.findUsages.other";
    }

    @NotNull
    public String getType(@NotNull PsiElement element) {
        Intrinsics.checkNotNullParameter(element, "element");
        return "";
    }

    @NotNull
    public String getDescriptiveName(@NotNull PsiElement element) {
        Intrinsics.checkNotNullParameter(element, "element");
        return "";
    }

    @NotNull
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        Intrinsics.checkNotNullParameter(element, "element");
        return "";
    }
}