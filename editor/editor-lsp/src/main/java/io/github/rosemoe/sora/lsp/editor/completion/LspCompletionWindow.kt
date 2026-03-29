package io.github.rosemoe.sora.lsp.editor.completion

import android.view.KeyEvent
import android.view.View.MeasureSpec
import io.github.rosemoe.sora.event.*
import io.github.rosemoe.sora.lang.completion.snippet.parser.CodeSnippetParser
import io.github.rosemoe.sora.lsp.editor.LspEditor
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.base.EditorPopupWindow
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.InsertTextFormat
import org.eclipse.lsp4j.services.TextDocumentService

class LspCompletionWindow(private val lspEditor: LspEditor, private val editor: CodeEditor) :
    EditorPopupWindow(
        editor,
        FEATURE_HIDE_WHEN_FAST_SCROLL or FEATURE_DISMISS_WHEN_OBSCURING_CURSOR,
    ) {

  private val eventManager = editor.createSubEventManager()
  private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
  private var resolveJob: Job? = null

  private var layoutImpl: CompletionLayout = DefaultLspCompletionLayout()
  private lateinit var rootView: android.view.View

  private var currentList: CompletionList? = null
  private var filteredItems: List<CompletionItem> = emptyList()
  private var currentSelectedIndex = -1
  private var triggerPosition: CharPosition? = null

  private val resolvedCache = ConcurrentHashMap<CompletionItem, CompletionItem>()

  init {
    layoutImpl.attach(this)
    rootView = layoutImpl.createView(android.view.LayoutInflater.from(editor.context))
    setContentView(rootView)
    subscribeEvents()
  }

  private fun subscribeEvents() {
    eventManager.subscribeAlways(EditorKeyEvent::class.java) { event ->
      if (isShowing && event.eventType == EditorKeyEvent.Type.DOWN) {
        when (event.keyCode) {
          KeyEvent.KEYCODE_DPAD_UP -> {
            moveSelection(-1)
            event.markAsConsumed()
          }
          KeyEvent.KEYCODE_DPAD_DOWN -> {
            moveSelection(1)
            event.markAsConsumed()
          }
          KeyEvent.KEYCODE_TAB,
          KeyEvent.KEYCODE_ENTER -> {
            applySelection(currentSelectedIndex)
            event.markAsConsumed()
          }
          KeyEvent.KEYCODE_ESCAPE -> {
            dismiss()
            event.markAsConsumed()
          }
        }
      }
    }
    eventManager.subscribeEvent(SelectionChangeEvent::class.java) { event, _ ->
      if (isShowing && event.cause == SelectionChangeEvent.CAUSE_TAP) dismiss()
    }
    eventManager.subscribeEvent(ScrollEvent::class.java) { _, _ ->
      if (isShowing) updateWindowLocation()
    }
    eventManager.subscribeEvent(ColorSchemeUpdateEvent::class.java) { _, _ ->
      layoutImpl.applyColorScheme(editor.colorScheme, editor.typefaceText)
    }
    eventManager.subscribeEvent(TextSizeChangeEvent::class.java) { event, _ ->
      layoutImpl.onTextSizeChanged(event.oldTextSize, event.newTextSize)
      if (isShowing) updateWindowLocation()
    }
  }

  fun show(list: CompletionList?, items: List<CompletionItem>, position: CharPosition) {
    if (items.isEmpty()) {
      dismiss()
      return
    }

    this.currentList = list
    this.filteredItems = items
    this.triggerPosition = position
    this.currentSelectedIndex = 0

    layoutImpl.applyColorScheme(editor.colorScheme, editor.typefaceText)
    layoutImpl.setCompletionItems(items)

    updateWindowLocation()
    if (!isShowing) super.show()

    requestDocResolve(0)
  }

  private fun updateWindowLocation() {
    val anchor = triggerPosition ?: editor.cursor.left()
    val rowHeight = editor.rowHeight
    val charY = editor.getCharOffsetY(anchor.line, anchor.column)
    val charX = editor.getCharOffsetX(anchor.line, anchor.column)

    val maxWidth = (editor.width * 0.9).toInt()
    val maxHeight = (editor.height * 0.5).toInt()

    rootView.measure(
        MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST),
    )

    val width = rootView.measuredWidth.coerceAtLeast((editor.dpUnit * 350).toInt())
    val height = rootView.measuredHeight.coerceAtMost(maxHeight)

    val margin = editor.dpUnit * 4
    val spaceBelow = editor.height - (charY + rowHeight + margin)

    val windowY =
        if (spaceBelow < height && charY > height) {
          charY - height - margin
        } else {
          charY + rowHeight + margin
        }

    val windowX = charX.coerceIn(0f, (editor.width - width).toFloat())

    setSize(width, height)
    setLocationAbsolutely(windowX.toInt(), windowY.toInt())
  }

  private fun moveSelection(delta: Int) {
    if (filteredItems.isEmpty()) return
    val newIndex = (currentSelectedIndex + delta).coerceIn(0, filteredItems.size - 1)
    if (newIndex != currentSelectedIndex) {
      currentSelectedIndex = newIndex
      layoutImpl.select(newIndex)
      requestDocResolve(newIndex)
    }
  }

  fun requestDocResolve(index: Int) {
    resolveJob?.cancel()
    val item = filteredItems.getOrNull(index) ?: return

    val cached = resolvedCache[item]
    if (cached != null) {
      layoutImpl.showDocumentation(cached)
      return
    }

    val textDocumentService: TextDocumentService = lspEditor.requestManager ?: return

    layoutImpl.setDocumentationLoading(true)
    resolveJob = scope.launch {
      delay(150) // Debounce
      try {
        val resolvedItem =
            withContext(Dispatchers.IO) { textDocumentService.resolveCompletionItem(item).get() }
                ?: item

        resolvedCache[item] = resolvedItem

        if (isActive && currentSelectedIndex == index) {
          layoutImpl.showDocumentation(resolvedItem)
        }
      } catch (e: Exception) {
        // Ignore cancellation
      } finally {
        if (isActive && currentSelectedIndex == index) {
          layoutImpl.setDocumentationLoading(false)
        }
      }
    }
  }

  fun applySelection(index: Int) {
    val item = filteredItems.getOrNull(index) ?: return
    val text = editor.text

    var lspRange: org.eclipse.lsp4j.Range? = null
    var newText: String? = null

    if (item.textEdit != null) {
      if (item.textEdit.isLeft) {
        val edit = item.textEdit.left
        lspRange = edit.range
        newText = edit.newText
      } else {
        val edit = item.textEdit.right
        lspRange = edit.replace
        newText = edit.newText
      }
    }

    val insertText = newText ?: item.insertText ?: item.label.toString()

    text.beginBatchEdit()

    var startIndex = editor.cursor.left().index

    if (lspRange != null) {
      val sLine = lspRange.start.line
      val sCol = lspRange.start.character
      val eLine = lspRange.end.line
      val eCol = lspRange.end.character

      startIndex = text.getCharIndex(sLine, sCol)
      val endIndex = text.getCharIndex(eLine, eCol)

      text.delete(startIndex, endIndex)
    }

    if (item.insertTextFormat == InsertTextFormat.Snippet) {
      val codeSnippet = CodeSnippetParser.parse(insertText)
      editor.snippetController.startSnippet(startIndex, codeSnippet, "")
    } else {
      val pos = text.indexer.getCharPosition(startIndex)
      text.insert(pos.line, pos.column, insertText)
    }

    item.additionalTextEdits?.let { edits ->
      val sortedEdits =
          edits.sortedWith(compareBy({ -it.range.start.line }, { -it.range.start.character }))
      for (edit in sortedEdits) {
        val start = text.getCharIndex(edit.range.start.line, edit.range.start.character)
        val end = text.getCharIndex(edit.range.end.line, edit.range.end.character)
        text.replace(start, end, edit.newText)
      }
    }

    text.endBatchEdit()
    dismiss()
  }

  fun launchRender(block: suspend CoroutineScope.() -> Unit): Job {
    return scope.launch(block = block)
  }

  fun setEnabled(enabled: Boolean) {
    eventManager.isEnabled = enabled
    if (!enabled) {
      dismiss()
    }
  }

  override fun dismiss() {
    resolveJob?.cancel()
    super.dismiss()
  }
}
