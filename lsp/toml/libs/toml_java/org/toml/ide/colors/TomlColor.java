package org.toml.ide.colors;

import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.options.OptionsBundle;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.psi.tree.IElementType;
import java.util.function.Supplier;
import kotlin.Metadata;
import kotlin.enums.EnumEntries;
import kotlin.enums.EnumEntriesKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.toml.TomlBundle;
import org.toml.lang.lexer._TomlLexer;

/* JADX WARN: Enum visitor error
jadx.core.utils.exceptions.JadxRuntimeException: Init of enum NUMBER uses external variables
	at jadx.core.dex.visitors.EnumVisitor.createEnumFieldByConstructor(EnumVisitor.java:444)
	at jadx.core.dex.visitors.EnumVisitor.processEnumFieldByField(EnumVisitor.java:368)
	at jadx.core.dex.visitors.EnumVisitor.processEnumFieldByWrappedInsn(EnumVisitor.java:333)
	at jadx.core.dex.visitors.EnumVisitor.extractEnumFieldsFromFilledArray(EnumVisitor.java:318)
	at jadx.core.dex.visitors.EnumVisitor.extractEnumFieldsFromInsn(EnumVisitor.java:258)
	at jadx.core.dex.visitors.EnumVisitor.extractEnumFieldsFromInvoke(EnumVisitor.java:289)
	at jadx.core.dex.visitors.EnumVisitor.extractEnumFieldsFromInsn(EnumVisitor.java:262)
	at jadx.core.dex.visitors.EnumVisitor.convertToEnum(EnumVisitor.java:151)
	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:100)
 */
