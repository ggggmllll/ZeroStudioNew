package com.intellij.psi;

import com.intellij.lang.Language;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;

public interface TokenType {
    public static final IElementType WHITE_SPACE = new IElementType("WHITE_SPACE", Language.ANY);
    public static final IElementType BAD_CHARACTER = new IElementType("BAD_CHARACTER", Language.ANY);
    public static final IElementType NEW_LINE_INDENT = new IElementType("NEW_LINE_INDENT", Language.ANY);
    public static final IElementType ERROR_ELEMENT = new IElementType("ERROR_ELEMENT", Language.ANY) { // from class: com.intellij.psi.TokenType.1
        @Override // com.intellij.psi.tree.IElementType
        public boolean isLeftBound() {
            return true;
        }
    };
    public static final IElementType CODE_FRAGMENT = new IFileElementType("CODE_FRAGMENT", Language.ANY);
    public static final IElementType DUMMY_HOLDER = new IFileElementType("DUMMY_HOLDER", Language.ANY);
}