/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  AndroidIDE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.itsaky.androidide.compose.preview.runtime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.reflect.Method
import java.lang.reflect.Modifier as ReflectModifier

/**
 * 核心 Compose 渲染器 (Compose-Preview-Renderer)
 *
 * <p>用途：</p>
 * <ul>
 *   <li><b>动态反射加载</b>: 从传入的类加载器中反射定位 `@Composable` 方法并执行。</li>
 *   <li><b>模式模拟</b>: 利用 <code>LocalInspectionMode</code> 模拟 Android Studio Layoutlib 行为，实现静态预览拦截。</li>
 *   <li><b>沙箱防御</b>: 捕获用户 UI 代码的任何崩溃，并原地呈现错误画面，防止宿主 IDE 闪退。</li>
 * </ul>
 *
 * @author android_zero
 */
class ComposableRenderer(
    private val composeView: ComposeView,
    private val classLoader: ComposeClassLoader
) {

    fun render(dexFile: File, className: String, functionName: String, isInteractive: Boolean = false) {
        val clazz = try {
            classLoader.loadClass(dexFile, className)
        } catch (e: Exception) {
            LOG.error("Failed to load class for Hot-Reload", e)
            showError("Failed to load class: $className\nReason: ${e.message}")
            return
        }

        if (clazz == null) {
            showError("Failed to load class: $className")
            return
        }

        val composableMethod = findComposableMethod(clazz, functionName)
        if (composableMethod == null) {
            showError("Composable function not found: $functionName")
            return
        }

        // 绑定到画布，利用 CompositionLocal 注入交互状态
        composeView.setContent {
            MaterialTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompositionLocalProvider(LocalInspectionMode provides !isInteractive) {
                        RenderComposable(clazz, composableMethod)
                    }
                }
            }
        }

        LOG.debug("Rendered composable successfully: {}#{} (Interactive={})", className, functionName, isInteractive)
    }

    private fun findComposableMethod(clazz: Class<*>, functionName: String): Method? {
        val methods = clazz.declaredMethods

        methods.find { it.name == functionName }?.let {
            it.isAccessible = true
            return it
        }

        val candidates = methods.filter { method ->
            !method.name.contains("\$default") &&
                (method.name.startsWith("$functionName\$") || method.name == "${functionName}\$lambda")
        }

        return candidates.minByOrNull { it.parameterCount }?.also { it.isAccessible = true }
    }

    @Composable
    private fun RenderComposable(clazz: Class<*>, method: Method) {
        val isStatic = ReflectModifier.isStatic(method.modifiers)
        val instance = if (isStatic) {
            null
        } else {
            runCatching { clazz.getDeclaredConstructor().newInstance() }.getOrNull()
        }

        if (!isStatic && instance == null) {
            LOG.error("Failed to create instance for non-static method: {}", method.name)
            ErrorContent("Failed to create instance for ${clazz.simpleName}")
            return
        }

        val composer = currentComposer
        val paramCount = method.parameterCount

        val invokeResult: Result<Any?> = when {
            paramCount == 0 -> runCatching { method.invoke(instance) }
            paramCount == 2 -> runCatching { method.invoke(instance, composer, 0) }
            paramCount > 2 -> runCatching {
                val args = arrayOfNulls<Any>(paramCount)
                args[paramCount - 2] = composer
                args[paramCount - 1] = 0
                method.invoke(instance, *args)
            }
            else -> {
                LOG.error("Unexpected parameter count {} for method: {}", paramCount, method.name)
                ErrorContent("Unexpected parameter count: $paramCount")
                return
            }
        }

        if (invokeResult.isFailure) {
            val e = invokeResult.exceptionOrNull()
            LOG.error("Failed to invoke composable method: {}", method.name, e)
            ErrorContent("Render Exception: ${e?.cause?.message ?: e?.message ?: "Unknown error"}")
        }
    }

    private fun showError(message: String) {
        composeView.setContent {
            MaterialTheme {
                ErrorContent(message)
            }
        }
    }

    @Composable
    private fun ErrorContent(message: String) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF3F3))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Preview Render Error",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFB00020)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ComposableRenderer::class.java)
    }
}