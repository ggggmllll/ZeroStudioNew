package com.itsaky.androidide.lsp.util

import com.itsaky.androidide.projects.FileManager
import com.itsaky.androidide.lsp.util.Logger
import java.io.File
import java.nio.file.Paths

/**
 * 文档同步管理器。
 * 
 * ## 功能描述
 * 桥接 AndroidIDE 的 [ActiveDocument] 系统。
 * 当 LSP 需要获取文档版本或最新内容时，优先从 FileManager 获取内存中的 ActiveDocument。
 * 
 * @author android_zero
 */
object DocumentSyncManager {

    private val LOG = Logger.instance("DocSync")

    /**
     * 获取指定文件的当前版本号。
     * 适配自 Xed 的 getVersion 逻辑，但直接读取 ActiveDocument。
     */
    fun getDocumentVersion(file: File): Int {
        val path = file.toPath()
        val activeDoc = FileManager.getActiveDocument(path)
        return activeDoc?.version ?: 0
    }

    /**
     * 获取文档的最新内容。
     * 如果文件已打开，返回内存内容；否则读取磁盘。
     */
    fun getDocumentContent(file: File): String {
        return FileManager.getDocumentContents(file.toPath())
    }
}