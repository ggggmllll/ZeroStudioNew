package org.toml.ide.formatter;

import com.intellij.formatting.SpacingBuilder;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.ide.formatter.impl.SpacingKt;
import org.toml.lang.TomlLanguage;
import org.toml.lang.lexer._TomlLexer;

/* compiled from: TomlFmtContext.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��.\n\u0002\u0018\u0002\n\u0002\u0010��\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n��\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\b\u0018�� \u00162\u00020\u0001:\u0001\u0016B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\t\u0010\f\u001a\u00020\u0003HÆ\u0003J\t\u0010\r\u001a\u00020\u0005HÆ\u0003J\u001d\u0010\u000e\u001a\u00020��2\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005HÆ\u0001J\u0013\u0010\u000f\u001a\u00020\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0012\u001a\u00020\u0013HÖ\u0001J\t\u0010\u0014\u001a\u00020\u0015HÖ\u0001R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n��\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0004\u001a\u00020\u0005¢\u0006\b\n��\u001a\u0004\b\n\u0010\u000b¨\u0006\u0017"}, d2 = {"Lorg/toml/ide/formatter/TomlFmtContext;", "", "commonSettings", "Lcom/intellij/psi/codeStyle/CommonCodeStyleSettings;", "spacingBuilder", "Lcom/intellij/formatting/SpacingBuilder;", "<init>", "(Lcom/intellij/psi/codeStyle/CommonCodeStyleSettings;Lcom/intellij/formatting/SpacingBuilder;)V", "getCommonSettings", "()Lcom/intellij/psi/codeStyle/CommonCodeStyleSettings;", "getSpacingBuilder", "()Lcom/intellij/formatting/SpacingBuilder;", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "", "Companion", "intellij.toml.core"})
public final class TomlFmtContext {
    @NotNull
    public static final Companion Companion = new Companion(null);
    @NotNull
    private final CommonCodeStyleSettings commonSettings;
    @NotNull
    private final SpacingBuilder spacingBuilder;

    @NotNull
    public final CommonCodeStyleSettings component1() {
        return this.commonSettings;
    }

    @NotNull
    public final SpacingBuilder component2() {
        return this.spacingBuilder;
    }

    @NotNull
    public final TomlFmtContext copy(@NotNull CommonCodeStyleSettings commonSettings, @NotNull SpacingBuilder spacingBuilder) {
        Intrinsics.checkNotNullParameter(commonSettings, "commonSettings");
        Intrinsics.checkNotNullParameter(spacingBuilder, "spacingBuilder");
        return new TomlFmtContext(commonSettings, spacingBuilder);
    }

    public static /* synthetic */ TomlFmtContext copy$default(TomlFmtContext tomlFmtContext, CommonCodeStyleSettings commonCodeStyleSettings, SpacingBuilder spacingBuilder, int i, Object obj) {
        if ((i & 1) != 0) {
            commonCodeStyleSettings = tomlFmtContext.commonSettings;
        }
        if ((i & 2) != 0) {
            spacingBuilder = tomlFmtContext.spacingBuilder;
        }
        return tomlFmtContext.copy(commonCodeStyleSettings, spacingBuilder);
    }

    @NotNull
    public String toString() {
        return "TomlFmtContext(commonSettings=" + this.commonSettings + ", spacingBuilder=" + this.spacingBuilder + ")";
    }

    public int hashCode() {
        int result = this.commonSettings.hashCode();
        return (result * 31) + this.spacingBuilder.hashCode();
    }

    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof TomlFmtContext) {
            TomlFmtContext tomlFmtContext = (TomlFmtContext) other;
            return Intrinsics.areEqual(this.commonSettings, tomlFmtContext.commonSettings) && Intrinsics.areEqual(this.spacingBuilder, tomlFmtContext.spacingBuilder);
        }
        return false;
    }

    public TomlFmtContext(@NotNull CommonCodeStyleSettings commonSettings, @NotNull SpacingBuilder spacingBuilder) {
        Intrinsics.checkNotNullParameter(commonSettings, "commonSettings");
        Intrinsics.checkNotNullParameter(spacingBuilder, "spacingBuilder");
        this.commonSettings = commonSettings;
        this.spacingBuilder = spacingBuilder;
    }

    @NotNull
    public final CommonCodeStyleSettings getCommonSettings() {
        return this.commonSettings;
    }

    @NotNull
    public final SpacingBuilder getSpacingBuilder() {
        return this.spacingBuilder;
    }

    /* compiled from: TomlFmtContext.kt */
    @Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��\u0018\n\u0002\u0018\u0002\n\u0002\u0010��\n\u0002\b\u0003\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\b\u0086\u0003\u0018��2\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007¨\u0006\b"}, d2 = {"Lorg/toml/ide/formatter/TomlFmtContext$Companion;", "", "<init>", "()V", "create", "Lorg/toml/ide/formatter/TomlFmtContext;", "settings", "Lcom/intellij/psi/codeStyle/CodeStyleSettings;", "intellij.toml.core"})
    /* loaded from: 2026040502522302a7da3a-171e-4c6c-a1b5-134e29fd6e0f.jar:org/toml/ide/formatter/TomlFmtContext$Companion.class */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker $constructor_marker) {
            this();
        }

        private Companion() {
        }

        @NotNull
        public final TomlFmtContext create(@NotNull CodeStyleSettings settings) {
            Intrinsics.checkNotNullParameter(settings, "settings");
            CommonCodeStyleSettings commonSettings = settings.getCommonSettings(TomlLanguage.INSTANCE);
            Intrinsics.checkNotNullExpressionValue(commonSettings, "getCommonSettings(...)");
            return new TomlFmtContext(commonSettings, SpacingKt.createSpacingBuilder(commonSettings));
        }
    }
}