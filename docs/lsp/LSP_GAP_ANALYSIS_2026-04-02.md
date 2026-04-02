# ZeroStudio LSP 能力差距分析（对标 LSP 3.18 / LSP4J）

日期：2026-04-02

参考：
- Microsoft LSP 规范（3.18）：https://microsoft.github.io/language-server-protocol/specifications/lsp/3.18/specification/
- LSP4J 项目： https://github.com/eclipse-lsp4j/lsp4j

---

## 1. 当前 API 能力覆盖（基于 `core/lsp-api` + `core/lsp-models`）

### 已覆盖（核心）
- 文档诊断：`DiagnosticResult`、`DiagnosticItem`
- 基础补全：`CompletionParams`、`CompletionResult`
- 定义/引用：`Definition*`、`Reference*`
- 签名帮助：`SignatureHelp*`
- 悬浮信息：`MarkupContent`
- 代码格式化：`FormatCodeParams`、`CodeFormatResult`
- 选择扩展：`ExpandSelectionParams`
- 基础 CodeAction 数据模型（客户端有执行入口）

### 部分覆盖
- CodeAction：当前主要是“客户端执行入口 + 数据结构”，缺少完整的请求/解析/resolve 生命周期能力。

---

## 2. 与标准 LSP 的主要差距

> 注：以下差距是从协议标准能力集合对照当前接口后得到，优先按对 IDE 体验影响排序。

### A 级（强优先级）
1. **Document Symbol / Workspace Symbol 全链路缺失**
   - 影响：大纲、全局符号检索、快速导航能力受限。
2. **Rename（prepareRename + rename）缺失**
   - 影响：安全重命名与跨文件改名体验不足。
3. **CodeAction 完整闭环不足（含 quick fix / refactor / source action）**
   - 影响：快速修复、重构菜单能力不完整。
4. **Semantic Tokens 缺失（含 full/range/delta）**
   - 影响：语义高亮无法对标 VSCode/IntelliJ 级体验。
5. **Document Highlight / Selection Range / Folding Range 缺失**
   - 影响：局部语义交互与结构化阅读能力不足。

### B 级（中优先级）
6. **Document Link / Document Color / ColorPresentation 缺失**
7. **Inlay Hint / Inline Value 缺失**
8. **Call Hierarchy / Type Hierarchy / Implementation / Declaration 缺失**
9. **Code Lens（及 resolve）缺失**
10. **Moniker / Linked Editing Range / Type Definition 缺失**

### C 级（平台协作能力）
11. **WorkspaceEdit 细粒度能力不完整（documentChanges / resource ops）**
12. **WorkDoneProgress / PartialResultToken 体系缺失**
13. **Notebook / Diagnostic Pull Model（新规范方向）缺失**

---

## 3. `core/lsp-api` 升级建议（接口层）

建议新增（按阶段）：

### Phase 1（P0）
- `documentSymbols(file)`
- `workspaceSymbols(query)`
- `rename(params)` + `prepareRename(params)`
- `codeAction(params)` + `resolveCodeAction(item)`
- `foldingRanges(file)`
- `documentHighlight(params)`

### Phase 2（P1）
- `semanticTokensFull/Range/Delta`
- `inlayHints(range)`
- `documentLinks(file)` + `resolveDocumentLink`
- `selectionRanges(positions)`

### Phase 3（P2）
- `callHierarchy*`, `typeHierarchy*`
- `codeLens*`, `inlineValue*`
- `moniker`, `linkedEditingRange`

---

## 4. `core/lsp-models` 升级建议（数据模型层）

建议新增模型：
- Symbol：`DocumentSymbol`, `SymbolInformation`, `WorkspaceSymbol`
- Rename：`PrepareRenameResult`, `RenameParams`, `WorkspaceEdit`（增强）
- Semantic Tokens：`SemanticTokensLegend`, `SemanticTokens`, `SemanticTokensDelta`
- Inlay/Inline：`InlayHint`, `InlineValue`
- Hierarchy：`CallHierarchyItem`, `TypeHierarchyItem`
- Code Lens / Document Link / Folding / SelectionRange
- 进度/结果：`WorkDoneProgress*`, `PartialResult*`

---

## 5. 与 LSP4J 的对齐策略

1. 以 **LSP4J 类型命名与字段语义** 作为主参考（减少适配层负担）。
2. 在 `core/lsp-models` 建立“协议对齐层”，减少 app/server 各处自定义结构分叉。
3. 对 `editor-lsp` 与 `core/lsp-api` 增加统一 capability 协商对象，避免 server 各自硬编码。

---

## 6. 建议落地路线图（12 周）

- 第 1-2 周：补齐 Symbol/Rename/CodeAction/Folding/Highlight 模型与 API。
- 第 3-5 周：Java/Kotlin/TOML/Smali/Groovy 先落地 P0 能力。
- 第 6-8 周：语义高亮（Semantic Tokens）+ Inlay Hint。
- 第 9-10 周：Call/Type Hierarchy + CodeLens。
- 第 11-12 周：稳定性、性能（增量索引/缓存/并发），并完善测试矩阵。

---

## 7. 当前结论

ZeroStudio 已具备基础 LSP 框架，但与“全功能协议支持”仍有系统性差距。
优先补齐 **Symbol / Rename / CodeAction / SemanticTokens / Folding / Highlight** 可显著提升 IDE 体验并与主流编辑器能力靠齐。
