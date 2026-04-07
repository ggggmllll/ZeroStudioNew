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
