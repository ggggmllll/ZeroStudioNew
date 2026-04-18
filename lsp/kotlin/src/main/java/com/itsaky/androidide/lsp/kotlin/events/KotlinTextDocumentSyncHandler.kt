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

package com.itsaky.androidide.lsp.kotlin.events

import com.itsaky.androidide.eventbus.events.editor.ChangeType
import com.itsaky.androidide.eventbus.events.editor.DocumentChangeEvent
import com.itsaky.androidide.eventbus.events.editor.DocumentCloseEvent
import com.itsaky.androidide.eventbus.events.editor.DocumentOpenEvent
import com.itsaky.androidide.eventbus.events.editor.DocumentSaveEvent
import com.itsaky.androidide.lsp.api.ILanguageServerRegistry
import com.itsaky.androidide.lsp.kotlin.KotlinLanguageServerImpl
import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.utils.Logger
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Kotlin 文档生命周期同步处理器。
 *
 * @author android_zero
 */
object KotlinTextDocumentSyncHandler {

  private val log = Logger.instance("KotlinTextDocumentSyncHandler")
  private val openedDocs = ConcurrentHashMap<Path, DocumentSnapshot>()

  private data class DocumentSnapshot(
      val languageId: String = "kotlin",
      var version: Int,
      var text: String,
  )

  /** 在主程序或管理器初始化时调用一次以注册 EventBus */
  fun init() {
    if (!EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().register(this)
      log.info("KotlinTextDocumentSyncHandler registered to EventBus.")
    }
  }

  fun onServerReady() {
    val server = getServer() ?: return
    openedDocs.forEach { (path, snapshot) ->
          runCatching {
            server.didOpen(
                DidOpenTextDocumentParams(
                    file = path,
                    languageId = snapshot.languageId,
                    version = snapshot.version,
                    text = snapshot.text,
                ),
            )
          }
          .onFailure { log.error("Failed to replay didOpen for ${path.fileName}", it) }
    }
  }

  private fun getServer(): KotlinLanguageServerImpl? {
    return ILanguageServerRegistry.getDefault().getServer("kotlin-lsp") as? KotlinLanguageServerImpl
  }

  private fun isKotlinFile(path: String): Boolean {
    return path.endsWith(".kt", ignoreCase = true) || path.endsWith(".kts", ignoreCase = true)
  }

  @Subscribe(threadMode = ThreadMode.ASYNC)
  fun onDocumentOpen(event: DocumentOpenEvent) {
    if (!isKotlinFile(event.openedFile.toString())) return
    openedDocs[event.openedFile] = DocumentSnapshot(version = event.version, text = event.text ?: "")

    val server = getServer() ?: return

    server.didOpen(
        DidOpenTextDocumentParams(
            file = event.openedFile,
            languageId = "kotlin",
            version = event.version,
            text = event.text ?: "",
        )
    )
  }

  @Subscribe(threadMode = ThreadMode.ASYNC)
  fun onDocumentChange(event: DocumentChangeEvent) {
    if (!isKotlinFile(event.changedFile.toString())) return
    
    val snapshot = openedDocs[event.changedFile] ?: return
    snapshot.version = event.version
    snapshot.text = event.newText ?: ""

    val server = getServer() ?: return

    // Server 初始化为 Incremental 模式，我们发送精准 Range
    val changeEvent = TextDocumentContentChangeEvent(
        range = event.changeRange,
        // 在编辑过程中，如果全量重置文本，它的 changeType = NEW_TEXT
        text = if (event.changeType == ChangeType.DELETE) "" else event.changedText.toString(),
        rangeLength = if (event.changeType == ChangeType.NEW_TEXT) null else Math.abs(event.changeDelta)
    )
    
    // 如果是全文本重写 (NEW_TEXT), 必须清空 Range 以告诉 LSP 这个是整个文本替换
    if (event.changeType == ChangeType.NEW_TEXT) {
        changeEvent.range = null
        changeEvent.rangeLength = null
        changeEvent.text = event.newText ?: ""
    }

    server.didChange(
        DidChangeTextDocumentParams(
            file = event.changedFile,
            version = event.version,
            contentChanges = listOf(changeEvent),
        )
    )
  }

  @Subscribe(threadMode = ThreadMode.ASYNC)
  fun onDocumentClose(event: DocumentCloseEvent) {
    if (!isKotlinFile(event.closedFile.toString())) return
    openedDocs.remove(event.closedFile)
    
    getServer()?.didClose(DidCloseTextDocumentParams(file = event.closedFile))
  }

  @Subscribe(threadMode = ThreadMode.ASYNC)
  fun onDocumentSave(event: DocumentSaveEvent) {
    if (!isKotlinFile(event.file.toString())) return
    
    getServer()?.didSave(
        DidSaveTextDocumentParams(
            file = event.file,
            reason = TextDocumentSaveReason.Manual,
            text = null,
        )
    )
  }
}