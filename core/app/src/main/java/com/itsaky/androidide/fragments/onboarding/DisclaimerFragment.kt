/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */

package com.itsaky.androidide.fragments.onboarding

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.github.appintro.SlidePolicy
import com.itsaky.androidide.databinding.FragmentOnboardingDisclaimerBinding
import com.itsaky.androidide.fragments.FragmentWithBinding
import io.noties.markwon.Markwon
import org.slf4j.LoggerFactory
import java.io.InputStreamReader

/**
 * 引导页：免责与隐私协议声明
 * 
 * 使用 [Markwon] 从 Assets 渲染 Markdown，
 * 采用 [SlidePolicy] 拦截用户未同意协议时的翻页动作。
 * 
 * @author android_zero
 */
class DisclaimerFragment : FragmentWithBinding<FragmentOnboardingDisclaimerBinding>(FragmentOnboardingDisclaimerBinding::inflate), SlidePolicy {

    private var isAgreed = false

    companion object {
        private val logger = LoggerFactory.getLogger(DisclaimerFragment::class.java)

        @JvmStatic
        fun newInstance(context: Context): DisclaimerFragment {
            return DisclaimerFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化 Markwon 渲染器
        val markwon = Markwon.create(requireContext())
        
        // 从 assets/docs 读取并渲染 md
        val markdownContent = try {
            requireContext().assets.open("docs/Disclaimer-Agreement.md").use { inputStream ->
                InputStreamReader(inputStream).readText()
            }
        } catch (e: Exception) {
            logger.error("Failed to load Disclaimer-Agreement.md", e)
            "Failed to load Disclaimer & Privacy Policy. Please ensure the file exists at assets/docs/Disclaimer-Agreement.md."
        }

        markwon.setMarkdown(binding.markdownTextView, markdownContent)

        // 监听同意勾选状态
        binding.agreeCheckbox.setOnCheckedChangeListener { _, isChecked ->
            isAgreed = isChecked
        }
    }

    /**
     * 判断是否满足进入下一页条件
     */
    override val isPolicyRespected: Boolean
        get() = isAgreed

    /**
     * 若未授权尝试进入下一页时，底部弹出 Toast 拦截动作
     */
    override fun onUserIllegallyRequestedNextPage() {
        Toast.makeText(requireContext(), "Please read and agree to the Disclaimer and Privacy Agreement first.", Toast.LENGTH_SHORT).show()
    }
}