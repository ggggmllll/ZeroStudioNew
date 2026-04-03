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
package com.itsaky.androidide.viewmodel

import android.zero.studio.view.filetree.widget.FileTree
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.itsaky.androidide.tasks.executeAsync

/**
 * ViewModel for the internal data file tree fragment.
 *
 * @author android_zero
 */
internal class DataFileTreeViewModel : ViewModel() {
  private val _treeState = MutableLiveData<String?>()
  val treeState: MutableLiveData<String?>
    get() = _treeState

  val savedState: String
    get() = _treeState.value ?: ""

  fun saveState(treeView: FileTree?) {
    treeView?.let { tree ->
      executeAsync({ tree.getSaveState() }, { result -> _treeState.value = result })
    }
  }
}
