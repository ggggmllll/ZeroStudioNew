package android.zero.studio.lsp.clang

import com.itsaky.androidide.lsp.api.ILanguageClient
import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.api.IServerSettings
import com.itsaky.androidide.lsp.models.CodeFormatResult
import com.itsaky.androidide.lsp.models.CompletionItem
import com.itsaky.androidide.lsp.models.CompletionItemKind
import com.itsaky.androidide.lsp.models.CompletionParams
import com.itsaky.androidide.lsp.models.CompletionResult
import com.itsaky.androidide.lsp.models.DefinitionParams
import com.itsaky.androidide.lsp.models.DefinitionResult
import com.itsaky.androidide.lsp.models.DiagnosticItem
import com.itsaky.androidide.lsp.models.DiagnosticResult
import com.itsaky.androidide.lsp.models.DiagnosticSeverity
import com.itsaky.androidide.lsp.models.ExpandSelectionParams
import com.itsaky.androidide.lsp.models.FormatCodeParams
import com.itsaky.androidide.lsp.models.LSPFailure
import com.itsaky.androidide.lsp.models.MarkupContent
import com.itsaky.androidide.lsp.models.MarkupKind
import com.itsaky.androidide.lsp.models.MatchLevel
import com.itsaky.androidide.lsp.models.ReferenceParams
import com.itsaky.androidide.lsp.models.ReferenceResult
import com.itsaky.androidide.lsp.models.SignatureHelp
import com.itsaky.androidide.lsp.models.SignatureHelpParams
import com.itsaky.androidide.models.Location
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.models.Range
import com.itsaky.androidide.projects.IWorkspace
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject

class ClangLanguageServer(initialSettings: ClangdServerSettings) : ILanguageServer {

    override val serverId: String = SERVER_ID
    override var client: ILanguageClient? = null
        private set

    private var settings: ClangdServerSettings = initialSettings
    private var workspaceRoot: Path? = null

    private val diagnosticsCache = ConcurrentHashMap<Path, List<DiagnosticItem>>()
    private val fileVersions = ConcurrentHashMap<Path, Int>()
    private val resultCache = ClangdResultCache()
    private val requestDispatcher = ClangdRequestDispatcher()
    val healthMonitor = ClangdHealthMonitor()

    private val diagnosticsListener = ClangdNativeBridge.DiagnosticsListener { uri, diagnostics ->
        val path = uriToPath(uri) ?: return@DiagnosticsListener
        val mapped = diagnostics.map { it.toDiagnosticItem() }
        diagnosticsCache[path] = mapped
        client?.publishDiagnostics(DiagnosticResult(path, mapped))
    }

    private val healthListener = ClangdNativeBridge.HealthListener { type, message ->
        healthMonitor.onNativeHealth(type, message)
        if (type == "CLANGD_EXIT" && workspaceRoot != null) {
            initializeNative(workspaceRoot!!)
        }
    }

    companion object {
        const val SERVER_ID = "ide.lsp.clangd"
    }

    init {
        ClangdNativeBridge.addDiagnosticsListener(diagnosticsListener)
        ClangdNativeBridge.addHealthListener(healthListener)
    }

    override fun shutdown() {
        ClangdNativeBridge.removeDiagnosticsListener(diagnosticsListener)
        ClangdNativeBridge.removeHealthListener(healthListener)
        fileVersions.keys.forEach { file ->
            ClangdNativeBridge.nativeDidClose(toFileUri(file))
        }
        ClangdNativeBridge.nativeShutdown()
        healthMonitor.onShutdown()
        diagnosticsCache.clear()
        fileVersions.clear()
        resultCache.clear()
    }

    override fun connectClient(client: ILanguageClient?) {
        this.client = client
    }

    override fun applySettings(settings: IServerSettings?) {
        if (settings is ClangdServerSettings) {
            this.settings = settings
            workspaceRoot?.let { initializeNative(it) }
        }
    }

