package org.toml.ide.json;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.jsonSchema.extension.adapters.JsonObjectValueAdapter;
import com.jetbrains.jsonSchema.extension.adapters.JsonPropertyAdapter;
import com.jetbrains.jsonSchema.extension.adapters.JsonValueAdapter;
import java.util.Collection;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlKeySegment;
import org.toml.lang.psi.TomlKeyValue;
import org.toml.lang.psi.TomlKeyValueOwner;
import org.toml.lang.psi.TomlValue;

/* compiled from: TomlJsonPropertyAdapter.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��0\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0010\u001e\n��\n\u0002\u0018\u0002\n��\u0018��2\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0004\b\u0004\u0010\u0005J\b\u0010\u0006\u001a\u00020\u0007H\u0016J\b\u0010\b\u001a\u00020\tH\u0016J\b\u0010\n\u001a\u00020\u000bH\u0016J\u000e\u0010\f\u001a\b\u0012\u0004\u0012\u00020\t0\rH\u0016J\n\u0010\u000e\u001a\u0004\u0018\u00010\u000fH\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n��¨\u0006\u0010"}, d2 = {"Lorg/toml/ide/json/TomlJsonPropertyAdapter;", "Lcom/jetbrains/jsonSchema/extension/adapters/JsonPropertyAdapter;", "keyValue", "Lorg/toml/lang/psi/TomlKeyValue;", "<init>", "(Lorg/toml/lang/psi/TomlKeyValue;)V", "getName", "", "getNameValueAdapter", "Lcom/jetbrains/jsonSchema/extension/adapters/JsonValueAdapter;", "getDelegate", "Lcom/intellij/psi/PsiElement;", "getValues", "", "getParentObject", "Lcom/jetbrains/jsonSchema/extension/adapters/JsonObjectValueAdapter;", "intellij.toml.json"})
public final class TomlJsonPropertyAdapter implements JsonPropertyAdapter {
    @NotNull
    private final TomlKeyValue keyValue;

    public TomlJsonPropertyAdapter(@NotNull TomlKeyValue keyValue) {
        Intrinsics.checkNotNullParameter(keyValue, "keyValue");
        this.keyValue = keyValue;
    }

    @NotNull
    public String getName() {
        TomlKeySegment tomlKeySegment = (TomlKeySegment) CollectionsKt.lastOrNull(this.keyValue.getKey().getSegments());
        if (tomlKeySegment != null) {
            String name = tomlKeySegment.getName();
            if (name != null) {
                return name;
            }
        }
        return "";
    }

    @NotNull
    public JsonValueAdapter getNameValueAdapter() {
        return new TomlJsonGenericValueAdapter(this.keyValue.getKey());
    }

    @NotNull
    public PsiElement getDelegate() {
        return this.keyValue;
    }

    @NotNull
    public Collection<JsonValueAdapter> getValues() {
        TomlValue value = this.keyValue.getValue();
        return value == null ? CollectionsKt.emptyList() : CollectionsKt.listOf(TomlJsonValueAdapter.Companion.createAdapterByType(value));
    }

    @Nullable
    public JsonObjectValueAdapter getParentObject() {
        PsiElement parent = this.keyValue.getParent();
        if (parent instanceof TomlKeyValueOwner) {
            return new TomlJsonObjectAdapter((TomlKeyValueOwner) parent);
        }
        return null;
    }
}