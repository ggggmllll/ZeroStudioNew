package org.toml.ide;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.StringEscapesTokenTypes;
import com.intellij.psi.tree.IElementType;
import java.util.HashMap;
import java.util.Map;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.toml.ide.colors.TomlColor;
import org.toml.lang.lexer.TomlHighlightingLexer;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlElementTypes;

/* compiled from: TomlHighlighter.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n��\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\u0018\u0002\n��\u0018��2\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\u0004\u001a\u00020\u0005H\u0016J\u001d\u0010\u0006\u001a\n\u0012\u0006\b\u0001\u0012\u00020\b0\u00072\u0006\u0010\t\u001a\u00020\nH\u0016¢\u0006\u0002\u0010\u000bR\u001a\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u000e0\rX\u0082\u0004¢\u0006\u0002\n��¨\u0006\u000f"}, d2 = {"Lorg/toml/ide/TomlHighlighter;", "Lcom/intellij/openapi/fileTypes/SyntaxHighlighterBase;", "<init>", "()V", "getHighlightingLexer", "Lcom/intellij/lexer/Lexer;", "getTokenHighlights", "", "Lcom/intellij/openapi/editor/colors/TextAttributesKey;", "tokenType", "Lcom/intellij/psi/tree/IElementType;", "(Lcom/intellij/psi/tree/IElementType;)[Lcom/intellij/openapi/editor/colors/TextAttributesKey;", "tokenMap", "", "Lorg/toml/ide/colors/TomlColor;", "intellij.toml.core"})
public final class TomlHighlighter extends SyntaxHighlighterBase {
    @NotNull
    private final Map<IElementType, TomlColor> tokenMap;

    public TomlHighlighter() {
        HashMap $this$tokenMap_u24lambda_u240 = new HashMap();
        $this$tokenMap_u24lambda_u240.put(TomlElementTypes.BARE_KEY, TomlColor.KEY);
        $this$tokenMap_u24lambda_u240.put(TomlElementTypes.COMMENT, TomlColor.COMMENT);
        $this$tokenMap_u24lambda_u240.put(TomlElementTypes.BASIC_STRING, TomlColor.STRING);
        $this$tokenMap_u24lambda_u240.put(TomlElementTypes.LITERAL_STRING, TomlColor.STRING);
        $this$tokenMap_u24lambda_u240.put(TomlElementTypes.MULTILINE_BASIC_STRING, TomlColor.STRING);
        $this$tokenMap_u24lambda_u240.put(TomlElementTypes.MULTILINE_LITERAL_STRING, TomlColor.STRING);
        $this$tokenMap_u24lambda_u240.put(TomlElementTypes.NUMBER, TomlColor.NUMBER);
        $this$tokenMap_u24lambda_u240.put(TomlElementTypes.BOOLEAN, TomlColor.BOOLEAN);
        $this$tokenMap_u24lambda_u240.put(TomlElementTypes.DATE_TIME, TomlColor.DATE);
        $this$tokenMap_u24lambda_u240.put(StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN, TomlColor.INVALID_STRING_ESCAPE);
        $this$tokenMap_u24lambda_u240.put(StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN, TomlColor.INVALID_STRING_ESCAPE);
        $this$tokenMap_u24lambda_u240.put(StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN, TomlColor.VALID_STRING_ESCAPE);
        this.tokenMap = $this$tokenMap_u24lambda_u240;
    }

    @NotNull
    public Lexer getHighlightingLexer() {
        return new TomlHighlightingLexer();
    }

    @NotNull
    public TextAttributesKey[] getTokenHighlights(@NotNull IElementType tokenType) {
        Intrinsics.checkNotNullParameter(tokenType, "tokenType");
        TomlColor tomlColor = this.tokenMap.get(tokenType);
        TextAttributesKey[] pack = SyntaxHighlighterBase.pack(tomlColor != null ? tomlColor.getTextAttributesKey() : null);
        Intrinsics.checkNotNullExpressionValue(pack, "pack(...)");
        return pack;
    }
}