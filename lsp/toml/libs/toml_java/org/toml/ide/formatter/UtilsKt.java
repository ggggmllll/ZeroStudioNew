package org.toml.ide.formatter;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.toml.ide.formatter.settings.TomlCodeStyleSettings;
import org.toml.lang.lexer._TomlLexer;

/* compiled from: utils.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = 2, xi = 48, d1 = {"��\u000e\n��\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\"\u0015\u0010��\u001a\u00020\u0001*\u00020\u00028F¢\u0006\u0006\u001a\u0004\b\u0003\u0010\u0004¨\u0006\u0005"}, d2 = {"toml", "Lorg/toml/ide/formatter/settings/TomlCodeStyleSettings;", "Lcom/intellij/psi/codeStyle/CodeStyleSettings;", "getToml", "(Lcom/intellij/psi/codeStyle/CodeStyleSettings;)Lorg/toml/ide/formatter/settings/TomlCodeStyleSettings;", "intellij.toml.core"})
public final class UtilsKt {
    @NotNull
    public static final TomlCodeStyleSettings getToml(@NotNull CodeStyleSettings $this$toml) {
        Intrinsics.checkNotNullParameter($this$toml, "<this>");
        CustomCodeStyleSettings customSettings = $this$toml.getCustomSettings(TomlCodeStyleSettings.class);
        Intrinsics.checkNotNullExpressionValue(customSettings, "getCustomSettings(...)");
        return (TomlCodeStyleSettings) customSettings;
    }
}