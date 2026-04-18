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

package com.itsaky.androidide.lsp.models

import com.google.gson.annotations.SerializedName
import com.itsaky.androidide.lsp.CancellableRequestParams
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.progress.ICancelChecker
import java.nio.file.Path

/**
 * 签名帮助响应 (textDocument/signatureHelp)
 * 对应 LSP 3.17 协议数据模型，并完美桥接了 AndroidIDE 的遗留接口习惯。
 *
 * @author android_zero
 */
data class SignatureHelp(
    @SerializedName("signatures") var signatures: MutableList<SignatureInformation> = mutableListOf(),
    @SerializedName("activeSignature") var activeSignature: Int = 0,
    @SerializedName("activeParameter") var activeParameter: Int = 0
)

data class SignatureInformation(
    @SerializedName("label") var label: String = "",
    @SerializedName("documentation") var documentationElement: Either<String, MarkupContent>? = null,
    @SerializedName("parameters") var parameters: MutableList<ParameterInformation> = mutableListOf(),
    @SerializedName("activeParameter") var activeParameterElement: Int? = null
) {
    // 兼容旧版空的构造函数
    constructor() : this("", null, mutableListOf(), null)

    /**
     * 兼容旧版本 IDE 的 Getter/Setter
     * 将 Either<String, MarkupContent> 自动处理为 MarkupContent
     */
    var documentation: MarkupContent
        get() = if (documentationElement?.isRight == true) {
            documentationElement!!.right!!
        } else {
            MarkupContent("plaintext", documentationElement?.left ?: "")
        }
        set(value) { documentationElement = Either.forRight(value) }
}

data class ParameterInformation(
    @SerializedName("label") var labelElement: Either<String, List<Int>> = Either.forLeft(""),
    @SerializedName("documentation") var documentationElement: Either<String, MarkupContent>? = null
) {
    // 兼容旧版空的构造函数
    constructor() : this(Either.forLeft(""), null)

    /**
     * 兼容旧版本 IDE 的 Getter/Setter
     */
    var label: String
        get() = if (labelElement.isLeft) labelElement.left!! else ""
        set(value) { labelElement = Either.forLeft(value) }

    var documentation: MarkupContent
        get() = if (documentationElement?.isRight == true) {
            documentationElement!!.right!!
        } else {
            MarkupContent("plaintext", documentationElement?.left ?: "")
        }
        set(value) { documentationElement = Either.forRight(value) }
}

/**
 * 签名帮助上下文
 */
data class SignatureHelpContext(
    @SerializedName("triggerKind") var triggerKind: Int, // 1: Invoked, 2: TriggerChar, 3: ContentChange
    @SerializedName("triggerCharacter") var triggerCharacter: String? = null,
    @SerializedName("isRetrigger") var isRetrigger: Boolean = false,
    @SerializedName("activeSignatureHelp") var activeSignatureHelp: SignatureHelp? = null
)

/**
 * Gson 可完美序列化的 LSP Position。
 * 替代原生 Protobuf 的 Position，以防止 GSON 序列化时将 Protobuf 内部字段暴露导致 JSON-RPC 拒绝解析。
 */
data class LspPosition(
    @SerializedName("line") val line: Int,
    @SerializedName("character") val character: Int
)

data class SignatureHelpParams(
    @SerializedName("textDocument") var textDocument: TextDocumentIdentifier,
    @SerializedName("position") var lspPosition: LspPosition,
    @SerializedName("context") var context: SignatureHelpContext? = null
) : CancellableRequestParams {


    @Transient
    override var cancelChecker: ICancelChecker = ICancelChecker.NOOP

    @Transient
    var file: Path? = null

    @Transient
    var content: CharSequence? = null

    @Transient
    var position: Position = Position(0, 0)

    /**
     * 兼容使用 Protobuf RpcPosition 构造（如 LspSignatureHelpManager.kt 中的安全调用转换）
     */
    constructor(
        textDocument: TextDocumentIdentifier,
        position: com.itsaky.androidide.lsp.rpc.Position,
        context: SignatureHelpContext? = null
    ) : this(
        textDocument,
        LspPosition(position.line, position.character),
        context
    )

    /**
     * 兼容旧版本 AndroidIDE 原生的 Editor 调用方式
     */
    constructor(
        file: Path,
        position: Position,
        content: CharSequence? = null,
        cancelChecker: ICancelChecker
    ) : this(
        textDocument = TextDocumentIdentifier(com.itsaky.androidide.lsp.rpc.UriConverter.pathToUri(file)),
        lspPosition = LspPosition(position.line, position.column),
        context = null
    ) {
        this.file = file
        this.position = position
        this.content = content
        this.cancelChecker = cancelChecker
    }
}