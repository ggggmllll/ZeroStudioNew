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
package android.zero.studio.view.filetree.util

import android.zero.studio.view.filetree.interfaces.FileObject
import android.zero.studio.view.filetree.model.Node

object Sorter {
  fun sort(root: FileObject): List<Node<FileObject>> {
    val list = (root.listFiles() ?: return emptyList()).toMutableList()
    val dirs = list.filter { it.isDirectory() }.sortedBy { it.getName() }
    val files = (list - dirs.toSet()).sortedBy { it.getName() }
    return (dirs + files).map { Node(it) }.toMutableList()
  }
}
