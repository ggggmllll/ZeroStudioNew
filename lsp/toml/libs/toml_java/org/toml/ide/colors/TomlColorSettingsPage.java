package org.toml.ide.colors;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.psi.tree.IElementType;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import kotlin.Lazy;
import kotlin.LazyKt;
import kotlin.Metadata;
import kotlin.collections.MapsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.ranges.RangesKt;
import org.jetbrains.annotations.NotNull;
import org.toml.TomlIcons;
import org.toml.ide.TomlHighlighter;
import org.toml.lang.TomlLanguage;
import org.toml.lang.lexer._TomlLexer;

/* compiled from: TomlColorSettingsPage.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018��2\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\u0012\u001a\u00020\nH\u0016J\b\u0010\u0013\u001a\u00020\u0014H\u0016J\b\u0010\u0015\u001a\u00020\u0016H\u0016J\u0014\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u000b0\tH\u0016J\u0013\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u0016¢\u0006\u0002\u0010\u0019J\u0013\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u001b0\u0005H\u0016¢\u0006\u0002\u0010\u001cJ\b\u0010\u001d\u001a\u00020\nH\u0016R\u0016\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005X\u0082\u0004¢\u0006\u0004\n\u0002\u0010\u0007R\u001a\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u000b0\tX\u0082\u0004¢\u0006\u0002\n��R \u0010\f\u001a\u00070\n¢\u0006\u0002\b\r8BX\u0082\u0084\u0002¢\u0006\f\n\u0004\b\u0010\u0010\u0011\u001a\u0004\b\u000e\u0010\u000f¨\u0006\u001e"}, d2 = {"Lorg/toml/ide/colors/TomlColorSettingsPage;", "Lcom/intellij/openapi/options/colors/ColorSettingsPage;", "<init>", "()V", "attributesDescriptors", "", "Lcom/intellij/openapi/options/colors/AttributesDescriptor;", "[Lcom/intellij/openapi/options/colors/AttributesDescriptor;", "tagToDescriptorMap", "", "", "Lcom/intellij/openapi/editor/colors/TextAttributesKey;", "highlighterDemoText", "Lorg/jetbrains/annotations/NotNull;", "getHighlighterDemoText", "()Ljava/lang/String;", "highlighterDemoText$delegate", "Lkotlin/Lazy;", "getDisplayName", "getHighlighter", "Lcom/intellij/openapi/fileTypes/SyntaxHighlighter;", "getIcon", "Ljavax/swing/Icon;", "getAdditionalHighlightingTagToDescriptorMap", "getAttributeDescriptors", "()[Lcom/intellij/openapi/options/colors/AttributesDescriptor;", "getColorDescriptors", "Lcom/intellij/openapi/options/colors/ColorDescriptor;", "()[Lcom/intellij/openapi/options/colors/ColorDescriptor;", "getDemoText", "intellij.toml.core"})
@SourceDebugExtension({"SMAP\nTomlColorSettingsPage.kt\nKotlin\n*S Kotlin\n*F\n+ 1 TomlColorSettingsPage.kt\norg/toml/ide/colors/TomlColorSettingsPage\n+ 2 _Arrays.kt\nkotlin/collections/ArraysKt___ArraysKt\n+ 3 ArraysJVM.kt\nkotlin/collections/ArraysKt__ArraysJVMKt\n*L\n1#1,36:1\n11158#2:37\n11493#2,3:38\n8768#2,2:45\n9038#2,4:47\n37#3:41\n36#3,3:42\n*S KotlinDebug\n*F\n+ 1 TomlColorSettingsPage.kt\norg/toml/ide/colors/TomlColorSettingsPage\n*L\n21#1:37\n21#1:38,3\n22#1:45,2\n22#1:47,4\n21#1:41\n21#1:42,3\n*E\n"})
public final class TomlColorSettingsPage implements ColorSettingsPage {
    @NotNull
    private final AttributesDescriptor[] attributesDescriptors;
    @NotNull
    private final Map<String, TextAttributesKey> tagToDescriptorMap;
    @NotNull
    private final Lazy highlighterDemoText$delegate;

    public TomlColorSettingsPage() {
        TomlColor[] values = TomlColor.values();
        Collection destination$iv$iv = new ArrayList(values.length);
        for (TomlColor tomlColor : values) {
            destination$iv$iv.add(tomlColor.getAttributesDescriptor());
        }
        Collection $this$toTypedArray$iv = (List) destination$iv$iv;
        this.attributesDescriptors = (AttributesDescriptor[]) $this$toTypedArray$iv.toArray(new AttributesDescriptor[0]);
        TomlColor[] values2 = TomlColor.values();
        int capacity$iv = RangesKt.coerceAtLeast(MapsKt.mapCapacity(values2.length), 16);
        Map destination$iv$iv2 = new LinkedHashMap(capacity$iv);
        for (TomlColor tomlColor2 : values2) {
            destination$iv$iv2.put(tomlColor2.name(), tomlColor2.getTextAttributesKey());
        }
        this.tagToDescriptorMap = destination$iv$iv2;
        this.highlighterDemoText$delegate = LazyKt.lazy(() -> {
            return highlighterDemoText_delegate$lambda$3(r1);
        });
    }

    private final String getHighlighterDemoText() {
        return (String) this.highlighterDemoText$delegate.getValue();
    }

    private static final String highlighterDemoText_delegate$lambda$3(TomlColorSettingsPage this$0) {
        InputStream stream = this$0.getClass().getClassLoader().getResourceAsStream("org/toml/ide/colors/highlighterDemoText.toml");
        return StreamUtil.convertSeparators(StreamUtil.readText(stream, "UTF-8"));
    }

    @NotNull
    public String getDisplayName() {
        String displayName = TomlLanguage.INSTANCE.getDisplayName();
        Intrinsics.checkNotNullExpressionValue(displayName, "getDisplayName(...)");
        return displayName;
    }

    @NotNull
    public SyntaxHighlighter getHighlighter() {
        return new TomlHighlighter();
    }

    @NotNull
    public Icon getIcon() {
        Icon icon = TomlIcons.TomlFile;
        Intrinsics.checkNotNullExpressionValue(icon, "TomlFile");
        return icon;
    }

    @NotNull
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return this.tagToDescriptorMap;
    }

    @NotNull
    public AttributesDescriptor[] getAttributeDescriptors() {
        return this.attributesDescriptors;
    }

    @NotNull
    public ColorDescriptor[] getColorDescriptors() {
        ColorDescriptor[] colorDescriptorArr = ColorDescriptor.EMPTY_ARRAY;
        Intrinsics.checkNotNullExpressionValue(colorDescriptorArr, "EMPTY_ARRAY");
        return colorDescriptorArr;
    }

    @NotNull
    public String getDemoText() {
        return getHighlighterDemoText();
    }
}