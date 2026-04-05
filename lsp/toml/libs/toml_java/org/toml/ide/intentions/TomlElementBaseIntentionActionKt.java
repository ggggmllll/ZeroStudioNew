package org.toml.ide.intentions;

import com.intellij.openapi.application.ApplicationManager;
import kotlin.Metadata;
import org.toml.lang.lexer._TomlLexer;

/* compiled from: TomlElementBaseIntentionAction.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = 2, xi = 48, d1 = {"��\n\n��\n\u0002\u0010\u0002\n\u0002\b\u0002\u001a\b\u0010��\u001a\u00020\u0001H\u0002\u001a\b\u0010\u0002\u001a\u00020\u0001H\u0002¨\u0006\u0003"}, d2 = {"checkWriteAccessAllowed", "", "checkReadAccessAllowed", "intellij.toml.core"})
public final class TomlElementBaseIntentionActionKt {
    /* JADX INFO: Access modifiers changed from: private */
    public static final void checkWriteAccessAllowed() {
        if (!ApplicationManager.getApplication().isWriteAccessAllowed()) {
            throw new IllegalStateException("Needs write action".toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void checkReadAccessAllowed() {
        if (!ApplicationManager.getApplication().isReadAccessAllowed()) {
            throw new IllegalStateException("Needs read action".toString());
        }
    }
}