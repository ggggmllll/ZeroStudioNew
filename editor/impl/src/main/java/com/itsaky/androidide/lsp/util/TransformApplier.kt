package com.itsaky.androidide.lsp.util

import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.eclipse.lsp4j.jsonrpc.messages.Either

/**
 * Snippet 转换器。
 * 
 * ## 用途
 * 处理 LSP 代码片段中的 Transform 逻辑。
 * 例如：`${TM_FILENAME_BASE/(.*)/${1:/upcase}/}` 将文件名转换为大写。
 * 
 * @author android_zero
 */
object TransformApplier {

    /**
     * 对文本应用正则表达式转换。
     * 
     * @param text 原始变量文本
     * @param regexp 正则表达式模式
     * @param format 格式化字符串或占位符逻辑
     * @param options 正则选项（如 "g", "i"）
     */
    fun doTransform(text: String, regexp: String, format: String, options: String): String {
        return try {
            val flags = if (options.contains("i")) Pattern.CASE_INSENSITIVE else 0
            val pattern = Pattern.compile(regexp, flags)
            val matcher = pattern.matcher(text)
            
            if (options.contains("g")) {
                matcher.replaceAll { result ->
                    processFormat(result, format)
                }
            } else if (matcher.find()) {
                val prefix = text.substring(0, matcher.start())
                val transformed = processFormat(matcher, format)
                val suffix = text.substring(matcher.end())
                prefix + transformed + suffix
            } else {
                text
            }
        } catch (e: Exception) {
            text // 转换失败返回原样
        }
    }

    private fun processFormat(matcher: Matcher, format: String): String {
        // 这里实现了简单的占位符替换和转换
        var result = format
        // 1. 处理 /upcase 等转换指令
        if (result.contains("/upcase")) {
            return matcher.group(1)?.uppercase(Locale.ROOT) ?: ""
        }
        if (result.contains("/downcase")) {
            return matcher.group(1)?.lowercase(Locale.ROOT) ?: ""
        }
        // 2. 处理 $1, $2 等捕获组引用
        for (i in 0..matcher.groupCount()) {
            result = result.replace("$$i", matcher.group(i) ?: "")
        }
        return result
    }
}