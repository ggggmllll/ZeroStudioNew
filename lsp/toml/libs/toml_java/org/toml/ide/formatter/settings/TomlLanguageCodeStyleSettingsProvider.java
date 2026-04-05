package org.toml.ide.formatter.settings;

import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleConfigurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import com.intellij.psi.tree.IElementType;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.toml.lang.TomlLanguage;
import org.toml.lang.lexer._TomlLexer;

/* compiled from: TomlLanguageCodeStyleSettingsProvider.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��8\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\u0018��2\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\u0004\u001a\u00020\u0005H\u0016J\u0010\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH\u0016J\u0018\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\t2\u0006\u0010\r\u001a\u00020\tH\u0016J\u0010\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0016J\b\u0010\u0012\u001a\u00020\u0013H\u0016¨\u0006\u0014"}, d2 = {"Lorg/toml/ide/formatter/settings/TomlLanguageCodeStyleSettingsProvider;", "Lcom/intellij/psi/codeStyle/LanguageCodeStyleSettingsProvider;", "<init>", "()V", "getLanguage", "Lcom/intellij/lang/Language;", "createCustomSettings", "Lcom/intellij/psi/codeStyle/CustomCodeStyleSettings;", "settings", "Lcom/intellij/psi/codeStyle/CodeStyleSettings;", "createConfigurable", "Lcom/intellij/psi/codeStyle/CodeStyleConfigurable;", "baseSettings", "modelSettings", "getCodeSample", "", "settingsType", "Lcom/intellij/psi/codeStyle/LanguageCodeStyleSettingsProvider$SettingsType;", "getIndentOptionsEditor", "Lcom/intellij/application/options/IndentOptionsEditor;", "intellij.toml.core"})
public final class TomlLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {

    /* compiled from: TomlLanguageCodeStyleSettingsProvider.kt */
    @Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = 3, xi = 48)
    /* loaded from: 2026040502522302a7da3a-171e-4c6c-a1b5-134e29fd6e0f.jar:org/toml/ide/formatter/settings/TomlLanguageCodeStyleSettingsProvider$WhenMappings.class */
    public /* synthetic */ class WhenMappings {
        public static final /* synthetic */ int[] $EnumSwitchMapping$0;

        static {
            int[] iArr = new int[LanguageCodeStyleSettingsProvider.SettingsType.values().length];
            try {
                iArr[LanguageCodeStyleSettingsProvider.SettingsType.INDENT_SETTINGS.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            $EnumSwitchMapping$0 = iArr;
        }
    }

    @NotNull
    public Language getLanguage() {
        return TomlLanguage.INSTANCE;
    }

    @NotNull
    public CustomCodeStyleSettings createCustomSettings(@NotNull CodeStyleSettings settings) {
        Intrinsics.checkNotNullParameter(settings, "settings");
        return new TomlCodeStyleSettings(settings);
    }

    @NotNull
    public CodeStyleConfigurable createConfigurable(@NotNull final CodeStyleSettings baseSettings, @NotNull final CodeStyleSettings modelSettings) {
        Intrinsics.checkNotNullParameter(baseSettings, "baseSettings");
        Intrinsics.checkNotNullParameter(modelSettings, "modelSettings");
        final String configurableDisplayName = getConfigurableDisplayName();
        return new CodeStyleAbstractConfigurable(baseSettings, modelSettings, configurableDisplayName) { // from class: org.toml.ide.formatter.settings.TomlLanguageCodeStyleSettingsProvider$createConfigurable$1
            protected CodeStyleAbstractPanel createPanel(CodeStyleSettings settings) {
                Intrinsics.checkNotNullParameter(settings, "settings");
                CodeStyleSettings currentSettings = getCurrentSettings();
                Intrinsics.checkNotNullExpressionValue(currentSettings, "getCurrentSettings(...)");
                return new TomlCodeStyleMainPanel(currentSettings, settings);
            }
        };
    }

    @NotNull
    public String getCodeSample(@NotNull LanguageCodeStyleSettingsProvider.SettingsType settingsType) {
        String str;
        Intrinsics.checkNotNullParameter(settingsType, "settingsType");
        if (WhenMappings.$EnumSwitchMapping$0[settingsType.ordinal()] == 1) {
            str = TomlLanguageCodeStyleSettingsProviderKt.INDENT_SAMPLE;
            return str;
        }
        return "";
    }

    @NotNull
    public IndentOptionsEditor getIndentOptionsEditor() {
        return new SmartIndentOptionsEditor();
    }
}