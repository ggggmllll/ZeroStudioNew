/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software; you can redistribute it and/or modify
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
 *
 * @author android_zero
 *
 * This file has been modified to adapt to the new API of the sora-editor library.
 * The following methods have been removed as they are no longer public/protected in the superclass:
 * - drawWhitespaces
 * - drawRegionText
 * - drawRegionTextDirectional
 * - drawFunctionCharacter
 * - drawText
 * - drawTextRunDirect
 * - patchTextRegions
 * - measureText
 *
 * The following methods' signatures have been updated to match the superclass:
 * - drawMiniGraph (String? -> Drawable?)
 * - drawRowRegionBackground (updated parameters to match superclass)
 */
package com.itsaky.androidide.editor.ui

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.RenderNode
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.collection.MutableIntList
import com.itsaky.androidide.editor.BuildConfig
import io.github.rosemoe.sora.graphics.TextRow
import io.github.rosemoe.sora.lang.styling.CodeBlock
import io.github.rosemoe.sora.lang.styling.Spans
import io.github.rosemoe.sora.util.LongArrayList
import io.github.rosemoe.sora.util.MutableInt
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.EditorRenderer

/**
 * An implementation of [EditorRenderer] which traces the whole drawing process for [IDEEditor].
 *
 * **Purpose/Functionality**: This class extends the standard [EditorRenderer] to wrap critical
 * rendering methods with `androidx.tracing.trace`. This allows developers to visualize the
 * performance of the editor's rendering pipeline using system tracing tools (Systrace, Perfetto).
 *
 * **Workflow**: Every method overrides its counterpart in the superclass. Instead of executing
 * directly, it passes a section name (e.g., "drawView") to the [trace] inline function. This
 * creates a timed section in the system trace, executing the superclass logic within that section.
 *
 * **Usage**: This renderer is automatically initialized and attached to the [CodeEditor] when
 * debugging is enabled or explicitly requested. It is not meant to be called directly by
 * application code for drawing purposes, but rather acts as the drawing engine for the editor
 * widget.
 *
 * @author Akash Yadav
 * @author android_zero
 */
