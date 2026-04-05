package org.toml.ide.formatter.settings;

import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.toml.lang.TomlLanguage;
import org.toml.lang.lexer._TomlLexer;

/* compiled from: TomlCodeStyleMainPanel.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n��\b��\u0018��2\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003¢\u0006\u0004\b\u0005\u0010\u0006J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0004\u001a\u00020\u0003H\u0014¨\u0006\t"}, d2 = {"Lorg/toml/ide/formatter/settings/TomlCodeStyleMainPanel;", "Lcom/intellij/application/options/TabbedLanguageCodeStylePanel;", "currentSettings", "Lcom/intellij/psi/codeStyle/CodeStyleSettings;", "settings", "<init>", "(Lcom/intellij/psi/codeStyle/CodeStyleSettings;Lcom/intellij/psi/codeStyle/CodeStyleSettings;)V", "initTabs", "", "intellij.toml.core"})
public final class TomlCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public TomlCodeStyleMainPanel(@NotNull CodeStyleSettings currentSettings, @NotNull CodeStyleSettings settings) {
        super(TomlLanguage.INSTANCE, currentSettings, settings);
        Intrinsics.checkNotNullParameter(currentSettings, "currentSettings");
        Intrinsics.checkNotNullParameter(settings, "settings");
    }

    protected void initTabs(@NotNull CodeStyleSettings settings) {
        Intrinsics.checkNotNullParameter(settings, "settings");
        addIndentOptionsTab(settings);
    }
}