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

package com.itsaky.androidide.compose.preview.data.repository

import android.content.Context
import com.itsaky.androidide.compose.preview.compiler.CompileDiagnostic
import com.itsaky.androidide.compose.preview.data.source.ProjectContext
import com.itsaky.androidide.compose.preview.domain.model.ParsedPreviewSource
import java.io.File

/**
 * 负责调度增量编译与 DEX 转换的生命周期仓库协议。
 *
 * @author android_zero
 */
interface ComposePreviewRepository {

    suspend fun initialize(context: Context, filePath: String): Result<InitializationResult>

    suspend fun compilePreview(
        source: String,
        parsedSource: ParsedPreviewSource
    ): Result<CompilationResult>

    fun computeSourceHash(source: String): String

    fun reset()
}

sealed class InitializationResult {
    data class Ready(
        val runtimeDex: File?,
        val projectContext: ProjectContext
    ) : InitializationResult()

    data class NeedsBuild(
        val modulePath: String,
        val variantName: String
    ) : InitializationResult()

    data class Failed(val message: String) : InitializationResult()
}

data class CompilationResult(
    val dexFile: File,
    val className: String,
    val runtimeDex: File?,
    val projectDexFiles: List<File>
)

class CompilationException(
    message: String,
    val diagnostics: List<CompileDiagnostic> = emptyList()
) : Exception(message)