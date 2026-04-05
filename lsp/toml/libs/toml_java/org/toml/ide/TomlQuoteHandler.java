package org.toml.ide;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.intellij.psi.tree.IElementType;
import kotlin.Metadata;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlElementTypes;

/* compiled from: TomlQuoteHandler.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018��2\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003¨\u0006\u0004"}, d2 = {"Lorg/toml/ide/TomlQuoteHandler;", "Lcom/intellij/codeInsight/editorActions/SimpleTokenSetQuoteHandler;", "<init>", "()V", "intellij.toml.core"})
public final class TomlQuoteHandler extends SimpleTokenSetQuoteHandler {
    public TomlQuoteHandler() {
        super(new IElementType[]{TomlElementTypes.BASIC_STRING, TomlElementTypes.LITERAL_STRING});
    }
}