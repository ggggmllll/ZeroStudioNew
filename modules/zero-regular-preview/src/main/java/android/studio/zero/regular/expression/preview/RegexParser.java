package android.studio.zero.regular.expression.preview;

import android.studio.zero.regular.expression.preview.model.RegexAstNode;
import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;

/**
 * 正则表达式解析器。
 * 通过 JNI 调用 Oniguruma 库，将正则字符串解析为 AST，并转换为 Java 对象模型。
 *
 * @author android_zero
 */
public class RegexParser {
    static {
        // 加载包含 oniguruma 和 zero_preview_jni.c 的动态库
        System.loadLibrary("zero-regular-preview");
    }

    /**
     * JNI Native 方法：解析正则并返回 JSON 字符串。
     *
     * @param pattern 正则表达式内容的字节数组
     * @param encodingType 编码类型索引 (0:UTF-8, 1:ASCII, 2:UTF-16LE, 3:UTF-16BE)
     * @param options 语法选项位掩码
     * @return 描述 AST 的 JSON 字符串
     */
    private static native String nParseRegexToJson(byte[] pattern, int encodingType, int options);

    /**
     * 解析正则表达式。
     *
     * @param pattern 正则表达式字符串
     * @param encodingType 编码类型
     * @param options 编译选项
     * @return 解析后的根节点，如果解析失败返回 null
     */
    public static RegexAstNode parse(String pattern, int encodingType, int options) {
        if (pattern == null || pattern.isEmpty()) {
            return null;
        }
        
        try {
            // 将字符串转换为 UTF-8 字节数组传给 C 层，Oniguruma 处理多字节字符能力极强
            byte[] bytes = pattern.getBytes(StandardCharsets.UTF_8);
            
            // 调用 Native 层
            String json = nParseRegexToJson(bytes, encodingType, options);
            
            if (json == null || json.isEmpty()) {
                return null;
            }
            
            // 反序列化
            return new Gson().fromJson(json, RegexAstNode.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}