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

package com.itsaky.androidide.compose.preview.data.source

import com.itsaky.androidide.projects.IProjectManager
import com.itsaky.androidide.projects.android.AndroidModule
import org.slf4j.LoggerFactory
import java.io.File

/**
 * 提取项目上下文。
 * 为 Compose 预览服务提供必要的局部 Classpath 和 R.class 等依赖环境。
 *
 * @author android_zero
 */
data class ProjectContext(
    val modulePath: String?,
    val variantName: String,
    val compileClasspaths: List<File>,
    val intermediateClasspaths: Set<File>,
    val projectDexFiles: List<File>,
    val needsBuild: Boolean
)

class ProjectContextSource {

    fun resolveContext(filePath: String): ProjectContext {
        if (filePath.isBlank()) {
            LOG.warn("Empty file path provided for Hot-Reload, returning default context")
            return ProjectContext(null, "debug", emptyList(), emptySet(), emptyList(), false)
        }

        val file = File(filePath)
        val projectManager = IProjectManager.getInstance()
        val module = projectManager.findModuleForFile(file)

        if (module == null) {
            LOG.warn("No module found for file: {}", file.absolutePath)
            return ProjectContext(null, "debug", emptyList(), emptySet(), emptyList(), false)
        }

        val intermediateClasspaths = module.getIntermediateClasspaths()
        // Compose 热编译需要的完整 Classpath
        val compileClasspaths = (module.getCompileClasspaths() + intermediateClasspaths).distinct().filter { it.exists() }

        // AndroidIDE 提供生成的 .dex 供直接加载
        val projectDexFiles = module.getRuntimeDexFiles().toList().filter { it.exists() }
        val variantName = (module as? AndroidModule)?.getSelectedVariant()?.name ?: "debug"
        
        // 如果缺乏基本编译产物，说明需要让用户至少编译一次
        val needsBuild = intermediateClasspaths.isEmpty() || projectDexFiles.isEmpty()

        LOG.info("Hot-Reload Context - Module: {}, Variant: {}, CompileJars: {}, ProjectDex: {}, NeedsBuild: {}", 
                 module.path, variantName, compileClasspaths.size, projectDexFiles.size, needsBuild)

        return ProjectContext(
            modulePath = module.path,
            variantName = variantName,
            compileClasspaths = compileClasspaths,
            intermediateClasspaths = intermediateClasspaths,
            projectDexFiles = projectDexFiles,
            needsBuild = needsBuild
        )
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ProjectContextSource::class.java)
    }
}