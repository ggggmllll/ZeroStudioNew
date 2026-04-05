package org.toml.ide.json;

import com.intellij.psi.tree.IElementType;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlElement;
import org.toml.lang.psi.TomlKey;
import org.toml.lang.psi.TomlLiteral;
import org.toml.lang.psi.ext.TomlLiteralKind;
import org.toml.lang.psi.ext.TomlLiteralKt;

/* compiled from: TomlJsonValueAdapter.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0003\u0018��2\b\u0012\u0004\u0012\u00020\u00020\u0001B\u000f\u0012\u0006\u0010\u0003\u001a\u00020\u0002¢\u0006\u0004\b\u0004\u0010\u0005J\b\u0010\u0006\u001a\u00020\u0007H\u0016J\b\u0010\b\u001a\u00020\u0007H\u0016J\b\u0010\t\u001a\u00020\u0007H\u0016¨\u0006\n"}, d2 = {"Lorg/toml/ide/json/TomlJsonGenericValueAdapter;", "Lorg/toml/ide/json/TomlJsonValueAdapter;", "Lorg/toml/lang/psi/TomlElement;", "value", "<init>", "(Lorg/toml/lang/psi/TomlElement;)V", "isStringLiteral", "", "isNumberLiteral", "isBooleanLiteral", "intellij.toml.json"})
public final class TomlJsonGenericValueAdapter extends TomlJsonValueAdapter<TomlElement> {
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public TomlJsonGenericValueAdapter(@NotNull TomlElement value) {
        super(value, null);
        Intrinsics.checkNotNullParameter(value, "value");
    }

    @Override // org.toml.ide.json.TomlJsonValueAdapter
    public boolean isStringLiteral() {
        return ((getElement() instanceof TomlLiteral) && (TomlLiteralKt.getKind((TomlLiteral) getElement()) instanceof TomlLiteralKind.String)) || (getElement() instanceof TomlKey);
    }

    @Override // org.toml.ide.json.TomlJsonValueAdapter
    public boolean isNumberLiteral() {
        return (getElement() instanceof TomlLiteral) && (TomlLiteralKt.getKind((TomlLiteral) getElement()) instanceof TomlLiteralKind.Number);
    }

    @Override // org.toml.ide.json.TomlJsonValueAdapter
    public boolean isBooleanLiteral() {
        return (getElement() instanceof TomlLiteral) && (TomlLiteralKt.getKind((TomlLiteral) getElement()) instanceof TomlLiteralKind.Boolean);
    }
}