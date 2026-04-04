# AndroidIDE LSP Protocol/Model Gap Report (vs lsp4j-source)

Reference baseline: `core/lsp-api/lsp4j-source/org.eclipse.lsp4j/src/main/java/org/eclipse/lsp4j`.

## Newly added in this update

To narrow gaps against lsp4j naming/design, the following were added into `core/lsp-models`:

- Protocol enums (`ProtocolEnums.kt`):
  - `DiagnosticTag`, `CompletionItemTag`, `SymbolTag`
  - `MessageType`, `TextDocumentSyncKind`, `CompletionTriggerKind`, `CodeActionTriggerKind`
  - `FileChangeType`, `WorkDoneProgressKind`, `TextDocumentSaveReason`, `SignatureHelpTriggerKind`
  - `InsertTextMode`, `InlineCompletionTriggerKind`, `CodeActionTag`
- Text document lifecycle models (`TextDocumentLifecycle.kt`):
  - `DidOpenTextDocumentParams`, `DidChangeTextDocumentParams`, `DidCloseTextDocumentParams`, `DidSaveTextDocumentParams`
  - `TextDocumentContentChangeEvent`
- Model extensions:
  - `DiagnosticItem.tags: List<DiagnosticTag>`
  - `CompletionItem.tags: List<CompletionItemTag>`
  - `DocumentSymbol/SymbolInformation/WorkspaceSymbol.tags: List<SymbolTag>`

And `ILanguageServer` now includes lifecycle notifications:

- `didOpen(...)`, `didChange(...)`, `didClose(...)`, `didSave(...)`

## Still-missing / partial areas (compared to full lsp4j protocol surface)

The project intentionally keeps a leaner protocol than full LSP. The following are still partial or not fully modeled:

1. **Notebook document protocol family**
   - Notebook sync/cell lifecycle capabilities are not fully represented.
2. **Full progress/token protocol variants**
   - Existing progress models cover common begin/report/end, but not the entire token/cancellation matrix from lsp4j.
3. **Resource operation granularity**
   - Workspace edit resource operations exist in simplified form; advanced options/annotations are partial.
4. **Advanced code action metadata**
   - Full trigger/source/diagnostic linkage shape is simplified.
5. **Inline completion and insertion behavior options**
   - Trigger/mode enums added, but end-to-end behavior wiring remains partial.
6. **Message window/logging full parity**
   - Message types are modeled; full lsp4j request/response permutations are not all mirrored.

## Recommendation roadmap

1. Add notebook models/capabilities first (largest parity gap).
2. Expand workspace edit operation options/annotations.
3. Add richer code action context/trigger metadata.
4. Extend editor-side handling for completion/symbol/diagnostic tags and insert text modes.
5. Add compatibility tests that diff supported model sets against `lsp4j-source` snapshots.
