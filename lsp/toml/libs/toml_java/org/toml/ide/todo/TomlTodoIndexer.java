package org.toml.ide.todo;

import com.intellij.lexer.Lexer;
import com.intellij.psi.impl.cache.impl.BaseFilterLexer;
import com.intellij.psi.impl.cache.impl.OccurrenceConsumer;
import com.intellij.psi.impl.cache.impl.todo.LexerBasedTodoIndexer;
import com.intellij.psi.tree.IElementType;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.toml.lang.lexer.TomlLexer;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.ElementTypesKt;

/* compiled from: TomlTodoIndexer.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\b\u0002\u0018��2\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0016¨\u0006\b"}, d2 = {"Lorg/toml/ide/todo/TomlTodoIndexer;", "Lcom/intellij/psi/impl/cache/impl/todo/LexerBasedTodoIndexer;", "<init>", "()V", "createLexer", "Lcom/intellij/lexer/Lexer;", "consumer", "Lcom/intellij/psi/impl/cache/impl/OccurrenceConsumer;", "intellij.toml.core"})
final class TomlTodoIndexer extends LexerBasedTodoIndexer {
    @NotNull
    public Lexer createLexer(@NotNull final OccurrenceConsumer consumer) {
        Intrinsics.checkNotNullParameter(consumer, "consumer");
        final TomlLexer tomlLexer = new TomlLexer();
        return new BaseFilterLexer(consumer, tomlLexer) { // from class: org.toml.ide.todo.TomlTodoIndexer$createLexer$1
            /* JADX INFO: Access modifiers changed from: package-private */
            {
                Lexer lexer = (Lexer) tomlLexer;
            }

            public void advance() {
                if (ElementTypesKt.getTOML_COMMENTS().contains(this.myDelegate.getTokenType())) {
                    scanWordsInToken(2, false, false);
                    advanceTodoItemCountsInToken();
                }
                this.myDelegate.advance();
            }
        };
    }
}