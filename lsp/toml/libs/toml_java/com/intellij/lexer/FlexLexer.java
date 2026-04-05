package com.intellij.lexer;

import com.intellij.psi.tree.IElementType;
import java.io.IOException;
import org.jetbrains.annotations.NonNls;

@NonNls
public interface FlexLexer {
    void yybegin(int i);

    int yystate();

    int getTokenStart();

    int getTokenEnd();

    IElementType advance() throws IOException;

    void reset(CharSequence charSequence, int i, int i2, int i3);
}