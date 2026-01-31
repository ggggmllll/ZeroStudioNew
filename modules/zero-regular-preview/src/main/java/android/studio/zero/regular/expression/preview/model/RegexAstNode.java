package android.studio.zero.regular.expression.preview.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 正则表达式 AST 节点模型。
 * 对应 C 层 zero_preview_jni.c 生成的 JSON 结构。
 *
 * @author android_zero
 */
public class RegexAstNode {
    // --- 铁路图节点类型常量 (对应 C 代码中的 railType) ---
    public static final String TYPE_LITERAL     = "LITERAL";
    public static final String TYPE_ESCAPE      = "ESCAPE";
    public static final String TYPE_ANY_CHAR    = "ANY_CHAR";
    public static final String TYPE_CHARSET     = "CHARSET";
    public static final String TYPE_SEQUENCE    = "SEQUENCE";
    public static final String TYPE_ALTERNATION = "ALTERNATION";
    public static final String TYPE_QUANTIFIER  = "QUANTIFIER";
    public static final String TYPE_GROUP       = "GROUP";
    public static final String TYPE_ANCHOR      = "ANCHOR";
    public static final String TYPE_LOOKAROUND  = "LOOKAROUND";
    public static final String TYPE_BACKREF     = "BACKREF";
    public static final String TYPE_UNKNOWN     = "UNKNOWN";

    // --- 基础字段 ---
    @SerializedName("railType")
    public String railType;

    @SerializedName("rawType")
    public int rawType; // Oniguruma 原始类型 ID，用于调试

    @SerializedName("error")
    public String error;

    // --- 1. Literal (字面量) ---
    @SerializedName("text")
    public String text;

    // --- 2. Sequence / Alternation (序列/分支) ---
    @SerializedName("children")
    public List<RegexAstNode> children; // 用于 SEQUENCE

    @SerializedName("left")
    public RegexAstNode left; // 用于 ALTERNATION

    @SerializedName("right")
    public RegexAstNode right; // 用于 ALTERNATION

    // --- 3. Quantifier / Group / Anchor (容器/修饰) ---
    @SerializedName("body")
    public RegexAstNode body;

    // --- Quantifier (量词) ---
    @SerializedName("min")
    public int min;

    @SerializedName("max")
    public int max; // -1 代表无限

    @SerializedName("greedy")
    public boolean greedy;

    // --- Group (分组) ---
    @SerializedName("isCapture")
    public boolean isCapture;

    @SerializedName("groupNum")
    public int groupNum;

    @SerializedName("subType")
    public String groupSubType; // 用于原子组(atomic)或选项(option)的文本描述

    // --- Charset (字符集) ---
    @SerializedName("invert")
    public boolean invert; // [^...] 或 \D, \W

    @SerializedName("ranges")
    public List<String> ranges; // 例如 ["a-z", "0-9", "_"]

    // --- Escape / CType (转义) ---
    @SerializedName("escType")
    public int escType; // ONIGENC_CTYPE_xxx 常量

    // --- Anchor (锚点) ---
    @SerializedName("subType")
    public int anchorType; // ANCHOR_xxx 位掩码

    // --- BackRef (反向引用) ---
    @SerializedName("index")
    public int backRefIndex;
}