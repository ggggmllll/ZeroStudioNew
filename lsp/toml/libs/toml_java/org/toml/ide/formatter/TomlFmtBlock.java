package org.toml.ide.formatter;

import com.intellij.formatting.ASTBlock;
import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.ChildAttributes;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
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
import org.jetbrains.annotations.Nullable;
import org.toml.ide.formatter.impl.IndentKt;
import org.toml.ide.formatter.impl.SpacingKt;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlElementTypes;

/* compiled from: TomlFmtBlock.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��`\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n��\n\u0002\u0010\b\n��\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0010\u000e\n��\u0018��2\u00020\u0001B5\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\b\u0010\b\u001a\u0004\u0018\u00010\t\u0012\u0006\u0010\n\u001a\u00020\u000b¢\u0006\u0004\b\f\u0010\rJ\b\u0010\u000e\u001a\u00020\u0003H\u0016J\b\u0010\u000f\u001a\u00020\u0010H\u0016J\n\u0010\u0011\u001a\u0004\u0018\u00010\u0005H\u0016J\n\u0010\u0012\u001a\u0004\u0018\u00010\u0007H\u0016J\n\u0010\u0013\u001a\u0004\u0018\u00010\tH\u0016J\u000e\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00160\u0015H\u0016J\u000e\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00160\u0015H\u0002J\u001c\u0010\u001d\u001a\u0004\u0018\u00010\u001e2\b\u0010\u001f\u001a\u0004\u0018\u00010\u00162\u0006\u0010 \u001a\u00020\u0016H\u0016J\u0010\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020$H\u0016J\b\u0010%\u001a\u00020&H\u0016J\b\u0010'\u001a\u00020&H\u0016J\b\u0010,\u001a\u00020-H\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n��R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u0004¢\u0006\u0002\n��R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u0004¢\u0006\u0002\n��R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u0004¢\u0006\u0002\n��R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004¢\u0006\u0002\n��R!\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00160\u00158BX\u0082\u0084\u0002¢\u0006\f\n\u0004\b\u001a\u0010\u001b\u001a\u0004\b\u0018\u0010\u0019R\u001b\u0010(\u001a\u00020&8BX\u0082\u0084\u0002¢\u0006\f\n\u0004\b+\u0010\u001b\u001a\u0004\b)\u0010*¨\u0006."}, d2 = {"Lorg/toml/ide/formatter/TomlFmtBlock;", "Lcom/intellij/formatting/ASTBlock;", "node", "Lcom/intellij/lang/ASTNode;", "alignment", "Lcom/intellij/formatting/Alignment;", "indent", "Lcom/intellij/formatting/Indent;", "wrap", "Lcom/intellij/formatting/Wrap;", "ctx", "Lorg/toml/ide/formatter/TomlFmtContext;", "<init>", "(Lcom/intellij/lang/ASTNode;Lcom/intellij/formatting/Alignment;Lcom/intellij/formatting/Indent;Lcom/intellij/formatting/Wrap;Lorg/toml/ide/formatter/TomlFmtContext;)V", "getNode", "getTextRange", "Lcom/intellij/openapi/util/TextRange;", "getAlignment", "getIndent", "getWrap", "getSubBlocks", "", "Lcom/intellij/formatting/Block;", "mySubBlocks", "getMySubBlocks", "()Ljava/util/List;", "mySubBlocks$delegate", "Lkotlin/Lazy;", "buildChildren", "getSpacing", "Lcom/intellij/formatting/Spacing;", "child1", "child2", "getChildAttributes", "Lcom/intellij/formatting/ChildAttributes;", "newChildIndex", "", "isLeaf", "", "isIncomplete", "myIsIncomplete", "getMyIsIncomplete", "()Z", "myIsIncomplete$delegate", "toString", "", "intellij.toml.core"})
@SourceDebugExtension({"SMAP\nTomlFmtBlock.kt\nKotlin\n*S Kotlin\n*F\n+ 1 TomlFmtBlock.kt\norg/toml/ide/formatter/TomlFmtBlock\n+ 2 _Arrays.kt\nkotlin/collections/ArraysKt___ArraysKt\n+ 3 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,64:1\n3829#2:65\n4344#2,2:66\n1557#3:68\n1628#3,3:69\n*S KotlinDebug\n*F\n+ 1 TomlFmtBlock.kt\norg/toml/ide/formatter/TomlFmtBlock\n*L\n35#1:65\n35#1:66,2\n36#1:68\n36#1:69,3\n*E\n"})
public final class TomlFmtBlock implements ASTBlock {
    @NotNull
    private final ASTNode node;
    @Nullable
    private final Alignment alignment;
    @Nullable
    private final Indent indent;
    @Nullable
    private final Wrap wrap;
    @NotNull
    private final TomlFmtContext ctx;
    @NotNull
    private final Lazy mySubBlocks$delegate;
    @NotNull
    private final Lazy myIsIncomplete$delegate;

    public TomlFmtBlock(@NotNull ASTNode node, @Nullable Alignment alignment, @Nullable Indent indent, @Nullable Wrap wrap, @NotNull TomlFmtContext ctx) {
        Intrinsics.checkNotNullParameter(node, "node");
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        this.node = node;
        this.alignment = alignment;
        this.indent = indent;
        this.wrap = wrap;
        this.ctx = ctx;
        this.mySubBlocks$delegate = LazyKt.lazy(() -> {
            return mySubBlocks_delegate$lambda$0(r1);
        });
        this.myIsIncomplete$delegate = LazyKt.lazy(() -> {
            return myIsIncomplete_delegate$lambda$3(r1);
        });
    }

    @NotNull
    public ASTNode getNode() {
        return this.node;
    }

    @NotNull
    public TextRange getTextRange() {
        TextRange textRange = this.node.getTextRange();
        Intrinsics.checkNotNullExpressionValue(textRange, "getTextRange(...)");
        return textRange;
    }

    @Nullable
    public Alignment getAlignment() {
        return this.alignment;
    }

    @Nullable
    public Indent getIndent() {
        return this.indent;
    }

    @Nullable
    public Wrap getWrap() {
        return this.wrap;
    }

    @NotNull
    public List<Block> getSubBlocks() {
        return getMySubBlocks();
    }

    private final List<Block> getMySubBlocks() {
        return (List) this.mySubBlocks$delegate.getValue();
    }

    private static final List mySubBlocks_delegate$lambda$0(TomlFmtBlock this$0) {
        return this$0.buildChildren();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final List<Block> buildChildren() {
        ASTNode[] children = this.node.getChildren((TokenSet) null);
        Intrinsics.checkNotNullExpressionValue(children, "getChildren(...)");
        ASTNode[] aSTNodeArr = children;
        Collection destination$iv$iv = new ArrayList();
        for (ASTNode aSTNode : aSTNodeArr) {
            ASTNode it = aSTNode;
            if (!org.toml.ide.formatter.impl.UtilsKt.isWhitespaceOrEmpty(it)) {
                destination$iv$iv.add(aSTNode);
            }
        }
        Iterable $this$map$iv = (List) destination$iv$iv;
        Collection destination$iv$iv2 = new ArrayList(CollectionsKt.collectionSizeOrDefault($this$map$iv, 10));
        for (Object item$iv$iv : $this$map$iv) {
            ASTNode childNode = (ASTNode) item$iv$iv;
            destination$iv$iv2.add(TomlFormattingModelBuilder.Companion.createBlock(childNode, null, IndentKt.computeIndent(this, childNode), null, this.ctx));
        }
        return (List) destination$iv$iv2;
    }

    @Nullable
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        Intrinsics.checkNotNullParameter(child2, "child2");
        return SpacingKt.computeSpacing((Block) this, child1, child2, this.ctx);
    }

    @NotNull
    public ChildAttributes getChildAttributes(int newChildIndex) {
        Indent indent = Intrinsics.areEqual(this.node.getElementType(), TomlElementTypes.ARRAY) ? Indent.getNormalIndent() : Indent.getNoneIndent();
        return new ChildAttributes(indent, (Alignment) null);
    }

    public boolean isLeaf() {
        return this.node.getFirstChildNode() == null;
    }

    public boolean isIncomplete() {
        return getMyIsIncomplete();
    }

    private final boolean getMyIsIncomplete() {
        return ((Boolean) this.myIsIncomplete$delegate.getValue()).booleanValue();
    }

    private static final boolean myIsIncomplete_delegate$lambda$3(TomlFmtBlock this$0) {
        return FormatterUtil.isIncomplete(this$0.node);
    }

    @NotNull
    public String toString() {
        return this.node.getText() + " " + getTextRange();
    }
}