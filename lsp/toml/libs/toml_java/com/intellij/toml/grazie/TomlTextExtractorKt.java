package com.intellij.toml.grazie;

import com.intellij.grazie.text.TextContentBuilder;
import kotlin.Metadata;
import org.toml.lang.lexer._TomlLexer;

/* compiled from: TomlTextExtractor.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = 2, xi = 48, d1 = {"��\n\n��\n\u0002\u0018\u0002\n\u0002\b\u0003\"\u0018\u0010��\u001a\n \u0002*\u0004\u0018\u00010\u00010\u0001X\u0082\u0004¢\u0006\u0004\n\u0002\u0010\u0003¨\u0006\u0004"}, d2 = {"COMMENT_BUILDER", "Lcom/intellij/grazie/text/TextContentBuilder;", "kotlin.jvm.PlatformType", "Lcom/intellij/grazie/text/TextContentBuilder;", "intellij.toml.grazie"})
public final class TomlTextExtractorKt {
    private static final TextContentBuilder COMMENT_BUILDER = TextContentBuilder.FromPsi.removingIndents(" \t").removingLineSuffixes(" \t");
}