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
package android.zero.studio.view.filetree.provider

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import android.zero.studio.view.filetree.R
import android.zero.studio.view.filetree.interfaces.FileIconProvider
import android.zero.studio.view.filetree.interfaces.FileObject
import android.zero.studio.view.filetree.model.Node

class DefaultFileIconProvider(context: Context) : FileIconProvider {
  private val file = ContextCompat.getDrawable(context, R.drawable.file)
  private val folder = ContextCompat.getDrawable(context, R.drawable.folder)
  private val chevronRight = ContextCompat.getDrawable(context, R.drawable.ic_chevron_right)
  private val expandMore = ContextCompat.getDrawable(context, R.drawable.round_expand_more_24)

  override fun getIcon(node: Node<FileObject>): Drawable? {
    return if (node.value.isFile()) {
      file
    } else {
      folder
    }
  }

  override fun getChevronRight(): Drawable? {
    return chevronRight
  }

  override fun getExpandMore(): Drawable? {
    return expandMore
  }
}
