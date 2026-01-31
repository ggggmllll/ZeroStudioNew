package com.itsaky.androidide.lsp.utils

import io.github.rosemoe.sora.lang.completion.CompletionItem
import java.util.*

/**
 * 模糊匹配分值模型。
 * 移植自 Xed-Editor / VS Code 算法。
 * 
 * @author android_zero
 */
data class FuzzyScore(val score: Int, val matches: IntArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FuzzyScore) return false
        return score == other.score && matches.contentEquals(other.matches)
    }

    override fun hashCode(): Int {
        var result = score
        result = 31 * result + matches.contentHashCode()
        return result
    }
}

/**
 * 模糊搜索评分器。
 * 
 * ## 功能描述
 * 该类用于对补全建议（CompletionItems）进行智能排序。
 * 它不仅检查字符是否存在，还会根据字符位置（如首字母、驼峰、后缀等）计算权重。
 * 
 * ## 工作流程线路图
 * [输入字符] -> [计算字符距离] -> [检查边界/驼峰] -> [生成权重分值] -> [记录匹配索引]
 * 
 * @author android_zero
 */
object FuzzyScorer {

    fun score(pattern: String, word: String): FuzzyScore? {
        if (pattern.isEmpty()) return FuzzyScore(0, intArrayOf())
        if (pattern.length > word.length) return null

        val patternLower = pattern.lowercase(Locale.ROOT)
        val wordLower = word.lowercase(Locale.ROOT)
        
        val matches = mutableListOf<Int>()
        var patternIdx = 0
        var wordIdx = 0
        var totalScore = 0

        while (patternIdx < pattern.length && wordIdx < word.length) {
            if (patternLower[patternIdx] == wordLower[wordIdx]) {
                // 基础分
                var currentScore = 1
                
                // 额外加分：首字母匹配
                if (wordIdx == 0) currentScore += 10
                
                // 额外加分：驼峰或边界匹配
                if (wordIdx > 0 && (word[wordIdx].isUpperCase() || !word[wordIdx - 1].isLetterOrDigit())) {
                    currentScore += 8
                }
                
                // 额外加分：精确大小写匹配
                if (pattern[patternIdx] == word[wordIdx]) {
                    currentScore += 2
                }

                totalScore += currentScore
                matches.add(wordIdx)
                patternIdx++
            }
            wordIdx++
        }

        return if (patternIdx == pattern.length) {
            FuzzyScore(totalScore, matches.toIntArray())
        } else {
            null
        }
    }
}