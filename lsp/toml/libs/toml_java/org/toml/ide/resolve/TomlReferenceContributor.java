package org.toml.ide.resolve;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.tree.IElementType;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlLiteral;

/* compiled from: TomlReferenceContributor.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n��\n\u0002\u0018\u0002\n��\b��\u0018��2\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0016¨\u0006\b"}, d2 = {"Lorg/toml/ide/resolve/TomlReferenceContributor;", "Lcom/intellij/psi/PsiReferenceContributor;", "<init>", "()V", "registerReferenceProviders", "", "registrar", "Lcom/intellij/psi/PsiReferenceRegistrar;", "intellij.toml.core"})
public final class TomlReferenceContributor extends PsiReferenceContributor {
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        Intrinsics.checkNotNullParameter(registrar, "registrar");
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(TomlLiteral.class), new TomlWebReferenceProvider(), -100.0d);
    }
}