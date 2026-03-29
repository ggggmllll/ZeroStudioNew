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

package com.itsaky.androidide.fragments.git.tree

import java.io.File

/**
 * TreeView 节点数据模型
 *
 * @author android_zero
 */
data class FileNodeData(
    val file: File,
    var isHighlighted: Boolean = false,
    var isLoading: Boolean = false,
) {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as FileNodeData
    return file.absolutePath == other.file.absolutePath
  }

  override fun hashCode(): Int {
    return file.absolutePath.hashCode()
  }
}
