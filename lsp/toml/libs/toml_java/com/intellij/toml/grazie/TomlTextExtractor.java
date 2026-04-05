package com.intellij.toml.grazie;

import com.intellij.grazie.text.TextContent;
import com.intellij.grazie.text.TextContentBuilder;
import com.intellij.grazie.text.TextExtractor;
import com.intellij.grazie.utils.PsiUtilsKt;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtilKt;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.lang.lexer._TomlLexer;
import org.toml.lang.psi.TomlLiteral;
import org.toml.lang.psi.ext.TomlLiteralKind;
import org.toml.lang.psi.ext.TomlLiteralKt;

/* compiled from: TomlTextExtractor.kt */
@Metadata(mv = {2, _TomlLexer.YYINITIAL, _TomlLexer.YYINITIAL}, k = IElementType.FIRST_TOKEN_INDEX, xi = 48, d1 = {"��\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0010\"\n\u0002\u0018\u0002\n��\u0018��2\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J \u0010\u0004\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u0006\u001a\u00020\u00072\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\tH\u0014¨\u0006\u000b"}, d2 = {"Lcom/intellij/toml/grazie/TomlTextExtractor;", "Lcom/intellij/grazie/text/TextExtractor;", "<init>", "()V", "buildTextContent", "Lcom/intellij/grazie/text/TextContent;", "element", "Lcom/intellij/psi/PsiElement;", "allowedDomains", "", "Lcom/intellij/grazie/text/TextContent$TextDomain;", "intellij.toml.grazie"})
@SourceDebugExtension({"SMAP\nTomlTextExtractor.kt\nKotlin\n*S Kotlin\n*F\n+ 1 TomlTextExtractor.kt\ncom/intellij/toml/grazie/TomlTextExtractor\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 3 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,33:1\n1611#2,9:34\n1863#2:43\n1864#2:45\n1620#2:46\n1#3:44\n*S KotlinDebug\n*F\n+ 1 TomlTextExtractor.kt\ncom/intellij/toml/grazie/TomlTextExtractor\n*L\n27#1:34,9\n27#1:43\n27#1:45\n27#1:46\n27#1:44\n*E\n"})
public final class TomlTextExtractor extends TextExtractor {
    @Nullable
    protected TextContent buildTextContent(@NotNull PsiElement element, @NotNull Set<? extends TextContent.TextDomain> set) {
        TextContentBuilder textContentBuilder;
        Intrinsics.checkNotNullParameter(element, "element");
        Intrinsics.checkNotNullParameter(set, "allowedDomains");
        if (set.contains(TextContent.TextDomain.LITERALS) && (element instanceof TomlLiteral) && (TomlLiteralKt.getKind((TomlLiteral) element) instanceof TomlLiteralKind.String)) {
            return TextContentBuilder.FromPsi.build(element, TextContent.TextDomain.LITERALS);
        }
        if (set.contains(TextContent.TextDomain.COMMENTS) && (element instanceof PsiComment)) {
            Iterable siblings = PsiUtilsKt.getNotSoDistantSimilarSiblings(element, (v1) -> {
                return buildTextContent$lambda$0(r1, v1);
            });
            Iterable $this$mapNotNull$iv = siblings;
            Collection destination$iv$iv = new ArrayList();
            for (Object element$iv$iv$iv : $this$mapNotNull$iv) {
                PsiElement it = (PsiElement) element$iv$iv$iv;
                textContentBuilder = TomlTextExtractorKt.COMMENT_BUILDER;
                TextContent build = textContentBuilder.build(it, TextContent.TextDomain.COMMENTS);
                if (build != null) {
                    destination$iv$iv.add(build);
                }
            }
            return TextContent.joinWithWhitespace('\n', (List) destination$iv$iv);
        }
        return null;
    }

    private static final boolean buildTextContent$lambda$0(PsiElement $element, PsiElement it) {
        Intrinsics.checkNotNullParameter(it, "it");
        return Intrinsics.areEqual(PsiTreeUtilKt.getElementType(it), PsiTreeUtilKt.getElementType($element));
    }
}