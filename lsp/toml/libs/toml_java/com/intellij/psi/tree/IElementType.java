package com.intellij.psi.tree;

import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.util.ArrayFactory;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.lang.lexer._TomlLexer;

public class IElementType {
    public static final short FIRST_TOKEN_INDEX = 1;
    private static final short MAX_INDEXED_TYPES = 15000;
    private static short size;
    private final short myIndex;
    private final String myDebugName;
    private final Language myLanguage;
    private static final Logger LOG = Logger.getInstance("#com.intellij.psi.tree.IElementType");
    public static final IElementType[] EMPTY_ARRAY = new IElementType[0];
    public static final ArrayFactory<IElementType> ARRAY_FACTORY = count -> {
        return count == 0 ? EMPTY_ARRAY : new IElementType[count];
    };
    public static final Predicate TRUE = type -> {
        return true;
    };
    private static volatile IElementType[] ourRegistry = EMPTY_ARRAY;
    private static final Object lock = new String("registry lock");

    @FunctionalInterface
    /* loaded from: 2026040502522302a7da3a-171e-4c6c-a1b5-134e29fd6e0f.jar:com/intellij/psi/tree/IElementType$Predicate.class */
    public interface Predicate {
        boolean matches(@NotNull IElementType iElementType);
    }

    static {
        IElementType[] init = new IElementType[137];
        init[0] = new IElementType("NULL", Language.ANY, false);
        push(init);
    }

    @NotNull
    static IElementType[] push(@NotNull IElementType[] types) {
        IElementType[] oldRegistry;
        synchronized (lock) {
            oldRegistry = ourRegistry;
            ourRegistry = types;
            size = (short) ContainerUtil.skipNulls(Arrays.asList(ourRegistry)).size();
        }
        return oldRegistry;
    }

    public IElementType(@NotNull String debugName, @Nullable Language language) {
        this(debugName, language, true);
    }

    protected IElementType(@NotNull String debugName, @Nullable Language language, boolean register) {
        this.myDebugName = debugName;
        this.myLanguage = language == null ? Language.ANY : language;
        if (register) {
            synchronized (lock) {
                short s = size;
                size = (short) (s + 1);
                this.myIndex = s;
                LOG.assertTrue(this.myIndex < MAX_INDEXED_TYPES, "Too many element types registered. Out of (short) range.");
                IElementType[] newRegistry = this.myIndex >= ourRegistry.length ? (IElementType[]) ArrayUtil.realloc(ourRegistry, ((ourRegistry.length * 3) / 2) + 1, ARRAY_FACTORY) : ourRegistry;
                newRegistry[this.myIndex] = this;
                ourRegistry = newRegistry;
            }
            return;
        }
        this.myIndex = (short) -1;
    }

    @NotNull
    public Language getLanguage() {
        return this.myLanguage;
    }

    public final short getIndex() {
        return this.myIndex;
    }

    public int hashCode() {
        return this.myIndex >= 0 ? this.myIndex : super.hashCode();
    }

    public String toString() {
        return this.myDebugName;
    }

    public boolean isLeftBound() {
        return false;
    }

    public static IElementType find(short idx) {
        return ourRegistry[idx];
    }

    static short getAllocatedTypesCount() {
        short s;
        synchronized (lock) {
            s = size;
        }
        return s;
    }

    @NotNull
    public static IElementType[] enumerate(@NotNull Predicate p) {
        IElementType[] iElementTypeArr;
        List<IElementType> matches = new ArrayList<>();
        for (IElementType value : ourRegistry) {
            if (value != null && p.matches(value)) {
                matches.add(value);
            }
        }
        return (IElementType[]) matches.toArray(new IElementType[0]);
    }

    /* loaded from: 2026040502522302a7da3a-171e-4c6c-a1b5-134e29fd6e0f.jar:com/intellij/psi/tree/IElementType$TombstoneElementType.class */
    private static final class TombstoneElementType extends IElementType {
        private static /* synthetic */ void $$$reportNull$$$0(int i) {
            Object[] objArr = new Object[3];
            switch (i) {
                case _TomlLexer.YYINITIAL /* 0 */:
                default:
                    objArr[0] = "debugName";
                    break;
                case IElementType.FIRST_TOKEN_INDEX /* 1 */:
                    objArr[0] = "type";
                    break;
                case 2:
                    objArr[0] = "pluginDescriptor";
                    break;
            }
            objArr[1] = "com/intellij/psi/tree/IElementType$TombstoneElementType";
            switch (i) {
                case _TomlLexer.YYINITIAL /* 0 */:
                default:
                    objArr[2] = "<init>";
                    break;
                case IElementType.FIRST_TOKEN_INDEX /* 1 */:
                case 2:
                    objArr[2] = "create";
                    break;
            }
            throw new IllegalArgumentException(String.format("Argument for @NotNull parameter '%s' of %s.%s must not be null", objArr));
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        private TombstoneElementType(@NotNull @NonNls String debugName) {
            super(debugName, Language.ANY);
            if (debugName == null) {
                $$$reportNull$$$0(0);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static TombstoneElementType create(@NotNull IElementType type, @NotNull PluginDescriptor pluginDescriptor) {
            if (type == null) {
                $$$reportNull$$$0(1);
            }
            if (pluginDescriptor == null) {
                $$$reportNull$$$0(2);
            }
            return new TombstoneElementType("tombstone of " + type + " (" + type.getClass() + ") belonged to unloaded " + pluginDescriptor);
        }
    }
}