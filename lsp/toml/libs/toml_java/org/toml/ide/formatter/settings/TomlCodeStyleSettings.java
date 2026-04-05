package org.toml.ide.formatter.settings;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.toml.lang.lexer._TomlLexer;

/* compiled from: TomlCodeStyleSettings.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018��2\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0004\b\u0004\u0010\u0005¨\u0006\u0006"}, d2 = {"Lorg/toml/ide/formatter/settings/TomlCodeStyleSettings;", "Lcom/intellij/psi/codeStyle/CustomCodeStyleSettings;", "container", "Lcom/intellij/psi/codeStyle/CodeStyleSettings;", "<init>", "(Lcom/intellij/psi/codeStyle/CodeStyleSettings;)V", "intellij.toml.core"})
public final class TomlCodeStyleSettings extends CustomCodeStyleSettings {
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public TomlCodeStyleSettings(@NotNull CodeStyleSettings container) {
        super(TomlCodeStyleSettings.class.getSimpleName(), container);
        Intrinsics.checkNotNullParameter(container, "container");
    }
}