    override fun setupWorkspace(workspace: IWorkspace) {
        workspaceRoot = workspace.getRootProject().path
        initializeNative(workspaceRoot!!)
    }

    override fun complete(params: CompletionParams?): CompletionResult {
        if (params == null || !settings.completionsEnabled()) return CompletionResult.EMPTY
        syncFile(params.file, params.content?.toString())
        val key = "completion:${params.file}:${params.position.line}:${params.position.column}:${params.prefix.orEmpty()}"
        resultCache.get(key)?.let { return parseCompletionResponse(it, params.prefix.orEmpty()) }

        val requestId = ClangdNativeBridge.nativeRequestCompletion(toFileUri(params.file), params.position.line, params.position.column, null)
        val response = runBlocking {
            requestDispatcher.await(requestId, settings.requestTimeoutMs, params.cancelChecker)
        } ?: return CompletionResult.EMPTY

        resultCache.put(key, response)
        return parseCompletionResponse(response, params.prefix.orEmpty())
    }

    override suspend fun findReferences(params: ReferenceParams): ReferenceResult {
        if (!settings.referencesEnabled()) return ReferenceResult(emptyList())
        syncFile(params.file, null)
        val requestId = ClangdNativeBridge.nativeRequestReferences(
            toFileUri(params.file), params.position.line, params.position.column, params.includeDeclaration
        )
        val response = requestDispatcher.await(requestId, settings.requestTimeoutMs, params.cancelChecker)
            ?: return ReferenceResult(emptyList())
        return ReferenceResult(parseLocations(response))
    }

    override suspend fun findDefinition(params: DefinitionParams): DefinitionResult {
        if (!settings.definitionsEnabled()) return DefinitionResult(emptyList())
        syncFile(params.file, null)
        val requestId = ClangdNativeBridge.nativeRequestDefinition(toFileUri(params.file), params.position.line, params.position.column)
        val response = requestDispatcher.await(requestId, settings.requestTimeoutMs, params.cancelChecker)
            ?: return DefinitionResult(emptyList())
        return DefinitionResult(parseLocations(response))
    }

    override suspend fun expandSelection(params: ExpandSelectionParams): Range = params.selection

    override suspend fun signatureHelp(params: SignatureHelpParams): SignatureHelp = SignatureHelp(emptyList(), -1, -1)

    override suspend fun hover(params: DefinitionParams): MarkupContent {
        syncFile(params.file, null)
        val requestId = ClangdNativeBridge.nativeRequestHover(toFileUri(params.file), params.position.line, params.position.column)
        val response = requestDispatcher.await(requestId, settings.requestTimeoutMs, params.cancelChecker) ?: return MarkupContent()
        return parseHoverResponse(response)
    }

    override suspend fun analyze(file: Path): DiagnosticResult {
        if (!settings.diagnosticsEnabled()) return DiagnosticResult.NO_UPDATE
        syncFile(file, null)
        return DiagnosticResult(file, diagnosticsCache[file] ?: emptyList())
    }

    override fun formatCode(params: FormatCodeParams?): CodeFormatResult = CodeFormatResult(false, mutableListOf())

    override fun handleFailure(failure: LSPFailure?): Boolean = false

    private fun initializeNative(root: Path) {
        healthMonitor.onInitialize()
        val ok = ClangdNativeBridge.nativeInitialize(settings.clangdPath, root.toString(), settings.completionLimit)
        healthMonitor.onInitialized(ok)
    }

    private fun syncFile(file: Path, content: String?) {
        if (!file.exists()) return
        val uri = toFileUri(file)
        val newContent = content ?: runCatching { file.readText() }.getOrElse { "" }
        val currentVersion = fileVersions[file]
        val nextVersion = (currentVersion ?: 0) + 1
        fileVersions[file] = nextVersion

        if (currentVersion == null) {
            ClangdNativeBridge.nativeDidOpen(uri, newContent, languageId(file))
            return
        }

        ClangdNativeBridge.nativeDidChange(uri, newContent, nextVersion)
        resultCache.invalidateByPrefix("completion:${file}:")
    }

