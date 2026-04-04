package com.itsaky.androidide.lsp.clangd

import com.itsaky.androidide.models.Location
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.models.Range
import java.nio.file.Path
import org.json.JSONArray
import org.json.JSONObject

object ClangdJsonAdapter {

  fun parseCompletion(raw: String): ClangdCompletionResult {
    val root = JSONObject(raw)
    val result = root.optJSONObject("result") ?: return ClangdCompletionResult(emptyList(), false)
    val itemsArray = result.optJSONArray("items") ?: JSONArray()
    val items = mutableListOf<ClangdCompletionItem>()
    for (i in 0 until itemsArray.length()) {
      val item = itemsArray.optJSONObject(i) ?: continue
      val label = item.optString("label")
      if (label.isBlank()) continue
      items +=
          ClangdCompletionItem(
              label = label,
              detail = item.optString("detail"),
              insertText = item.optString("insertText", label),
              documentation = item.optString("documentation"),
              kind = item.optInt("kind", 0),
              deprecated = item.optBoolean("deprecated", false),
          )
    }
    return ClangdCompletionResult(items, result.optBoolean("isIncomplete", false))
  }

  fun parseHover(raw: String): ClangdHoverResult? {
    val result = JSONObject(raw).optJSONObject("result") ?: return null
    val contents = result.opt("contents")?.toString() ?: return null
    val rangeJson = result.optJSONObject("range")
    val start = rangeJson?.optJSONObject("start")
    val end = rangeJson?.optJSONObject("end")
    return ClangdHoverResult(
        content = contents,
        startLine = start?.optInt("line") ?: 0,
        startCharacter = start?.optInt("character") ?: 0,
        endLine = end?.optInt("line") ?: 0,
        endCharacter = end?.optInt("character") ?: 0,
    )
  }

  fun parseLocations(raw: String): List<Location> {
    val result = JSONObject(raw).opt("result") ?: return emptyList()
    val array = when (result) {
      is JSONArray -> result
      is JSONObject -> JSONArray().put(result)
      else -> JSONArray()
    }

    val locations = mutableListOf<Location>()
    for (i in 0 until array.length()) {
      val item = array.optJSONObject(i) ?: continue
      val file = uriToPath(item.optString("uri")) ?: continue
      val rangeJson = item.optJSONObject("range") ?: continue
      val start = rangeJson.optJSONObject("start") ?: continue
      val end = rangeJson.optJSONObject("end") ?: continue
      locations +=
          Location(
              file,
              Range(
                  Position(start.optInt("line"), start.optInt("character")),
                  Position(end.optInt("line"), end.optInt("character")),
              ),
          )
    }
    return locations
  }

  private fun uriToPath(uri: String): Path? = runCatching { Path.of(java.net.URI(uri)) }.getOrNull()
}