/* JADX WARN: Failed to restore enum class, 'enum' modifier and super class removed */
/* compiled from: TomlColors.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��0\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n��\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0081\u0002\u0018��2\b\u0012\u0004\u0012\u00020��0\u0001B(\b\u0002\u0012\u0011\u0010\u0002\u001a\r\u0012\t\u0012\u00070\u0004¢\u0006\u0002\b\u00050\u0003\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0007¢\u0006\u0004\b\b\u0010\tR\u0011\u0010\u0012\u001a\u00020\u0007¢\u0006\b\n��\u001a\u0004\b\u0013\u0010\u0014R\u0011\u0010\u0015\u001a\u00020\u0016¢\u0006\b\n��\u001a\u0004\b\u0017\u0010\u0018R\u0011\u0010\u0019\u001a\u00020\u001a¢\u0006\b\n��\u001a\u0004\b\u001b\u0010\u001cj\u0002\b\nj\u0002\b\u000bj\u0002\b\fj\u0002\b\rj\u0002\b\u000ej\u0002\b\u000fj\u0002\b\u0010j\u0002\b\u0011¨\u0006\u001d"}, d2 = {"Lorg/toml/ide/colors/TomlColor;", "", "humanName", "Ljava/util/function/Supplier;", "", "Lcom/intellij/openapi/util/NlsContexts$AttributeDescriptor;", "default", "Lcom/intellij/openapi/editor/colors/TextAttributesKey;", "<init>", "(Ljava/lang/String;ILjava/util/function/Supplier;Lcom/intellij/openapi/editor/colors/TextAttributesKey;)V", "KEY", "COMMENT", "BOOLEAN", "NUMBER", "DATE", "STRING", "VALID_STRING_ESCAPE", "INVALID_STRING_ESCAPE", "textAttributesKey", "getTextAttributesKey", "()Lcom/intellij/openapi/editor/colors/TextAttributesKey;", "attributesDescriptor", "Lcom/intellij/openapi/options/colors/AttributesDescriptor;", "getAttributesDescriptor", "()Lcom/intellij/openapi/options/colors/AttributesDescriptor;", "testSeverity", "Lcom/intellij/lang/annotation/HighlightSeverity;", "getTestSeverity", "()Lcom/intellij/lang/annotation/HighlightSeverity;", "intellij.toml.core"})
public final class TomlColor {
    @NotNull
    private final TextAttributesKey textAttributesKey;
    @NotNull
    private final AttributesDescriptor attributesDescriptor;
    @NotNull
    private final HighlightSeverity testSeverity;
    public static final TomlColor KEY = new TomlColor("KEY", 0, TomlBundle.INSTANCE.messagePointer("color.settings.toml.keys", new Object[0]), DefaultLanguageHighlighterColors.KEYWORD);
    public static final TomlColor COMMENT = new TomlColor("COMMENT", 1, TomlBundle.INSTANCE.messagePointer("color.settings.toml.comments", new Object[0]), DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TomlColor BOOLEAN = new TomlColor("BOOLEAN", 2, TomlBundle.INSTANCE.messagePointer("color.settings.toml.boolean", new Object[0]), DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL);
    public static final TomlColor NUMBER;
    public static final TomlColor DATE;
    public static final TomlColor STRING;
    public static final TomlColor VALID_STRING_ESCAPE;
    public static final TomlColor INVALID_STRING_ESCAPE;
    private static final /* synthetic */ TomlColor[] $VALUES;
    private static final /* synthetic */ EnumEntries $ENTRIES;

    private static final /* synthetic */ TomlColor[] $values() {
        return new TomlColor[]{KEY, COMMENT, BOOLEAN, NUMBER, DATE, STRING, VALID_STRING_ESCAPE, INVALID_STRING_ESCAPE};
    }

    private TomlColor(String $enum$name, int $enum$ordinal, Supplier humanName, TextAttributesKey textAttributesKey) {
        TextAttributesKey createTextAttributesKey = TextAttributesKey.createTextAttributesKey("org.toml." + name(), textAttributesKey);
        Intrinsics.checkNotNullExpressionValue(createTextAttributesKey, "createTextAttributesKey(...)");
        this.textAttributesKey = createTextAttributesKey;
        this.attributesDescriptor = new AttributesDescriptor(humanName, this.textAttributesKey);
        this.testSeverity = new HighlightSeverity(name(), HighlightSeverity.INFORMATION.myVal);
    }

    /* synthetic */ TomlColor(String str, int i, Supplier supplier, TextAttributesKey textAttributesKey, int i2, DefaultConstructorMarker defaultConstructorMarker) {
        this(str, i, supplier, (i2 & 2) != 0 ? null : textAttributesKey);
    }

    static {
        Supplier messagePointer = OptionsBundle.messagePointer("options.language.defaults.number", new Object[0]);
        Intrinsics.checkNotNullExpressionValue(messagePointer, "messagePointer(...)");
        NUMBER = new TomlColor("NUMBER", 3, messagePointer, DefaultLanguageHighlighterColors.NUMBER);
        DATE = new TomlColor("DATE", 4, TomlBundle.INSTANCE.messagePointer("color.settings.toml.date", new Object[0]), DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL);
        Supplier messagePointer2 = OptionsBundle.messagePointer("options.language.defaults.string", new Object[0]);
        Intrinsics.checkNotNullExpressionValue(messagePointer2, "messagePointer(...)");
        STRING = new TomlColor("STRING", 5, messagePointer2, DefaultLanguageHighlighterColors.STRING);
        Supplier messagePointer3 = OptionsBundle.messagePointer("options.language.defaults.valid.esc.seq", new Object[0]);
        Intrinsics.checkNotNullExpressionValue(messagePointer3, "messagePointer(...)");
        VALID_STRING_ESCAPE = new TomlColor("VALID_STRING_ESCAPE", 6, messagePointer3, DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
        Supplier messagePointer4 = OptionsBundle.messagePointer("options.language.defaults.invalid.esc.seq", new Object[0]);
        Intrinsics.checkNotNullExpressionValue(messagePointer4, "messagePointer(...)");
        INVALID_STRING_ESCAPE = new TomlColor("INVALID_STRING_ESCAPE", 7, messagePointer4, DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE);
        $VALUES = $values();
        $ENTRIES = EnumEntriesKt.enumEntries($VALUES);
    }

    @NotNull
    public final TextAttributesKey getTextAttributesKey() {
        return this.textAttributesKey;
    }

    @NotNull
    public final AttributesDescriptor getAttributesDescriptor() {
        return this.attributesDescriptor;
    }

    @NotNull
    public final HighlightSeverity getTestSeverity() {
        return this.testSeverity;
    }

    public static TomlColor[] values() {
        return (TomlColor[]) $VALUES.clone();
    }

    public static TomlColor valueOf(String value) {
        return (TomlColor) Enum.valueOf(TomlColor.class, value);
    }

    @NotNull
    public static EnumEntries<TomlColor> getEntries() {
        return $ENTRIES;
    }
}