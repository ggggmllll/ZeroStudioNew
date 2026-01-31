package com.itsaky.androidide.lsp

import io.github.rosemoe.sora.lsp.editor.LspProject

/**
 * 扩展方法：将 LspManager 中注册的所有服务器定义加载到 sora-editor-lsp 的 Project 中。
 * 
 * 用法: lspProject.loadAllRegisteredServers()
 */
fun LspProject.loadAllRegisteredServers() {
    val definitions = LspManager.getAllDefinitions()
    if (definitions.isNotEmpty()) {
        this.addServerDefinitions(definitions)
    }
}