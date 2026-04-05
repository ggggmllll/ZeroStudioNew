package org.toml.ide;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.lang.TomlLanguage;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlElementTypes;

/* compiled from: TomlBraceMatcher.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��4\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n��\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n��\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018�� \u00122\u00020\u0001:\u0001\u0012B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0013\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u0016¢\u0006\u0002\u0010\u0007J\u001a\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\u000bH\u0016J\u0018\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u000eH\u0016¨\u0006\u0013"}, d2 = {"Lorg/toml/ide/TomlBraceMatcher;", "Lcom/intellij/lang/PairedBraceMatcher;", "<init>", "()V", "getPairs", "", "Lcom/intellij/lang/BracePair;", "()[Lcom/intellij/lang/BracePair;", "isPairedBracesAllowedBeforeType", "", "lbraceType", "Lcom/intellij/psi/tree/IElementType;", "contextType", "getCodeConstructStart", "", "file", "Lcom/intellij/psi/PsiFile;", "openingBraceOffset", "Companion", "intellij.toml.core"})
public final class TomlBraceMatcher implements PairedBraceMatcher {
    @NotNull
    public static final Companion Companion = new Companion(null);
    @NotNull
    private static final BracePair[] PAIRS = {new BracePair(new IElementType("FAKE_L_BRACE", TomlLanguage.INSTANCE), new IElementType("FAKE_L_BRACE", TomlLanguage.INSTANCE), false), new BracePair(TomlElementTypes.L_CURLY, TomlElementTypes.R_CURLY, false), new BracePair(TomlElementTypes.L_BRACKET, TomlElementTypes.R_BRACKET, false)};

    @NotNull
    public BracePair[] getPairs() {
        return PAIRS;
    }

    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
        Intrinsics.checkNotNullParameter(lbraceType, "lbraceType");
        return true;
    }

    public int getCodeConstructStart(@NotNull PsiFile file, int openingBraceOffset) {
        Intrinsics.checkNotNullParameter(file, "file");
        return openingBraceOffset;
    }

    /* compiled from: TomlBraceMatcher.kt */
    @Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��\u0018\n\u0002\u0018\u0002\n\u0002\u0010��\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018��2\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003R\u0016\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005X\u0082\u0004¢\u0006\u0004\n\u0002\u0010\u0007¨\u0006\b"}, d2 = {"Lorg/toml/ide/TomlBraceMatcher$Companion;", "", "<init>", "()V", "PAIRS", "", "Lcom/intellij/lang/BracePair;", "[Lcom/intellij/lang/BracePair;", "intellij.toml.core"})
    /* loaded from: 2026040502522302a7da3a-171e-4c6c-a1b5-134e29fd6e0f.jar:org/toml/ide/TomlBraceMatcher$Companion.class */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker $constructor_marker) {
            this();
        }

        private Companion() {
        }
    }
}