class TracingEditorRenderer(private val enabled: Boolean = BuildConfig.DEBUG, editor: CodeEditor) :
    EditorRenderer(editor) {

  /**
   * **Purpose**: The main entry point for drawing the editor content onto the canvas.
   *
   * **Workflow**: Wraps [EditorRenderer.draw] in a "draw" trace section.
   *
   * @param canvas The canvas on which the editor will be drawn.
   */
  override fun draw(canvas: Canvas) = trace("draw") { super.draw(canvas) }

  /**
   * **Purpose**: Updates the display list for a specific line using hardware acceleration
   * (RenderNode).
   *
   * **Functionality**: On Android Q and above, this helps in caching the rendering commands for a
   * line to improve scrolling performance by avoiding re-recording drawing commands every frame.
   *
   * **Workflow**: Wraps [EditorRenderer.updateLineDisplayList] in a trace section.
   *
   * @param renderNode The RenderNode that holds the display list for the line.
   * @param line The line index in the document.
   * @param spans The styling spans associated with this line.
   */
  @RequiresApi(Build.VERSION_CODES.Q)
  override fun updateLineDisplayList(renderNode: RenderNode?, line: Int, spans: Spans.Reader?) =
      trace("updateLineDisplayList") { super.updateLineDisplayList(renderNode, line, spans) }

  /**
   * **Purpose**: Draws the text content of a single line.
   *
   * **Functionality**: This handles the actual character layout and drawing for a line of code,
   * including syntax highlighting application via [spans].
   *
   * **Workflow**: Wraps [EditorRenderer.drawSingleTextLine] in a trace section.
   *
   * @param canvas The canvas to draw text on.
   * @param line The line index.
   * @param offsetX The horizontal offset for drawing.
   * @param offsetY The vertical offset for drawing.
   * @param spans The reader for accessing style spans.
   * @param visibleOnly If true, optimizes to draw only visible parts.
   * @return The width of the drawn line.
   */
  override fun drawSingleTextLine(
      canvas: Canvas?,
      line: Int,
      offsetX: Float,
      offsetY: Float,
      spans: Spans.Reader?,
      visibleOnly: Boolean,
  ) =
      trace("drawSingleTextLine") {
        super.drawSingleTextLine(canvas, line, offsetX, offsetY, spans, visibleOnly)
      }

  /**
   * **Purpose**: Coordinates the drawing of the entire editor view visible area.
   *
   * **Functionality**: This method orchestrates the calls to draw background, gutter, lines,
   * scrollbars, and overlays. It is the core of the rendering loop.
   *
   * **Workflow**: Wraps [EditorRenderer.drawView] in a trace section.
   *
   * @param canvas The canvas to draw on.
   */
  override fun drawView(canvas: Canvas?) = trace("drawView") { super.drawView(canvas) }

  /**
   * **Purpose**: Draws the user-defined background for the gutter area (line number area).
   *
   * **Usage**: Used when plugins or themes define specific background colors for the gutter of
   * certain lines (e.g., git diff indicators).
   *
   * @param canvas The canvas to draw on.
   * @param right The right boundary of the gutter area.
   */
  override fun drawUserGutterBackground(canvas: Canvas?, right: Int) =
      trace("drawUserGutterBackground") { super.drawUserGutterBackground(canvas, right) }

  /**
   * **Purpose**: Draws "Sticky" line numbers.
   *
   * **Functionality**: When "Sticky Scroll" is enabled, this draws the line numbers for the code
   * blocks that are currently stuck to the top of the viewport.
   *
   * @param candidates List of code blocks currently sticking.
   * @param offset Horizontal offset.
   * @param lineNumberWidth The reserved width for line numbers.
   * @param lineNumberColor The paint color for the text.
   */
  override fun drawStuckLineNumbers(
      canvas: Canvas?,
      candidates: MutableList<CodeBlock>?,
      offset: Float,
      lineNumberWidth: Float,
      lineNumberColor: Int,
  ) =
      trace("drawStuckLineNumbers") {
        super.drawStuckLineNumbers(canvas, candidates, offset, lineNumberWidth, lineNumberColor)
      }

  /**
   * **Purpose**: Draws the content of "Sticky" lines.
   *
   * **Functionality**: Renders the text content of the scope headers (e.g., function definitions,
   * classes) that stick to the top of the screen when scrolling within their scope.
   *
   * @param canvas The canvas to draw on.
   * @param candidates List of sticky code blocks.
   * @param offset Horizontal offset.
   */
  override fun drawStuckLines(canvas: Canvas?, candidates: MutableList<CodeBlock>?, offset: Float) =
      trace("drawStuckLines") { super.drawStuckLines(canvas, candidates, offset) }

  /**
   * **Purpose**: Draws the visual marker for the hard wrap column.
   *
   * **Functionality**: Draws a vertical line indicating where the code should ideally wrap (e.g.,
   * at column 80 or 100).
   *
   * @param canvas The canvas to draw on.
   * @param offset The x-coordinate offset for calculation.
   */
  override fun drawHardwrapMarker(canvas: Canvas?, offset: Float) =
      trace("drawHardwrapMarker") { super.drawHardwrapMarker(canvas, offset) }

  /**
   * **Purpose**: Draws icons in the side gutter (e.g., breakpoints, bookmarks).
   *
   * @param canvas The canvas to draw on.
   * @param offset The horizontal offset where icons should be placed.
   */
  override fun drawSideIcons(canvas: Canvas?, offset: Float) =
      trace("drawSideIcons") { super.drawSideIcons(canvas, offset) }

  /**
   * **Purpose**: Draws a tip/hint during formatting operations.
   *
   * @param canvas The canvas to draw on.
   */
  override fun drawFormatTip(canvas: Canvas?) =
      trace("drawFormatTip") { super.drawFormatTip(canvas) }

  /**
   * **Purpose**: Utility to fill a specific rectangle with a color.
   *
   * @param canvas The target canvas.
   * @param color The color integer.
   * @param rect The definition of the rectangle to fill (RectF).
   */
  override fun drawColor(canvas: Canvas?, color: Int, rect: RectF?) =
      trace("drawColor") { super.drawColor(canvas, color, rect) }

  /**
   * **Purpose**: Utility to fill a specific rectangle with a color (Integer Rect variant).
   *
   * @param canvas The target canvas.
   * @param color The color integer.
   * @param rect The definition of the rectangle to fill (Rect).
   */
  override fun drawColor(canvas: Canvas?, color: Int, rect: Rect?) =
      trace("drawColor") { super.drawColor(canvas, color, rect) }

  /**
   * **Purpose**: Utility to draw a rounded rectangle with a specific color.
   *
   * @param canvas The target canvas.
   * @param color The color integer.
   * @param rect The definition of the rectangle area.
   */
  override fun drawColorRound(canvas: Canvas?, color: Int, rect: RectF?) =
      trace("drawColorRound") { super.drawColorRound(canvas, color, rect) }

  /**
   * **Purpose**: Draws the background color for a specific row in the editor.
   *
   * @param canvas The canvas to draw on.
   * @param color The background color.
   * @param row The visual row index.
   */
  override fun drawRowBackground(canvas: Canvas?, color: Int, row: Int) =
      trace("drawRowBackground") { super.drawRowBackground(canvas, color, row) }

  /**
   * **Purpose**: Draws the background for a row with an explicit right boundary.
   *
   * @param canvas The canvas to draw on.
   * @param color The background color.
   * @param row The visual row index.
   * @param right The right coordinate limit for the background.
   */
  override fun drawRowBackground(canvas: Canvas?, color: Int, row: Int, right: Int) =
      trace("drawRowBackground") { super.drawRowBackground(canvas, color, row, right) }

  /**
   * **Purpose**: Draws the line number text for a specific line.
   *
   * @param canvas The canvas.
   * @param line The logical line index (0-based).
   * @param row The visual row index (handles word wraps).
   * @param offsetX X offset for the text.
   * @param width Width allocated for line numbers.
   * @param color Text color.
   */
  override fun drawLineNumber(
      canvas: Canvas?,
      line: Int,
      row: Int,
      offsetX: Float,
      width: Float,
      color: Int,
  ) = trace("drawLineNumber") { super.drawLineNumber(canvas, line, row, offsetX, width, color) }

  /**
   * **Purpose**: Draws the background strip for the line number area (gutter).
   *
   * @param canvas The canvas.
   * @param offsetX Starting X coordinate.
   * @param width Width of the gutter.
   * @param color Background color.
   */
  override fun drawLineNumberBackground(canvas: Canvas?, offsetX: Float, width: Float, color: Int) =
      trace("drawLineNumberBackground") {
        super.drawLineNumberBackground(canvas, offsetX, width, color)
      }

  /**
   * **Purpose**: Draws the divider line between the gutter and the code area.
   *
   * @param canvas The canvas.
   * @param offsetX The X coordinate where the divider is drawn.
   * @param color The color of the divider line.
   */
  override fun drawDivider(canvas: Canvas?, offsetX: Float, color: Int) =
      trace("drawDivider") { super.drawDivider(canvas, offsetX, color) }

  /**
   * **Purpose**: Main loop for drawing visible rows of text.
   *
   * **Functionality**: Iterates through the layout to render text backgrounds, text content, and
   * prepares deferred drawing tasks for cursors and line numbers.
   *
   * @param canvas The canvas.
   * @param offset Current scroll offset adjustment.
   * @param postDrawLineNumbers List to collect line numbers to draw later (z-order handling).
   * @param postDrawCursor List to collect cursors to draw later.
   * @param postDrawCurrentLines List to collect current line highlights.
   * @param requiredFirstLn Output parameter to store the first visible line index.
   */
  override fun drawRows(
      canvas: Canvas?,
      offset: Float,
      postDrawLineNumbers: LongArrayList?,
      postDrawCursor: MutableList<DrawCursorTask>?,
      postDrawCurrentLines: MutableIntList?,
      requiredFirstLn: MutableInt?,
  ) =
      trace("drawRows") {
        super.drawRows(
            canvas,
            offset,
            postDrawLineNumbers,
            postDrawCursor,
            postDrawCurrentLines,
            requiredFirstLn,
        )
      }

  /**
   * **Purpose**: Draws wavy lines or underlines for diagnostics (errors, warnings).
   *
   * @param canvas The canvas.
   * @param offset Horizontal offset for alignment.
   */
  override fun drawDiagnosticIndicators(canvas: Canvas?, offset: Float) =
      trace("drawDiagnosticIndicators") { super.drawDiagnosticIndicators(canvas, offset) }

  /**
   * **Purpose**: Draws small graphical markers (e.g., for non-printable characters like newlines or
   * tabs).
   *
   * @param canvas The canvas.
   * @param offset X coordinate.
   * @param row Row index.
   * @param graph The drawable to render (e.g., return symbol).
   */
  override fun drawMiniGraph(canvas: Canvas?, offset: Float, row: Int, graph: Drawable?) =
      trace("drawMiniGraph") { super.drawMiniGraph(canvas, offset, row, graph) }

  /**
   * **Purpose**: Calculates the top Y coordinate for drawing a row's background. **Note**: May
   * differ from standard row top depending on settings (e.g., if background wraps text only).
   */
  override fun getRowTopForBackground(row: Int) =
      trace("getRowTopForBackground") {
        return@trace super.getRowTopForBackground(row)
      }

  /** **Purpose**: Calculates the bottom Y coordinate for drawing a row's background. */
  override fun getRowBottomForBackground(row: Int) =
      trace("getRowBottomForBackground") {
        return@trace super.getRowBottomForBackground(row)
      }

  /**
   * **Purpose**: Draws a specific background region within a row (e.g., selection, search
   * highlight).
   *
   * **Functionality**: Renders a colored rectangle (possibly with a border) behind a range of text.
   * This method signature handles the updated API requiring a [TextRow] object and border colors.
   *
   * @param canvas The canvas to draw on.
   * @param row The visual row index containing the region.
   * @param tr The [TextRow] helper object used to measure text advances for accurate background
   *   placement.
   * @param highlightStart The starting character index of the highlight.
   * @param highlightEnd The ending character index of the highlight.
   * @param rowStart The character index where this row starts.
   * @param rowEnd The character index where this row ends.
   * @param color The fill color of the background region.
   * @param borderColor The border color of the region (0 if no border).
   */
  override fun drawRowRegionBackground(
      canvas: Canvas,
      row: Int,
      tr: TextRow?,
      highlightStart: Int,
      highlightEnd: Int,
      rowStart: Int,
      rowEnd: Int,
      color: Int,
      borderColor: Int,
  ) =
      trace("drawRowRegionBackground") {
        super.drawRowRegionBackground(
            canvas,
            row,
            tr,
            highlightStart,
            highlightEnd,
            rowStart,
            rowEnd,
            color,
            borderColor,
        )
      }

  /** **Purpose**: Low-level utility to draw a rectangle (possibly rounded) for row backgrounds. */
  override fun drawRowBackgroundRect(canvas: Canvas?, rect: RectF?) =
      trace("drawRowBackgroundRect") { super.drawRowBackgroundRect(canvas, rect) }

  /** **Purpose**: Draws the edge glow effect when scrolling overshoots the content boundaries. */
  override fun drawEdgeEffect(canvas: Canvas?) =
      trace("drawEdgeEffect") { super.drawEdgeEffect(canvas) }

  /**
   * **Purpose**: Draws vertical indentation guidelines (block lines).
   *
   * @param canvas The canvas.
   * @param offsetX The text offset to align lines correctly.
   */
  override fun drawBlockLines(canvas: Canvas?, offsetX: Float) =
      trace("drawBlockLines") { super.drawBlockLines(canvas, offsetX) }

  /** **Purpose**: Draws the block line indicator in the side gutter for the current scope. */
  override fun drawSideBlockLine(canvas: Canvas?) =
      trace("drawSideBlockLine") { super.drawSideBlockLine(canvas) }

  /** **Purpose**: Draws both vertical and horizontal scrollbars. */
  override fun drawScrollBars(canvas: Canvas?) =
      trace("drawScrollBars") { super.drawScrollBars(canvas) }

  /** **Purpose**: Draws the background track for the vertical scrollbar. */
  override fun drawScrollBarTrackVertical(canvas: Canvas?) =
      trace("drawScrollBarTrackVertical") { super.drawScrollBarTrackVertical(canvas) }

  /** **Purpose**: Draws the moving thumb of the vertical scrollbar. */
  override fun drawScrollBarVertical(canvas: Canvas?) =
      trace("drawScrollBarVertical") { super.drawScrollBarVertical(canvas) }

  /**
   * **Purpose**: Draws the bubble/panel that appears when dragging the scrollbar (shows current
   * line info).
   *
   * @param canvas The canvas.
   * @param topY The top Y position of the scrollbar thumb.
   * @param length The height of the scrollbar thumb.
   */
  override fun drawLineInfoPanel(canvas: Canvas?, topY: Float, length: Float) =
      trace("drawLineInfoPanel") { super.drawLineInfoPanel(canvas, topY, length) }

  /** **Purpose**: Draws the background track for the horizontal scrollbar. */
  override fun drawScrollBarTrackHorizontal(canvas: Canvas?) =
      trace("drawScrollBarTrackHorizontal") { super.drawScrollBarTrackHorizontal(canvas) }

  /** **Purpose**: Highlights regions related to code snippets (e.g., tab stops, placeholders). */
  override fun patchSnippetRegions(canvas: Canvas?, textOffset: Float) =
      trace("patchSnippetRegions") { super.patchSnippetRegions(canvas, textOffset) }

  /** **Purpose**: Highlights matching delimiters (brackets, braces) when the cursor is near one. */
  override fun patchHighlightedDelimiters(canvas: Canvas?, textOffset: Float) =
      trace("patchHighlightedDelimiters") { super.patchHighlightedDelimiters(canvas, textOffset) }

  /**
   * **Purpose**: Utility to patch/overlay a colored region on the text (used by snippet and
   * delimiter highlighting).
   *
   * @param start Start index.
   * @param end End index.
   * @param color Text foreground color override.
   * @param backgroundColor Background color.
   * @param underlineColor Underline color.
   */
  override fun patchTextRegionWithColor(
      canvas: Canvas?,
      textOffset: Float,
      start: Int,
      end: Int,
      color: Int,
      backgroundColor: Int,
      underlineColor: Int,
  ) =
      trace("patchTextRegionWithColor") {
        super.patchTextRegionWithColor(
            canvas,
            textOffset,
            start,
            end,
            color,
            backgroundColor,
            underlineColor,
        )
      }

  /** **Purpose**: Draws the cursor selection animation (e.g., when the cursor moves smoothly). */
  override fun drawSelectionOnAnimation(canvas: Canvas?) =
      trace("drawSelectionOnAnimation") { super.drawSelectionOnAnimation(canvas) }

  /** **Purpose**: Draws the moving thumb of the horizontal scrollbar. */
  override fun drawScrollBarHorizontal(canvas: Canvas?) =
      trace("drawScrollBarHorizontal") { super.drawScrollBarHorizontal(canvas) }

  /**
   * **Purpose**: Builds or updates the measurement cache for lines to optimize text layout.
   *
   * @param startLine Start line index.
   * @param endLine End line index.
   * @param timestamp Current frame timestamp to validate cache freshness.
   * @param useCachedContent Whether to use cached content text if available.
   */
  override fun buildMeasureCacheForLines(
      startLine: Int,
      endLine: Int,
      timestamp: Long,
      useCachedContent: Boolean,
  ) =
      trace("buildMeasureCacheForLines") {
        super.buildMeasureCacheForLines(startLine, endLine, timestamp, useCachedContent)
      }

  /** **Purpose**: Overload for building measure cache with default settings. */
  override fun buildMeasureCacheForLines(startLine: Int, endLine: Int) =
      trace("buildMeasureCacheForLines") { super.buildMeasureCacheForLines(startLine, endLine) }

  /**
   * **Purpose**: Internal utility to execute a block of code within a system trace section.
   *
   * **Usage**:
   * ```
   * trace("MySection") {
   *     // heavy work
   * }
   * ```
   *
   * If tracing is disabled, the action is executed directly without tracing overhead.
   *
   * @param section The name of the trace section (visible in profiling tools).
   * @param action The code block to execute.
   * @return The result of the action block.
   */
  private inline fun <T : Any?> trace(section: String, crossinline action: () -> T): T =
      if (enabled) {
        androidx.tracing.trace(section, action)
      } else {
        action()
      }
}
