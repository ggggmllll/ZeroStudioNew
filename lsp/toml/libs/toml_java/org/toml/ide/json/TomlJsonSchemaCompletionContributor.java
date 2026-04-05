package org.toml.ide.json;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.json.pointer.JsonPointerPosition;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.IconManager;
import com.intellij.ui.PlatformIcons;
import com.intellij.util.Consumer;
import com.intellij.util.ThreeState;
import com.jetbrains.jsonSchema.extension.JsonLikePsiWalker;
import com.jetbrains.jsonSchema.extension.adapters.JsonPropertyAdapter;
import com.jetbrains.jsonSchema.extension.adapters.JsonValueAdapter;
import com.jetbrains.jsonSchema.ide.JsonSchemaService;
import com.jetbrains.jsonSchema.impl.JsonSchemaDocumentationProvider;
import com.jetbrains.jsonSchema.impl.JsonSchemaObject;
import com.jetbrains.jsonSchema.impl.JsonSchemaResolver;
import com.jetbrains.jsonSchema.impl.JsonSchemaType;
import com.jetbrains.jsonSchema.impl.light.legacy.JsonSchemaObjectReadingUtils;
import com.jetbrains.jsonSchema.impl.tree.JsonSchemaNodeExpansionRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.text.StringsKt;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.ide.experiments.TomlExperiments;
import org.toml.ide.json.TomlJsonSchemaCompletionFileFilter;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlLiteral;
import org.toml.lang.psi.TomlTableHeader;
import org.toml.lang.psi.ext.TomlLiteralKind;
import org.toml.lang.psi.ext.TomlLiteralKt;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: TomlJsonSchemaCompletionContributor.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"�� \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0002\u0018�� \u000b2\u00020\u0001:\u0002\n\u000bB\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0018\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH\u0016¨\u0006\f"}, d2 = {"Lorg/toml/ide/json/TomlJsonSchemaCompletionContributor;", "Lcom/intellij/codeInsight/completion/CompletionContributor;", "<init>", "()V", "fillCompletionVariants", "", "parameters", "Lcom/intellij/codeInsight/completion/CompletionParameters;", "result", "Lcom/intellij/codeInsight/completion/CompletionResultSet;", "Worker", "Companion", "intellij.toml.json"})
public final class TomlJsonSchemaCompletionContributor extends CompletionContributor {
    private static final double LOW_PRIORITY = -1000.0d;
    @NotNull
    public static final Companion Companion = new Companion(null);
    @NotNull
    private static final List<JsonSchemaType> JSON_COMPOUND_TYPES = CollectionsKt.listOf(new JsonSchemaType[]{JsonSchemaType._array, JsonSchemaType._object, JsonSchemaType._any, null});

    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        Intrinsics.checkNotNullParameter(parameters, "parameters");
        Intrinsics.checkNotNullParameter(result, "result");
        if (TomlExperiments.INSTANCE.isJsonSchemaEnabled()) {
            TomlJsonSchemaCompletionFileFilter.Companion companion = TomlJsonSchemaCompletionFileFilter.Companion;
            PsiFile originalFile = parameters.getOriginalFile();
            Intrinsics.checkNotNullExpressionValue(originalFile, "getOriginalFile(...)");
            if (companion.shouldCompleteInFile(originalFile)) {
                PsiElement position = parameters.getPosition();
                Intrinsics.checkNotNullExpressionValue(position, "getPosition(...)");
                JsonSchemaService jsonSchemaService = JsonSchemaService.Impl.get(position.getProject());
                JsonSchemaObject jsonSchemaObject = jsonSchemaService.getSchemaObject(parameters.getOriginalFile());
                if (jsonSchemaObject != null) {
                    PsiElement originalPosition = parameters.getOriginalPosition();
                    if (originalPosition == null) {
                        originalPosition = parameters.getPosition();
                        Intrinsics.checkNotNullExpressionValue(originalPosition, "getPosition(...)");
                    }
                    PsiElement completionPosition = originalPosition;
                    PsiElement position2 = parameters.getPosition();
                    Intrinsics.checkNotNullExpressionValue(position2, "getPosition(...)");
                    Worker worker = new Worker(jsonSchemaObject, position2, completionPosition, (Consumer) result);
                    worker.work();
                }
            }
        }
    }

    /* compiled from: TomlJsonSchemaCompletionContributor.kt */
    @Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��x\n\u0002\u0018\u0002\n\u0002\u0010��\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010#\n\u0002\b\u0003\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u001e\n\u0002\u0010\u000e\n��\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n��\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010 \n��\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\b\u0002\u0018��2\u00020\u0001B/\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u0012\u000e\u0010\u0007\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\b¢\u0006\u0004\b\n\u0010\u000bJ\u0006\u0010\u0014\u001a\u00020\u0015J>\u0010\u0016\u001a\u00020\u00152\u0006\u0010\u0017\u001a\u00020\u00032\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u001a0\u00192\b\u0010\u001b\u001a\u0004\u0018\u00010\u001c2\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u001a0\r2\u0006\u0010\u0006\u001a\u00020\u0005H\u0002J\"\u0010\u001e\u001a\u00020\u00152\u0006\u0010\u001f\u001a\u00020\u001a2\u0006\u0010 \u001a\u00020\u00032\b\u0010!\u001a\u0004\u0018\u00010\"H\u0002J\u0018\u0010&\u001a\u00020\u00152\u0006\u0010\u0017\u001a\u00020\u00032\u0006\u0010'\u001a\u00020$H\u0002J\u0018\u0010(\u001a\b\u0012\u0004\u0012\u00020\t0)2\b\u0010*\u001a\u0004\u0018\u00010+H\u0002J\u0010\u0010,\u001a\u00020\t2\u0006\u0010-\u001a\u00020\u001aH\u0002J\u0017\u0010.\u001a\u00070/¢\u0006\u0002\b02\b\u0010*\u001a\u0004\u0018\u00010+H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n��R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n��R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n��R\u0016\u0010\u0007\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\bX\u0082\u0004¢\u0006\u0002\n��R\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\t0\r¢\u0006\b\n��\u001a\u0004\b\u000e\u0010\u000fR\u0010\u0010\u0010\u001a\u0004\u0018\u00010\u0011X\u0082\u0004¢\u0006\u0002\n��R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004¢\u0006\u0002\n��R\u0014\u0010#\u001a\u00020$8BX\u0082\u0004¢\u0006\u0006\u001a\u0004\b#\u0010%¨\u00061"}, d2 = {"Lorg/toml/ide/json/TomlJsonSchemaCompletionContributor$Worker;", "", "rootSchema", "Lcom/jetbrains/jsonSchema/impl/JsonSchemaObject;", "position", "Lcom/intellij/psi/PsiElement;", "originalPosition", "resultConsumer", "Lcom/intellij/util/Consumer;", "Lcom/intellij/codeInsight/lookup/LookupElement;", "<init>", "(Lcom/jetbrains/jsonSchema/impl/JsonSchemaObject;Lcom/intellij/psi/PsiElement;Lcom/intellij/psi/PsiElement;Lcom/intellij/util/Consumer;)V", "variants", "", "getVariants", "()Ljava/util/Set;", "walker", "Lcom/jetbrains/jsonSchema/extension/JsonLikePsiWalker;", "project", "Lcom/intellij/openapi/project/Project;", "work", "", "addAllPropertyVariants", "schema", "properties", "", "", "adapter", "Lcom/jetbrains/jsonSchema/extension/adapters/JsonPropertyAdapter;", "knownNames", "addPropertyVariant", "key", "jsonSchemaObject", "originalPositionAdapter", "Lcom/jetbrains/jsonSchema/extension/adapters/JsonValueAdapter;", "isInsideStringLiteral", "", "()Z", "suggestValues", "isSurelyValue", "suggestValuesByType", "", "type", "Lcom/jetbrains/jsonSchema/impl/JsonSchemaType;", "buildPairLookupElement", "element", "getIconForType", "Ljavax/swing/Icon;", "Lorg/jetbrains/annotations/NotNull;", "intellij.toml.json"})
    @SourceDebugExtension({"SMAP\nTomlJsonSchemaCompletionContributor.kt\nKotlin\n*S Kotlin\n*F\n+ 1 TomlJsonSchemaCompletionContributor.kt\norg/toml/ide/json/TomlJsonSchemaCompletionContributor$Worker\n+ 2 psiTreeUtil.kt\ncom/intellij/psi/util/PsiTreeUtilKt\n+ 3 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,195:1\n66#2,2:196\n1557#3:198\n1628#3,3:199\n*S KotlinDebug\n*F\n+ 1 TomlJsonSchemaCompletionContributor.kt\norg/toml/ide/json/TomlJsonSchemaCompletionContributor$Worker\n*L\n113#1:196,2\n166#1:198\n166#1:199,3\n*E\n"})
    /* loaded from: 2026040502522302a7da3a-171e-4c6c-a1b5-134e29fd6e0f.jar:org/toml/ide/json/TomlJsonSchemaCompletionContributor$Worker.class */
    private static final class Worker {
        @NotNull
        private final JsonSchemaObject rootSchema;
        @NotNull
        private final PsiElement position;
        @NotNull
        private final PsiElement originalPosition;
        @NotNull
        private final Consumer<LookupElement> resultConsumer;
        @NotNull
        private final Set<LookupElement> variants;
        @Nullable
        private final JsonLikePsiWalker walker;
        @NotNull
        private final Project project;

        /* compiled from: TomlJsonSchemaCompletionContributor.kt */
        @Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = 3, xi = 48)
        /* loaded from: 2026040502522302a7da3a-171e-4c6c-a1b5-134e29fd6e0f.jar:org/toml/ide/json/TomlJsonSchemaCompletionContributor$Worker$WhenMappings.class */
        public /* synthetic */ class WhenMappings {
            public static final /* synthetic */ int[] $EnumSwitchMapping$0;

            static {
                int[] iArr = new int[JsonSchemaType.values().length];
                try {
                    iArr[JsonSchemaType._object.ordinal()] = 1;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[JsonSchemaType._array.ordinal()] = 2;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[JsonSchemaType._string.ordinal()] = 3;
                } catch (NoSuchFieldError e3) {
                }
                try {
                    iArr[JsonSchemaType._boolean.ordinal()] = 4;
                } catch (NoSuchFieldError e4) {
                }
                $EnumSwitchMapping$0 = iArr;
            }
        }

        public Worker(@NotNull JsonSchemaObject rootSchema, @NotNull PsiElement position, @NotNull PsiElement originalPosition, @NotNull Consumer<LookupElement> consumer) {
            Intrinsics.checkNotNullParameter(rootSchema, "rootSchema");
            Intrinsics.checkNotNullParameter(position, "position");
            Intrinsics.checkNotNullParameter(originalPosition, "originalPosition");
            Intrinsics.checkNotNullParameter(consumer, "resultConsumer");
            this.rootSchema = rootSchema;
            this.position = position;
            this.originalPosition = originalPosition;
            this.resultConsumer = consumer;
            this.variants = new LinkedHashSet();
            this.walker = JsonLikePsiWalker.getWalker(this.position, this.rootSchema);
            Project project = this.originalPosition.getProject();
            Intrinsics.checkNotNullExpressionValue(project, "getProject(...)");
            this.project = project;
        }

        @NotNull
        public final Set<LookupElement> getVariants() {
            return this.variants;
        }

        public final void work() {
            PsiElement checkable;
            JsonLikePsiWalker jsonLikePsiWalker = this.walker;
            if (jsonLikePsiWalker == null || (checkable = jsonLikePsiWalker.findElementToCheck(this.position)) == null) {
                return;
            }
            ThreeState isName = this.walker.isName(checkable);
            JsonPointerPosition pointerPosition = this.walker.findPosition(checkable, isName == ThreeState.NO);
            if (pointerPosition != null) {
                if (pointerPosition.isEmpty() && isName == ThreeState.NO) {
                    return;
                }
                JsonSchemaNodeExpansionRequest expansionRequest = new JsonSchemaNodeExpansionRequest(this.walker.createValueAdapter(checkable), false);
                Collection<JsonSchemaObject> schemas = new JsonSchemaResolver(this.project, this.rootSchema, pointerPosition, expansionRequest).resolve();
                Intrinsics.checkNotNullExpressionValue(schemas, "resolve(...)");
                HashSet knownNames = new HashSet();
                for (JsonSchemaObject schema : schemas) {
                    if (isName != ThreeState.NO) {
                        Set properties = this.walker.getPropertyNamesOfParentObject(this.originalPosition, this.position);
                        JsonPropertyAdapter adapter = this.walker.getParentPropertyAdapter(checkable);
                        Intrinsics.checkNotNull(schema);
                        Intrinsics.checkNotNull(properties);
                        addAllPropertyVariants(schema, properties, adapter, knownNames, this.originalPosition);
                    }
                    if (isName != ThreeState.YES) {
                        Intrinsics.checkNotNull(schema);
                        suggestValues(schema, isName == ThreeState.NO);
                    }
                }
                for (LookupElement variant : this.variants) {
                    this.resultConsumer.consume(variant);
                }
            }
        }

        private final void addAllPropertyVariants(JsonSchemaObject schema, Collection<String> collection, JsonPropertyAdapter adapter, Set<String> set, PsiElement originalPosition) {
            StreamEx of = StreamEx.of(schema.getPropertyNames());
            Function1 function1 = (v3) -> {
                return addAllPropertyVariants$lambda$0(r1, r2, r3, v3);
            };
            StreamEx variants = of.filter((v1) -> {
                return addAllPropertyVariants$lambda$1(r1, v1);
            });
            Iterator it = variants.iterator();
            Intrinsics.checkNotNullExpressionValue(it, "iterator(...)");
            while (it.hasNext()) {
                String variant = (String) it.next();
                Intrinsics.checkNotNull(variant);
                set.add(variant);
                JsonSchemaObject jsonSchemaObject = schema.getPropertyByName(variant);
                if (jsonSchemaObject != null) {
                    boolean isTomlHeader = PsiTreeUtil.getParentOfType(originalPosition, TomlTableHeader.class, true) != null;
                    if (!isTomlHeader || TomlJsonSchemaCompletionContributor.JSON_COMPOUND_TYPES.contains(JsonSchemaObjectReadingUtils.guessType(jsonSchemaObject))) {
                        addPropertyVariant(variant, jsonSchemaObject, adapter != null ? adapter.getNameValueAdapter() : null);
                    }
                }
            }
        }

        private static final boolean addAllPropertyVariants$lambda$1(Function1 $tmp0, Object p0) {
            return ((Boolean) $tmp0.invoke(p0)).booleanValue();
        }

        private static final boolean addAllPropertyVariants$lambda$0(Collection $properties, Set $knownNames, JsonPropertyAdapter $adapter, String name) {
            if ($properties.contains(name) || $knownNames.contains(name)) {
                if (!Intrinsics.areEqual(name, $adapter != null ? $adapter.getName() : null)) {
                    return false;
                }
            }
            return true;
        }

        private final void addPropertyVariant(String key, JsonSchemaObject jsonSchemaObject, JsonValueAdapter originalPositionAdapter) {
            Collection currentVariants = new JsonSchemaResolver(this.project, jsonSchemaObject, new JsonPointerPosition(), new JsonSchemaNodeExpansionRequest(originalPositionAdapter, false)).resolve();
            Intrinsics.checkNotNullExpressionValue(currentVariants, "resolve(...)");
            JsonSchemaObject jsonSchemaObject2 = (JsonSchemaObject) CollectionsKt.firstOrNull(currentVariants);
            if (jsonSchemaObject2 == null) {
                jsonSchemaObject2 = jsonSchemaObject;
            }
            JsonSchemaObject jsonSchemaObject3 = jsonSchemaObject2;
            String description = JsonSchemaDocumentationProvider.getBestDocumentation(true, jsonSchemaObject3);
            String str = description;
            if (str == null || StringsKt.isBlank(str)) {
                String typeDescription = JsonSchemaObjectReadingUtils.getTypeDescription(jsonSchemaObject3, true);
                if (typeDescription == null) {
                    typeDescription = "";
                }
                description = typeDescription;
            }
            LookupElement withIcon = LookupElementBuilder.create(key).withTypeText(description).withIcon(getIconForType(JsonSchemaObjectReadingUtils.guessType(jsonSchemaObject3)));
            Intrinsics.checkNotNullExpressionValue(withIcon, "withIcon(...)");
            this.variants.add(withIcon);
        }

        private final boolean isInsideStringLiteral() {
            PsiElement parent = this.position.getParent();
            TomlLiteral tomlLiteral = parent instanceof TomlLiteral ? (TomlLiteral) parent : null;
            return (tomlLiteral != null ? TomlLiteralKt.getKind(tomlLiteral) : null) instanceof TomlLiteralKind.String;
        }

        private final void suggestValues(JsonSchemaObject schema, boolean isSurelyValue) {
            String obj;
            List enumVariants = schema.getEnum();
            if (enumVariants != null) {
                for (Object o : enumVariants) {
                    if (!isInsideStringLiteral() || (o instanceof String)) {
                        if (isInsideStringLiteral()) {
                            obj = StringUtil.unquoteString(o.toString());
                        } else {
                            obj = o.toString();
                        }
                        String variant = obj;
                        Intrinsics.checkNotNull(variant);
                        Set<LookupElement> set = this.variants;
                        LookupElementBuilder create = LookupElementBuilder.create(variant);
                        Intrinsics.checkNotNullExpressionValue(create, "create(...)");
                        set.add(create);
                    }
                }
            } else if (isSurelyValue && !isInsideStringLiteral()) {
                this.variants.addAll(suggestValuesByType(JsonSchemaObjectReadingUtils.guessType(schema)));
            }
        }

        private final List<LookupElement> suggestValuesByType(JsonSchemaType type) {
            switch (type == null ? -1 : WhenMappings.$EnumSwitchMapping$0[type.ordinal()]) {
                case IElementType.FIRST_TOKEN_INDEX /* 1 */:
                    return CollectionsKt.listOf(buildPairLookupElement("{}"));
                case 2:
                    return CollectionsKt.listOf(buildPairLookupElement("[]"));
                case 3:
                    return CollectionsKt.listOf(buildPairLookupElement("\"\""));
                case 4:
                    Iterable $this$map$iv = CollectionsKt.listOf(new String[]{"true", "false"});
                    Collection destination$iv$iv = new ArrayList(CollectionsKt.collectionSizeOrDefault($this$map$iv, 10));
                    for (Object item$iv$iv : $this$map$iv) {
                        String it = (String) item$iv$iv;
                        destination$iv$iv.add(LookupElementBuilder.create(it));
                    }
                    return (List) destination$iv$iv;
                default:
                    return CollectionsKt.emptyList();
            }
        }

        private final LookupElement buildPairLookupElement(String element) {
            LookupElement withInsertHandler = LookupElementBuilder.create(element).withInsertHandler(Worker::buildPairLookupElement$lambda$3);
            Intrinsics.checkNotNullExpressionValue(withInsertHandler, "withInsertHandler(...)");
            LookupElement withPriority = PrioritizedLookupElement.withPriority(withInsertHandler, (double) TomlJsonSchemaCompletionContributor.LOW_PRIORITY);
            Intrinsics.checkNotNullExpressionValue(withPriority, "withPriority(...)");
            return withPriority;
        }

        private static final void buildPairLookupElement$lambda$3(InsertionContext context, LookupElement lookupElement) {
            Intrinsics.checkNotNullParameter(context, "context");
            Intrinsics.checkNotNullParameter(lookupElement, "<unused var>");
            EditorModificationUtil.moveCaretRelatively(context.getEditor(), -1);
        }

        private final Icon getIconForType(JsonSchemaType type) {
            Icon platformIcon;
            switch (type == null ? -1 : WhenMappings.$EnumSwitchMapping$0[type.ordinal()]) {
                case IElementType.FIRST_TOKEN_INDEX /* 1 */:
                    platformIcon = AllIcons.Json.Object;
                    break;
                case 2:
                    platformIcon = AllIcons.Json.Array;
                    break;
                default:
                    platformIcon = IconManager.Companion.getInstance().getPlatformIcon(PlatformIcons.Property);
                    break;
            }
            Icon icon = platformIcon;
            Intrinsics.checkNotNull(icon);
            return icon;
        }
    }

    /* compiled from: TomlJsonSchemaCompletionContributor.kt */
    @Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��\u001c\n\u0002\u0018\u0002\n\u0002\u0010��\n\u0002\b\u0003\n\u0002\u0010\u0006\n��\n\u0002\u0010 \n\u0002\u0018\u0002\n��\b\u0086\u0003\u0018��2\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n��R\u0016\u0010\u0006\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\b0\u0007X\u0082\u0004¢\u0006\u0002\n��¨\u0006\t"}, d2 = {"Lorg/toml/ide/json/TomlJsonSchemaCompletionContributor$Companion;", "", "<init>", "()V", "LOW_PRIORITY", "", "JSON_COMPOUND_TYPES", "", "Lcom/jetbrains/jsonSchema/impl/JsonSchemaType;", "intellij.toml.json"})
    /* loaded from: 2026040502522302a7da3a-171e-4c6c-a1b5-134e29fd6e0f.jar:org/toml/ide/json/TomlJsonSchemaCompletionContributor$Companion.class */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker $constructor_marker) {
            this();
        }

        private Companion() {
        }
    }
}