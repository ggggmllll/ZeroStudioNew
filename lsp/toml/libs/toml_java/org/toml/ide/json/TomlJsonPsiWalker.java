package org.toml.ide.json;

import com.intellij.json.pointer.JsonPointerPosition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ThreeState;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.jsonSchema.extension.JsonLikePsiWalker;
import com.jetbrains.jsonSchema.extension.adapters.JsonPropertyAdapter;
import com.jetbrains.jsonSchema.extension.adapters.JsonValueAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kotlin.Metadata;
import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;
import kotlin.collections.SetsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlArray;
import org.toml.lang.psi.TomlArrayTable;
import org.toml.lang.psi.TomlElement;
import org.toml.lang.psi.TomlFile;
import org.toml.lang.psi.TomlHeaderOwner;
import org.toml.lang.psi.TomlInlineTable;
import org.toml.lang.psi.TomlKey;
import org.toml.lang.psi.TomlKeySegment;
import org.toml.lang.psi.TomlKeyValue;
import org.toml.lang.psi.TomlKeyValueOwner;
import org.toml.lang.psi.TomlTable;
import org.toml.lang.psi.TomlValue;

/* compiled from: TomlJsonPsiWalker.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��P\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\"\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0010 \n��\n\u0002\u0018\u0002\n\u0002\b\u0007\bÆ\u0002\u0018��2\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u0012\u0010\u0004\u001a\u00020\u00052\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007H\u0016J\u0010\u0010\b\u001a\u00020\t2\u0006\u0010\u0006\u001a\u00020\u0007H\u0016J\u0012\u0010\n\u001a\u0004\u0018\u00010\u00072\u0006\u0010\u0006\u001a\u00020\u0007H\u0016J\u0018\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\r\u001a\u00020\tH\u0016J \u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000f2\u0006\u0010\u0011\u001a\u00020\u00072\b\u0010\u0012\u001a\u0004\u0018\u00010\u0007H\u0016J\u0012\u0010\u0013\u001a\u0004\u0018\u00010\u00142\u0006\u0010\u0006\u001a\u00020\u0007H\u0016J\u0010\u0010\u0015\u001a\u00020\t2\u0006\u0010\u0006\u001a\u00020\u0007H\u0016J\u0012\u0010\u0016\u001a\u0004\u0018\u00010\u00172\u0006\u0010\u0006\u001a\u00020\u0007H\u0016J\u0016\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00070\u00192\u0006\u0010\u001a\u001a\u00020\u001bH\u0016J\u0014\u0010\u001c\u001a\u0004\u0018\u00010\u00072\b\u0010\u001d\u001a\u0004\u0018\u00010\u0007H\u0016J\u0010\u0010\u001e\u001a\u00020\t2\u0006\u0010\u0006\u001a\u00020\u0007H\u0016J\b\u0010\u001f\u001a\u00020\tH\u0016J\b\u0010 \u001a\u00020\tH\u0016J\b\u0010!\u001a\u00020\tH\u0016¨\u0006\""}, d2 = {"Lorg/toml/ide/json/TomlJsonPsiWalker;", "Lcom/jetbrains/jsonSchema/extension/JsonLikePsiWalker;", "<init>", "()V", "isName", "Lcom/intellij/util/ThreeState;", "element", "Lcom/intellij/psi/PsiElement;", "isPropertyWithValue", "", "findElementToCheck", "findPosition", "Lcom/intellij/json/pointer/JsonPointerPosition;", "forceLastTransition", "getPropertyNamesOfParentObject", "", "", "originalPosition", "computedPosition", "getParentPropertyAdapter", "Lcom/jetbrains/jsonSchema/extension/adapters/JsonPropertyAdapter;", "isTopJsonElement", "createValueAdapter", "Lcom/jetbrains/jsonSchema/extension/adapters/JsonValueAdapter;", "getRoots", "", "file", "Lcom/intellij/psi/PsiFile;", "getPropertyNameElement", "property", "hasMissingCommaAfter", "acceptsEmptyRoot", "requiresNameQuotes", "allowsSingleQuotes", "intellij.toml.json"})
@SourceDebugExtension({"SMAP\nTomlJsonPsiWalker.kt\nKotlin\n*S Kotlin\n*F\n+ 1 TomlJsonPsiWalker.kt\norg/toml/ide/json/TomlJsonPsiWalker\n+ 2 psiTreeUtil.kt\ncom/intellij/psi/util/PsiTreeUtilKt\n+ 3 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 4 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,160:1\n66#2,2:161\n66#2,2:177\n66#2,2:179\n67#2:186\n967#3,7:163\n1628#3,3:170\n1557#3:173\n1628#3,3:174\n1619#3:181\n1863#3:182\n1864#3:184\n1620#3:185\n1#4:183\n*S KotlinDebug\n*F\n+ 1 TomlJsonPsiWalker.kt\norg/toml/ide/json/TomlJsonPsiWalker\n*L\n27#1:161,2\n130#1:177,2\n131#1:179,2\n138#1:186\n46#1:163,7\n53#1:170,3\n83#1:173\n83#1:174,3\n134#1:181\n134#1:182\n134#1:184\n134#1:185\n134#1:183\n*E\n"})
public final class TomlJsonPsiWalker implements JsonLikePsiWalker {
    @NotNull
    public static final TomlJsonPsiWalker INSTANCE = new TomlJsonPsiWalker();

    private TomlJsonPsiWalker() {
    }

    @NotNull
    public ThreeState isName(@Nullable PsiElement element) {
        return element instanceof TomlKeySegment ? ThreeState.YES : ThreeState.NO;
    }

    public boolean isPropertyWithValue(@NotNull PsiElement element) {
        Intrinsics.checkNotNullParameter(element, "element");
        return (element instanceof TomlKeyValue) && ((TomlKeyValue) element).getValue() != null;
    }

    @Nullable
    public PsiElement findElementToCheck(@NotNull PsiElement element) {
        Intrinsics.checkNotNullParameter(element, "element");
        return PsiTreeUtil.getParentOfType(element, TomlElement.class, true);
    }

    @NotNull
    public JsonPointerPosition findPosition(@NotNull PsiElement element, boolean forceLastTransition) {
        List list;
        Iterable segments;
        Intrinsics.checkNotNullParameter(element, "element");
        JsonPointerPosition position = new JsonPointerPosition();
        PsiElement psiElement = element;
        List tableHeaderSegments = new ArrayList();
        Integer nestedIndex = null;
        while (!(psiElement instanceof PsiFile)) {
            PsiElement parent = psiElement.getParent();
            if ((psiElement instanceof TomlKeySegment) && (parent instanceof TomlKey)) {
                if (!Intrinsics.areEqual(psiElement, element) || forceLastTransition) {
                    position.addPrecedingStep(((TomlKeySegment) psiElement).getName());
                }
                Iterable $this$takeWhile$iv = ((TomlKey) parent).getSegments();
                ArrayList list$iv = new ArrayList();
                for (Object item$iv : $this$takeWhile$iv) {
                    TomlKeySegment it = (TomlKeySegment) item$iv;
                    if (!(!Intrinsics.areEqual(it, psiElement))) {
                        break;
                    }
                    list$iv.add(item$iv);
                }
                for (TomlKeySegment segment : CollectionsKt.asReversed(list$iv)) {
                    position.addPrecedingStep(segment.getName());
                }
            } else if ((psiElement instanceof TomlKeyValue) && (parent instanceof TomlHeaderOwner)) {
                TomlKey parentKey = ((TomlHeaderOwner) parent).getHeader().getKey();
                if (parentKey == null) {
                    break;
                }
                Iterable<TomlKeySegment> $this$mapTo$iv = parentKey.getSegments();
                for (TomlKeySegment it2 : $this$mapTo$iv) {
                    tableHeaderSegments.add(it2.getName());
                }
                if (element instanceof TomlKeyValue) {
                    TomlKey currentKey = ((TomlKeyValue) psiElement).getKey();
                    for (TomlKeySegment segment2 : CollectionsKt.asReversed(currentKey.getSegments())) {
                        if (!Intrinsics.areEqual(segment2, element) || forceLastTransition) {
                            position.addPrecedingStep(segment2.getName());
                        }
                    }
                }
            } else if ((psiElement instanceof TomlValue) && (parent instanceof TomlArray)) {
                if (!Intrinsics.areEqual(psiElement, element) || forceLastTransition) {
                    position.addPrecedingStep(((TomlArray) parent).getElements().indexOf(psiElement));
                }
            } else if ((psiElement instanceof TomlValue) && (parent instanceof TomlKeyValue)) {
                for (TomlKeySegment segment3 : CollectionsKt.asReversed(((TomlKeyValue) parent).getKey().getSegments())) {
                    if (!Intrinsics.areEqual(segment3, element) || forceLastTransition) {
                        position.addPrecedingStep(segment3.getName());
                    }
                }
            } else if ((psiElement instanceof TomlHeaderOwner) && (parent instanceof PsiFile)) {
                TomlKey key = ((TomlHeaderOwner) psiElement).getHeader().getKey();
                if (key == null || (segments = key.getSegments()) == null) {
                    list = null;
                } else {
                    Iterable $this$map$iv = segments;
                    Collection destination$iv$iv = new ArrayList(CollectionsKt.collectionSizeOrDefault($this$map$iv, 10));
                    for (Object item$iv$iv : $this$map$iv) {
                        TomlKeySegment it3 = (TomlKeySegment) item$iv$iv;
                        destination$iv$iv.add(it3.getName());
                    }
                    list = (List) destination$iv$iv;
                }
                if (list == null) {
                    list = CollectionsKt.emptyList();
                }
                List currentSegments = list;
                while (tableHeaderSegments.size() > currentSegments.size() && ContainerUtil.startsWith(tableHeaderSegments, currentSegments)) {
                    if (nestedIndex != null) {
                        position.addPrecedingStep(nestedIndex.intValue());
                        nestedIndex = null;
                    }
                    position.addPrecedingStep((String) CollectionsKt.removeLast(tableHeaderSegments));
                }
                if (Intrinsics.areEqual(currentSegments, tableHeaderSegments) && (psiElement instanceof TomlArrayTable)) {
                    if (nestedIndex != null) {
                        nestedIndex = Integer.valueOf(nestedIndex.intValue() + 1);
                    } else {
                        nestedIndex = 0;
                    }
                }
            }
            if ((psiElement instanceof TomlHeaderOwner) && (parent instanceof PsiFile) && ((TomlHeaderOwner) psiElement).getPrevSibling() != null) {
                do {
                    psiElement = psiElement.getPrevSibling();
                    if (!(psiElement instanceof TomlHeaderOwner)) {
                    }
                } while (psiElement.getPrevSibling() != null);
            } else {
                psiElement = psiElement.getParent();
            }
        }
        while (true) {
            if (!tableHeaderSegments.isEmpty()) {
                if (nestedIndex != null) {
                    position.addPrecedingStep(nestedIndex.intValue());
                    nestedIndex = null;
                }
                position.addPrecedingStep((String) CollectionsKt.removeLast(tableHeaderSegments));
            } else {
                return position;
            }
        }
    }

    @NotNull
    public Set<String> getPropertyNamesOfParentObject(@NotNull PsiElement originalPosition, @Nullable PsiElement computedPosition) {
        TomlTable tomlTable;
        Intrinsics.checkNotNullParameter(originalPosition, "originalPosition");
        TomlTable tomlTable2 = (TomlTable) PsiTreeUtil.getParentOfType(originalPosition, TomlTable.class, true);
        if (tomlTable2 != null) {
            tomlTable = tomlTable2;
        } else {
            TomlInlineTable tomlInlineTable = (TomlInlineTable) PsiTreeUtil.getParentOfType(originalPosition, TomlInlineTable.class, true);
            if (tomlInlineTable != null) {
                tomlTable = tomlInlineTable;
            } else {
                PsiElement prevSibling = originalPosition.getPrevSibling();
                TomlTable tomlTable3 = prevSibling instanceof TomlTable ? (TomlTable) prevSibling : null;
                if (tomlTable3 == null) {
                    return SetsKt.emptySet();
                }
                tomlTable = tomlTable3;
            }
        }
        TomlKeyValueOwner table = tomlTable;
        Iterable $this$mapNotNullTo$iv = new TomlJsonObjectAdapter(table).getPropertyList();
        Collection destination$iv = new HashSet();
        for (Object element$iv$iv : $this$mapNotNullTo$iv) {
            JsonPropertyAdapter it = (JsonPropertyAdapter) element$iv$iv;
            String name = it.getName();
            if (name != null) {
                destination$iv.add(name);
            }
        }
        return (Set) destination$iv;
    }

    @Nullable
    public JsonPropertyAdapter getParentPropertyAdapter(@NotNull PsiElement element) {
        Intrinsics.checkNotNullParameter(element, "element");
        TomlKeyValue property = (TomlKeyValue) PsiTreeUtil.getParentOfType(element, TomlKeyValue.class, false);
        if (property == null) {
            return null;
        }
        return new TomlJsonPropertyAdapter(property);
    }

    public boolean isTopJsonElement(@NotNull PsiElement element) {
        Intrinsics.checkNotNullParameter(element, "element");
        return element instanceof TomlFile;
    }

    @Nullable
    public JsonValueAdapter createValueAdapter(@NotNull PsiElement element) {
        Intrinsics.checkNotNullParameter(element, "element");
        if (element instanceof TomlElement) {
            return TomlJsonValueAdapter.Companion.createAdapterByType((TomlElement) element);
        }
        return null;
    }

    @NotNull
    /* renamed from: getRoots */
    public List<PsiElement> m15getRoots(@NotNull PsiFile file) {
        Intrinsics.checkNotNullParameter(file, "file");
        if (file instanceof TomlFile) {
            PsiElement[] children = ((TomlFile) file).getChildren();
            Intrinsics.checkNotNullExpressionValue(children, "getChildren(...)");
            return ArraysKt.toList(children);
        }
        return CollectionsKt.emptyList();
    }

    @Nullable
    public PsiElement getPropertyNameElement(@Nullable PsiElement property) {
        TomlKeyValue tomlKeyValue = property instanceof TomlKeyValue ? (TomlKeyValue) property : null;
        return tomlKeyValue != null ? tomlKeyValue.getKey() : null;
    }

    public boolean hasMissingCommaAfter(@NotNull PsiElement element) {
        Intrinsics.checkNotNullParameter(element, "element");
        return false;
    }

    public boolean acceptsEmptyRoot() {
        return true;
    }

    public boolean requiresNameQuotes() {
        return false;
    }

    public boolean allowsSingleQuotes() {
        return false;
    }
}