    private fun parseCompletionResponse(raw: String, prefix: String): CompletionResult {
        val root = JSONObject(raw)
        val result = root.optJSONObject("result") ?: return CompletionResult.EMPTY
        val itemsArray = result.optJSONArray("items") ?: return CompletionResult.EMPTY
        val completionItems = mutableListOf<CompletionItem>()

        for (i in 0 until itemsArray.length()) {
            val item = itemsArray.optJSONObject(i) ?: continue
            val label = item.optString("label")
            if (label.isBlank()) continue
            val detail = item.optString("detail")
            val insertText = item.optString("insertText", label)
            val kind = mapKind(item.optInt("kind", 0))
            val matchToken = if (settings.shouldMatchAllLowerCase()) prefix.lowercase() else prefix
            val candidate = if (settings.shouldMatchAllLowerCase()) label.lowercase() else label
            val match = CompletionItem.matchLevel(candidate, matchToken, settings.completionFuzzyMatchMinRatio())
            if (prefix.isNotBlank() && match == MatchLevel.NO_MATCH) continue

            completionItems += CompletionItem(
                label,
                detail,
                insertText,
                null,
                item.optString("sortText", label),
                null,
                kind,
                match,
                emptyList(),
                null,
            )
        }

        return CompletionResult(completionItems)
    }

    private fun mapKind(kind: Int): CompletionItemKind = when (kind) {
        3, 10 -> CompletionItemKind.FUNCTION
        6 -> CompletionItemKind.VARIABLE
        7, 8 -> CompletionItemKind.CLASS
        9 -> CompletionItemKind.MODULE
        14 -> CompletionItemKind.KEYWORD
        else -> CompletionItemKind.NONE
    }

    private fun parseHoverResponse(raw: String): MarkupContent {
        val result = JSONObject(raw).optJSONObject("result") ?: return MarkupContent()
        val contents = result.opt("contents") ?: return MarkupContent()

        return when (contents) {
            is JSONObject -> {
                val kind = if (contents.optString("kind") == "markdown") MarkupKind.MARKDOWN else MarkupKind.PLAIN
                MarkupContent(contents.optString("value"), kind)
            }
            is JSONArray -> {
                val joined = buildString {
                    for (i in 0 until contents.length()) {
                        if (i > 0) append('\n')
                        append(contents.opt(i).toString())
                    }
                }
                MarkupContent(joined, MarkupKind.PLAIN)
            }
            else -> MarkupContent(contents.toString(), MarkupKind.PLAIN)
        }
    }

    private fun parseLocations(raw: String): List<Location> {
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
            locations += Location(
                file,
                Range(
                    Position(start.optInt("line"), start.optInt("character")),
                    Position(end.optInt("line"), end.optInt("character")),
                ),
            )
        }
        return locations
    }

    private fun ClangDiagnosticItem.toDiagnosticItem(): DiagnosticItem {
        val mappedSeverity = when (severity) {
            1 -> DiagnosticSeverity.ERROR
            2 -> DiagnosticSeverity.WARNING
            3 -> DiagnosticSeverity.INFO
            else -> DiagnosticSeverity.HINT
        }
        return DiagnosticItem(
            message = message,
            code = code.orEmpty(),
            range = Range(Position(startLine, startCharacter), Position(endLine, endCharacter)),
            source = source.orEmpty(),
            severity = mappedSeverity,
        )
    }

    private fun toFileUri(path: Path): String = path.toUri().toString()

    private fun uriToPath(uri: String): Path? = runCatching { Path.of(java.net.URI(uri)) }.getOrNull()

    private fun languageId(file: Path): String {
        val name = file.fileName.toString().lowercase()
        return when {
            name.endsWith(".c") -> "c"
            name.endsWith(".h") -> "c"
            name.endsWith(".m") -> "objective-c"
            name.endsWith(".mm") -> "objective-cpp"
            name.endsWith(".ixx") || name.endsWith(".cppm") -> "cpp"
            else -> "cpp"
        }
    }
}
