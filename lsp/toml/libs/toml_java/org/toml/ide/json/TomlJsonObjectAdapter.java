package org.toml.ide.json;

import com.intellij.psi.tree.IElementType;
import com.jetbrains.jsonSchema.extension.adapters.JsonObjectValueAdapter;
import com.jetbrains.jsonSchema.extension.adapters.JsonPropertyAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import kotlin.Lazy;
import kotlin.LazyKt;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import org.jetbrains.annotations.NotNull;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlKeyValue;
import org.toml.lang.psi.TomlKeyValueOwner;

/* compiled from: TomlJsonValueAdapter.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n��\u0018��2\b\u0012\u0004\u0012\u00020\u00020\u00012\u00020\u0003B\u000f\u0012\u0006\u0010\u0004\u001a\u00020\u0002¢\u0006\u0004\b\u0005\u0010\u0006J\b\u0010\u000e\u001a\u00020\u000fH\u0016J\b\u0010\u0010\u001a\u00020\u000fH\u0016J\b\u0010\u0011\u001a\u00020\u0003H\u0016J\u000e\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00130\bH\u0016R!\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b8BX\u0082\u0084\u0002¢\u0006\f\n\u0004\b\f\u0010\r\u001a\u0004\b\n\u0010\u000b¨\u0006\u0014"}, d2 = {"Lorg/toml/ide/json/TomlJsonObjectAdapter;", "Lorg/toml/ide/json/TomlJsonValueAdapter;", "Lorg/toml/lang/psi/TomlKeyValueOwner;", "Lcom/jetbrains/jsonSchema/extension/adapters/JsonObjectValueAdapter;", "keyValueOwner", "<init>", "(Lorg/toml/lang/psi/TomlKeyValueOwner;)V", "childAdapters", "", "Lorg/toml/ide/json/TomlJsonPropertyAdapter;", "getChildAdapters", "()Ljava/util/List;", "childAdapters$delegate", "Lkotlin/Lazy;", "isObject", "", "isNull", "getAsObject", "getPropertyList", "Lcom/jetbrains/jsonSchema/extension/adapters/JsonPropertyAdapter;", "intellij.toml.json"})
@SourceDebugExtension({"SMAP\nTomlJsonValueAdapter.kt\nKotlin\n*S Kotlin\n*F\n+ 1 TomlJsonValueAdapter.kt\norg/toml/ide/json/TomlJsonObjectAdapter\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,70:1\n1557#2:71\n1628#2,3:72\n*S KotlinDebug\n*F\n+ 1 TomlJsonValueAdapter.kt\norg/toml/ide/json/TomlJsonObjectAdapter\n*L\n61#1:71\n61#1:72,3\n*E\n"})
public final class TomlJsonObjectAdapter extends TomlJsonValueAdapter<TomlKeyValueOwner> implements JsonObjectValueAdapter {
    @NotNull
    private final Lazy childAdapters$delegate;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public TomlJsonObjectAdapter(@NotNull TomlKeyValueOwner keyValueOwner) {
        super(keyValueOwner, null);
        Intrinsics.checkNotNullParameter(keyValueOwner, "keyValueOwner");
        this.childAdapters$delegate = LazyKt.lazy(() -> {
            return childAdapters_delegate$lambda$1(r1);
        });
    }

    private final List<TomlJsonPropertyAdapter> getChildAdapters() {
        return (List) this.childAdapters$delegate.getValue();
    }

    private static final List childAdapters_delegate$lambda$1(TomlJsonObjectAdapter this$0) {
        Iterable $this$map$iv = this$0.getElement().getEntries();
        Collection destination$iv$iv = new ArrayList(CollectionsKt.collectionSizeOrDefault($this$map$iv, 10));
        for (Object item$iv$iv : $this$map$iv) {
            TomlKeyValue it = (TomlKeyValue) item$iv$iv;
            destination$iv$iv.add(new TomlJsonPropertyAdapter(it));
        }
        return (List) destination$iv$iv;
    }

    @Override // org.toml.ide.json.TomlJsonValueAdapter
    public boolean isObject() {
        return true;
    }

    @Override // org.toml.ide.json.TomlJsonValueAdapter
    public boolean isNull() {
        return false;
    }

    @Override // org.toml.ide.json.TomlJsonValueAdapter
    @NotNull
    public JsonObjectValueAdapter getAsObject() {
        return this;
    }

    @NotNull
    public List<JsonPropertyAdapter> getPropertyList() {
        return getChildAdapters();
    }
}