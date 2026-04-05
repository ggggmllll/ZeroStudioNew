package org.toml.ide.formatter.settings;

import kotlin.Metadata;
import kotlin.text.StringsKt;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.toml.lang.lexer._TomlLexer;

/* compiled from: TomlLanguageCodeStyleSettingsProvider.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = 2, xi = 48, d1 = {"��\n\n��\n\u0002\u0010\u000e\n\u0002\b\u0003\u001a\u0012\u0010��\u001a\u00020\u00012\b\b\u0001\u0010\u0002\u001a\u00020\u0001H\u0002\"\u000e\u0010\u0003\u001a\u00020\u0001X\u0082\u0004¢\u0006\u0002\n��¨\u0006\u0004"}, d2 = {"sample", "", "code", "INDENT_SAMPLE", "intellij.toml.core"})
public final class TomlLanguageCodeStyleSettingsProviderKt {
    @NotNull
    private static final String INDENT_SAMPLE = sample("\n[config]\nitems = [\n    \"foo\",\n    \"bar\"\n]\n");

    private static final String sample(@Language("TOML") String code) {
        return StringsKt.trim(code).toString();
    }
}