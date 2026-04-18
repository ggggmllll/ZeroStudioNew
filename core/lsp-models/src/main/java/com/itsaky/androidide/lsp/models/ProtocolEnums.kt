package com.itsaky.androidide.lsp.models

/** Additional protocol enums aligned with lsp4j-source naming. */
enum class CompletionItemTag {
  Deprecated,
}

enum class MessageType {
  Error,
  Warning,
  Info,
  Log,
  Debug,
}

enum class TextDocumentSyncKind {
  None,
  Full,
  Incremental,
}

enum class CodeActionTriggerKind {
  Invoked,
  Automatic,
}

enum class FileChangeType {
  Created,
  Changed,
  Deleted,
}

enum class WorkDoneProgressKind {
  Begin,
  Report,
  End,
}

enum class TextDocumentSaveReason {
  Manual,
  AfterDelay,
  FocusOut,
}

enum class SignatureHelpTriggerKind {
  Invoked,
  TriggerCharacter,
  ContentChange,
}

enum class InsertTextMode {
  AsIs,
  AdjustIndentation,
}

enum class InlineCompletionTriggerKind {
  Invoked,
  Automatic,
}

enum class CodeActionTag {
  Lint,
  Refactor,
}
