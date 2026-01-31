package com.itsaky.androidide.lsp.utils

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import com.itsaky.androidide.lsp.util.Logger
import io.github.rosemoe.sora.lang.completion.CompletionItem
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

/**
 * 补全项排序与高亮工具。
 * 
 * ## 功能描述
 * 1. 使用 FuzzyScorer 过滤不匹配的项。
 * 2. 根据分值进行降序排列。
 * 3. 在 Label 中使用 Spannable 高亮匹配部分。
 * 
 * @author android_zero
 */
object CompletionSorter {

    private val LOG = Logger.instance("CompletionSorter")

    /**
     * 对补全项进行排序并应用匹配高亮。
     * 
     * @param items 原始补全项列表
     * @param prefix 用户当前输入的字符前缀
     * @param colorScheme 颜色方案，用于获取高亮颜色
     */
    fun sortAndHighlight(
        items: List<CompletionItem>,
        prefix: String,
        colorScheme: EditorColorScheme
    ): List<CompletionItem> {
        if (prefix.isEmpty()) return items

        val highlightColor = colorScheme.getColor(EditorColorScheme.COMPLETION_WND_TEXT_MATCHED)
        
        return items.mapNotNull { item ->
            val scoreResult = FuzzyScorer.score(prefix, item.label.toString())
            if (scoreResult != null) {
                // 保存分值到 extra 以供比较
                item.extra = scoreResult
                
                // 应用高亮样式
                val spannable = SpannableString(item.label)
                scoreResult.matches.forEach { idx ->
                    if (idx < spannable.length) {
                        spannable.setSpan(
                            ForegroundColorSpan(highlightColor),
                            idx, idx + 1,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                item.label = spannable
                item
            } else {
                null
            }
        }.sortedByDescending { 
            (it.extra as? FuzzyScore)?.score ?: 0 
        }
    }
}