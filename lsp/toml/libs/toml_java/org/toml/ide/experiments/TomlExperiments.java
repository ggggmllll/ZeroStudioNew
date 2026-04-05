package org.toml.ide.experiments;

import com.intellij.openapi.util.registry.Registry;
import com.intellij.psi.tree.IElementType;
import kotlin.Metadata;
import org.jetbrains.annotations.NotNull;
import org.toml.lang.lexer._TomlLexer;

/* compiled from: TomlExperiments.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��\u001a\n\u0002\u0018\u0002\n\u0002\u0010��\n\u0002\b\u0003\n\u0002\u0010\u000e\n��\n\u0002\u0010\u000b\n\u0002\b\u0004\bÆ\u0002\u0018��2\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\t\u001a\u00020\u00072\u0006\u0010\n\u001a\u00020\u0005H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T¢\u0006\u0002\n��R\u0011\u0010\u0006\u001a\u00020\u00078F¢\u0006\u0006\u001a\u0004\b\u0006\u0010\b¨\u0006\u000b"}, d2 = {"Lorg/toml/ide/experiments/TomlExperiments;", "", "<init>", "()V", "JSON_SCHEMA", "", "isJsonSchemaEnabled", "", "()Z", "isFeatureEnabled", "registryKey", "intellij.toml.core"})
public final class TomlExperiments {
    @NotNull
    public static final TomlExperiments INSTANCE = new TomlExperiments();
    @NotNull
    public static final String JSON_SCHEMA = "org.toml.json.schema";

    private TomlExperiments() {
    }

    public final boolean isJsonSchemaEnabled() {
        return isFeatureEnabled(JSON_SCHEMA);
    }

    private final boolean isFeatureEnabled(String registryKey) {
        return Registry.Companion.get(registryKey).asBoolean();
    }
}