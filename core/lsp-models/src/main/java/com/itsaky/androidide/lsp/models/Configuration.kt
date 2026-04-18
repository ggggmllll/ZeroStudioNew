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
/*
 *  @author android_zero
 */
package com.itsaky.androidide.lsp.models

/**
 * 配置请求参数 (workspace/configuration)
 */
data class ConfigurationParams(
    val items: List<ConfigurationItem>
)

data class ConfigurationItem(
    val scopeUri: String? = null, 
    val section: String? = null   
)

/**
 * 配置变更通知参数 (workspace/didChangeConfiguration)
 */
data class DidChangeConfigurationParams(
    val settings: Any
)