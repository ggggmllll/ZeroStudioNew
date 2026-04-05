package org.toml.ide;

import com.intellij.lang.Commenter;
import com.intellij.psi.tree.IElementType;
import kotlin.Metadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.lang.lexer._TomlLexer;

/* compiled from: TomlCommenter.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0005\u0018��2\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\u0004\u001a\u00020\u0005H\u0016J\n\u0010\u0006\u001a\u0004\u0018\u00010\u0005H\u0016J\n\u0010\u0007\u001a\u0004\u0018\u00010\u0005H\u0016J\n\u0010\b\u001a\u0004\u0018\u00010\u0005H\u0016J\n\u0010\t\u001a\u0004\u0018\u00010\u0005H\u0016¨\u0006\n"}, d2 = {"Lorg/toml/ide/TomlCommenter;", "Lcom/intellij/lang/Commenter;", "<init>", "()V", "getLineCommentPrefix", "", "getBlockCommentPrefix", "getBlockCommentSuffix", "getCommentedBlockCommentPrefix", "getCommentedBlockCommentSuffix", "intellij.toml.core"})
public final class TomlCommenter implements Commenter {
    @NotNull
    public String getLineCommentPrefix() {
        return "#";
    }

    @Nullable
    public String getBlockCommentPrefix() {
        return null;
    }

    @Nullable
    public String getBlockCommentSuffix() {
        return null;
    }

    @Nullable
    public String getCommentedBlockCommentPrefix() {
        return null;
    }

    @Nullable
    public String getCommentedBlockCommentSuffix() {
        return null;
    }
}