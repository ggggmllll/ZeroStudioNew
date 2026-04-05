package org.toml.ide.wordSelection;

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandlerBase;
import com.intellij.codeInsight.editorActions.SelectWordUtil;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import java.util.ArrayList;
import java.util.List;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.lang.lexer.TomlEscapeLexer;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.ElementTypesKt;
import org.toml.lang.psi.ext.PsiElementKt;
import org.toml.lang.psi.ext.TomlLiteralKind;

/* compiled from: TomlStringLiteralSelectionHandler.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��4\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n��\n\u0002\u0018\u0002\n��\n\u0002\u0010 \n\u0002\u0018\u0002\n��\n\u0002\u0010\r\n��\n\u0002\u0010\b\n��\n\u0002\u0018\u0002\n��\u0018��2\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0016J0\u0010\b\u001a\n\u0012\u0004\u0012\u00020\n\u0018\u00010\t2\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010H\u0016¨\u0006\u0011"}, d2 = {"Lorg/toml/ide/wordSelection/TomlStringLiteralSelectionHandler;", "Lcom/intellij/codeInsight/editorActions/ExtendWordSelectionHandlerBase;", "<init>", "()V", "canSelect", "", "e", "Lcom/intellij/psi/PsiElement;", "select", "", "Lcom/intellij/openapi/util/TextRange;", "editorText", "", "cursorOffset", "", "editor", "Lcom/intellij/openapi/editor/Editor;", "intellij.toml.core"})
public final class TomlStringLiteralSelectionHandler extends ExtendWordSelectionHandlerBase {
    public boolean canSelect(@NotNull PsiElement e) {
        Intrinsics.checkNotNullParameter(e, "e");
        return ElementTypesKt.getTOML_STRING_LITERALS().contains(PsiElementKt.getElementType(e));
    }

    @Nullable
    public List<TextRange> select(@NotNull PsiElement e, @NotNull CharSequence editorText, int cursorOffset, @NotNull Editor editor) {
        TextRange valueRange;
        Intrinsics.checkNotNullParameter(e, "e");
        Intrinsics.checkNotNullParameter(editorText, "editorText");
        Intrinsics.checkNotNullParameter(editor, "editor");
        TomlLiteralKind.Companion companion = TomlLiteralKind.Companion;
        ASTNode node = e.getNode();
        Intrinsics.checkNotNullExpressionValue(node, "getNode(...)");
        TomlLiteralKind fromAstNode = companion.fromAstNode(node);
        TomlLiteralKind.String string = fromAstNode instanceof TomlLiteralKind.String ? (TomlLiteralKind.String) fromAstNode : null;
        if (string == null) {
            return null;
        }
        TomlLiteralKind.String kind = string;
        TextRange value = kind.getOffsets().getValue();
        if (value == null || (valueRange = value.shiftRight(kind.getNode().getStartOffset())) == null) {
            return null;
        }
        List select = super.select(e, editorText, cursorOffset, editor);
        if (select == null) {
            select = new ArrayList();
        }
        List result = select;
        IElementType elementType = PsiElementKt.getElementType(e);
        if (TomlEscapeLexer.Companion.getESCAPABLE_LITERALS_TOKEN_SET().contains(elementType)) {
            SelectWordUtil.addWordHonoringEscapeSequences(editorText, valueRange, cursorOffset, TomlEscapeLexer.Companion.of(elementType), result);
        }
        result.add(valueRange);
        return result;
    }
}