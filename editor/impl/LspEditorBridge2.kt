class LspEditorBridge(private val editor: IDEEditor, private val server: ILanguageServer) {
    
    private val codeActionManager = LspCodeActionManager(editor, server)

    /**
     * 当编辑器监听到 SelectionChange 且可能存在修复项时调用
     */
    fun onUserRequestFix(line: Int, column: Int, view: View) {
        codeActionManager.requestQuickFix(line, column, view)
    }
    
}