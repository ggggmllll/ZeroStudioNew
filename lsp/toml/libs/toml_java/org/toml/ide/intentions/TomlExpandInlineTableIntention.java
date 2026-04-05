package org.toml.ide.intentions;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtilKt;
import java.util.Iterator;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.sequences.Sequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.TomlBundle;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlFile;
import org.toml.lang.psi.TomlHeaderOwner;
import org.toml.lang.psi.TomlInlineTable;
import org.toml.lang.psi.TomlKey;
import org.toml.lang.psi.TomlKeyValue;
import org.toml.lang.psi.TomlKeyValueOwner;
import org.toml.lang.psi.TomlPsiFactory;
import org.toml.lang.psi.TomlTable;
import org.toml.lang.psi.TomlTableHeader;
import org.toml.lang.psi.TomlValue;

/* compiled from: TomlExpandInlineTableIntention.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��2\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0010\u0002\n\u0002\b\u0003\u0018��2\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001\u0012B\u0007¢\u0006\u0004\b\u0003\u0010\u0004J\b\u0010\u0005\u001a\u00020\u0006H\u0016J\b\u0010\u0007\u001a\u00020\u0006H\u0016J\"\u0010\b\u001a\u0004\u0018\u00010\u00022\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0016J \u0010\u000f\u001a\u00020\u00102\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\u0002H\u0016¨\u0006\u0013"}, d2 = {"Lorg/toml/ide/intentions/TomlExpandInlineTableIntention;", "Lorg/toml/ide/intentions/TomlElementBaseIntentionAction;", "Lorg/toml/ide/intentions/TomlExpandInlineTableIntention$Context;", "<init>", "()V", "getText", "", "getFamilyName", "findApplicableContext", "project", "Lcom/intellij/openapi/project/Project;", "editor", "Lcom/intellij/openapi/editor/Editor;", "element", "Lcom/intellij/psi/PsiElement;", "invoke", "", "ctx", "Context", "intellij.toml.core"})
@SourceDebugExtension({"SMAP\nTomlExpandInlineTableIntention.kt\nKotlin\n*S Kotlin\n*F\n+ 1 TomlExpandInlineTableIntention.kt\norg/toml/ide/intentions/TomlExpandInlineTableIntention\n+ 2 psiTreeUtil.kt\ncom/intellij/psi/util/PsiTreeUtilKt\n+ 3 _Sequences.kt\nkotlin/sequences/SequencesKt___SequencesKt\n*L\n1#1,70:1\n80#2:71\n183#3,2:72\n*S KotlinDebug\n*F\n+ 1 TomlExpandInlineTableIntention.kt\norg/toml/ide/intentions/TomlExpandInlineTableIntention\n*L\n21#1:71\n21#1:72,2\n*E\n"})
public final class TomlExpandInlineTableIntention extends TomlElementBaseIntentionAction<Context> {
    @NotNull
    public String getText() {
        return TomlBundle.INSTANCE.message("intention.toml.name.expand.into.separate.table", new Object[0]);
    }

    @NotNull
    public String getFamilyName() {
        return getText();
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // org.toml.ide.intentions.TomlElementBaseIntentionAction
    @Nullable
    public Context findApplicableContext(@NotNull Project project, @NotNull Editor editor, @NotNull PsiElement element) {
        Object obj;
        boolean z;
        Intrinsics.checkNotNullParameter(project, "project");
        Intrinsics.checkNotNullParameter(editor, "editor");
        Intrinsics.checkNotNullParameter(element, "element");
        Sequence $this$firstOrNull$iv = PsiTreeUtilKt.parentsOfType(element, TomlKeyValue.class, true);
        Iterator it = $this$firstOrNull$iv.iterator();
        while (true) {
            if (!it.hasNext()) {
                obj = null;
                break;
            }
            Object element$iv = it.next();
            TomlKeyValue it2 = (TomlKeyValue) element$iv;
            boolean hasInlineTableValue = it2.getValue() instanceof TomlInlineTable;
            boolean hasTableOrArrayParent = (it2.getParent() instanceof TomlKeyValueOwner) && (it2.getParent() instanceof TomlHeaderOwner);
            boolean hasTomlFileParent = it2.getParent() instanceof TomlFile;
            if (hasInlineTableValue && (hasTableOrArrayParent || hasTomlFileParent)) {
                z = true;
                continue;
            } else {
                z = false;
                continue;
            }
            if (z) {
                obj = element$iv;
                break;
            }
        }
        TomlKeyValue keyValue = (TomlKeyValue) obj;
        if (keyValue == null) {
            return null;
        }
        TomlValue value = keyValue.getValue();
        Intrinsics.checkNotNull(value, "null cannot be cast to non-null type org.toml.lang.psi.TomlInlineTable");
        TomlInlineTable inlineTable = (TomlInlineTable) value;
        PsiElement parent = keyValue.getParent();
        TomlKeyValueOwner parentTable = parent instanceof TomlKeyValueOwner ? (TomlKeyValueOwner) parent : null;
        return new Context(keyValue, inlineTable, parentTable);
    }

    @Override // org.toml.ide.intentions.TomlElementBaseIntentionAction
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull Context ctx) {
        String str;
        int endOffset;
        TomlTableHeader parentHeader;
        String parentTableKey;
        Intrinsics.checkNotNullParameter(project, "project");
        Intrinsics.checkNotNullParameter(editor, "editor");
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        String key = ctx.getKeyValue().getKey().getText();
        TomlKeyValueOwner parentTable = ctx.getParentTable();
        if (parentTable != null) {
            TomlHeaderOwner tomlHeaderOwner = parentTable instanceof TomlHeaderOwner ? (TomlHeaderOwner) parentTable : null;
            if (tomlHeaderOwner == null || (parentHeader = tomlHeaderOwner.getHeader()) == null) {
                return;
            }
            TomlKey key2 = parentHeader.getKey();
            if (key2 == null || (parentTableKey = key2.getText()) == null) {
                return;
            }
            str = parentTableKey + "." + key;
        } else {
            str = key;
        }
        String newTableKey = str;
        TomlPsiFactory tomlPsiFactory = new TomlPsiFactory(project, false, 2, null);
        Intrinsics.checkNotNull(newTableKey);
        TomlTable newTable = tomlPsiFactory.createTable(newTableKey);
        TomlPsiFactory psiFactory = new TomlPsiFactory(project, false, 2, null);
        for (TomlKeyValue entry : ctx.getInlineTable().getEntries()) {
            newTable.add(psiFactory.createNewline());
            newTable.add(entry.copy());
        }
        if (parentTable != null) {
            ctx.getKeyValue().delete();
            PsiElement parent = parentTable.getParent();
            PsiElement addedTable = parent.addAfter(newTable, parentTable);
            parent.addAfter(psiFactory.createWhitespace("\n\n"), parentTable);
            Intrinsics.checkNotNull(addedTable);
            endOffset = PsiTreeUtilKt.getEndOffset(addedTable);
        } else {
            PsiElement replace = ctx.getKeyValue().replace(newTable);
            Intrinsics.checkNotNullExpressionValue(replace, "replace(...)");
            endOffset = PsiTreeUtilKt.getEndOffset(replace);
        }
        int addedTableOffset = endOffset;
        editor.getCaretModel().moveToOffset(addedTableOffset);
    }

    /* compiled from: TomlExpandInlineTableIntention.kt */
    @Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��\u001e\n\u0002\u0018\u0002\n\u0002\u0010��\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\t\u0018��2\u00020\u0001B!\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007¢\u0006\u0004\b\b\u0010\tR\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n��\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0004\u001a\u00020\u0005¢\u0006\b\n��\u001a\u0004\b\f\u0010\rR\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0007¢\u0006\b\n��\u001a\u0004\b\u000e\u0010\u000f¨\u0006\u0010"}, d2 = {"Lorg/toml/ide/intentions/TomlExpandInlineTableIntention$Context;", "", "keyValue", "Lorg/toml/lang/psi/TomlKeyValue;", "inlineTable", "Lorg/toml/lang/psi/TomlInlineTable;", "parentTable", "Lorg/toml/lang/psi/TomlKeyValueOwner;", "<init>", "(Lorg/toml/lang/psi/TomlKeyValue;Lorg/toml/lang/psi/TomlInlineTable;Lorg/toml/lang/psi/TomlKeyValueOwner;)V", "getKeyValue", "()Lorg/toml/lang/psi/TomlKeyValue;", "getInlineTable", "()Lorg/toml/lang/psi/TomlInlineTable;", "getParentTable", "()Lorg/toml/lang/psi/TomlKeyValueOwner;", "intellij.toml.core"})
    /* loaded from: 2026040502522302a7da3a-171e-4c6c-a1b5-134e29fd6e0f.jar:org/toml/ide/intentions/TomlExpandInlineTableIntention$Context.class */
    public static final class Context {
        @NotNull
        private final TomlKeyValue keyValue;
        @NotNull
        private final TomlInlineTable inlineTable;
        @Nullable
        private final TomlKeyValueOwner parentTable;

        public Context(@NotNull TomlKeyValue keyValue, @NotNull TomlInlineTable inlineTable, @Nullable TomlKeyValueOwner parentTable) {
            Intrinsics.checkNotNullParameter(keyValue, "keyValue");
            Intrinsics.checkNotNullParameter(inlineTable, "inlineTable");
            this.keyValue = keyValue;
            this.inlineTable = inlineTable;
            this.parentTable = parentTable;
        }

        @NotNull
        public final TomlKeyValue getKeyValue() {
            return this.keyValue;
        }

        @NotNull
        public final TomlInlineTable getInlineTable() {
            return this.inlineTable;
        }

        @Nullable
        public final TomlKeyValueOwner getParentTable() {
            return this.parentTable;
        }
    }
}