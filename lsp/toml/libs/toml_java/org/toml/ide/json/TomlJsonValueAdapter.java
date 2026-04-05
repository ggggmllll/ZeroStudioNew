package org.toml.ide.json;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.jsonSchema.extension.adapters.JsonArrayValueAdapter;
import com.jetbrains.jsonSchema.extension.adapters.JsonObjectValueAdapter;
import com.jetbrains.jsonSchema.extension.adapters.JsonValueAdapter;
import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlArray;
import org.toml.lang.psi.TomlElement;
import org.toml.lang.psi.TomlKeyValueOwner;

/* compiled from: TomlJsonValueAdapter.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��:\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n��\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\b6\u0018�� \u0017*\b\b��\u0010\u0001*\u00020\u00022\u00020\u0003:\u0001\u0017B\u0011\b\u0004\u0012\u0006\u0010\u0004\u001a\u00028��¢\u0006\u0004\b\u0005\u0010\u0006J\u0006\u0010\n\u001a\u00020\u000bJ\b\u0010\f\u001a\u00020\rH\u0016J\b\u0010\u000e\u001a\u00020\rH\u0016J\b\u0010\u000f\u001a\u00020\rH\u0016J\b\u0010\u0010\u001a\u00020\rH\u0016J\b\u0010\u0011\u001a\u00020\rH\u0016J\b\u0010\u0012\u001a\u00020\rH\u0016J\n\u0010\u0013\u001a\u0004\u0018\u00010\u0014H\u0016J\n\u0010\u0015\u001a\u0004\u0018\u00010\u0016H\u0016R\u0016\u0010\u0004\u001a\u00028��X\u0084\u0004¢\u0006\n\n\u0002\u0010\t\u001a\u0004\b\u0007\u0010\b\u0082\u0001\u0003\u0018\u0019\u001a¨\u0006\u001b"}, d2 = {"Lorg/toml/ide/json/TomlJsonValueAdapter;", "T", "Lorg/toml/lang/psi/TomlElement;", "Lcom/jetbrains/jsonSchema/extension/adapters/JsonValueAdapter;", "element", "<init>", "(Lorg/toml/lang/psi/TomlElement;)V", "getElement", "()Lorg/toml/lang/psi/TomlElement;", "Lorg/toml/lang/psi/TomlElement;", "getDelegate", "Lcom/intellij/psi/PsiElement;", "isObject", "", "isArray", "isNull", "isStringLiteral", "isNumberLiteral", "isBooleanLiteral", "getAsObject", "Lcom/jetbrains/jsonSchema/extension/adapters/JsonObjectValueAdapter;", "getAsArray", "Lcom/jetbrains/jsonSchema/extension/adapters/JsonArrayValueAdapter;", "Companion", "Lorg/toml/ide/json/TomlJsonArrayAdapter;", "Lorg/toml/ide/json/TomlJsonGenericValueAdapter;", "Lorg/toml/ide/json/TomlJsonObjectAdapter;", "intellij.toml.json"})
public abstract class TomlJsonValueAdapter<T extends TomlElement> implements JsonValueAdapter {
    @NotNull
    public static final Companion Companion = new Companion(null);
    @NotNull
    private final T element;

    public /* synthetic */ TomlJsonValueAdapter(TomlElement element, DefaultConstructorMarker $constructor_marker) {
        this(element);
    }

    private TomlJsonValueAdapter(T t) {
        this.element = t;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @NotNull
    public final T getElement() {
        return this.element;
    }

    @NotNull
    public final PsiElement getDelegate() {
        return this.element;
    }

    public boolean isObject() {
        return false;
    }

    public boolean isArray() {
        return false;
    }

    public boolean isNull() {
        return false;
    }

    public boolean isStringLiteral() {
        return false;
    }

    public boolean isNumberLiteral() {
        return false;
    }

    public boolean isBooleanLiteral() {
        return false;
    }

    @Nullable
    public JsonObjectValueAdapter getAsObject() {
        return null;
    }

    @Nullable
    public JsonArrayValueAdapter getAsArray() {
        return null;
    }

    /* compiled from: TomlJsonValueAdapter.kt */
    @Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��\u0018\n\u0002\u0018\u0002\n\u0002\u0010��\n\u0002\b\u0003\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\b\u0086\u0003\u0018��2\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u0012\u0010\u0004\u001a\u0006\u0012\u0002\b\u00030\u00052\u0006\u0010\u0006\u001a\u00020\u0007¨\u0006\b"}, d2 = {"Lorg/toml/ide/json/TomlJsonValueAdapter$Companion;", "", "<init>", "()V", "createAdapterByType", "Lorg/toml/ide/json/TomlJsonValueAdapter;", "value", "Lorg/toml/lang/psi/TomlElement;", "intellij.toml.json"})
    /* loaded from: 2026040502522302a7da3a-171e-4c6c-a1b5-134e29fd6e0f.jar:org/toml/ide/json/TomlJsonValueAdapter$Companion.class */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker $constructor_marker) {
            this();
        }

        private Companion() {
        }

        @NotNull
        public final TomlJsonValueAdapter<?> createAdapterByType(@NotNull TomlElement value) {
            Intrinsics.checkNotNullParameter(value, "value");
            return value instanceof TomlKeyValueOwner ? new TomlJsonObjectAdapter((TomlKeyValueOwner) value) : value instanceof TomlArray ? new TomlJsonArrayAdapter((TomlArray) value) : new TomlJsonGenericValueAdapter(value);
        }
    }
}