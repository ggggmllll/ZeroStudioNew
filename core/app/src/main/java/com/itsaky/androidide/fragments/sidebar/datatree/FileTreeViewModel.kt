package com.itsaky.androidide.fragments.sidebar.datatree

import android.zero.studio.view.filetree.widget.FileTree
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.itsaky.androidide.tasks.executeAsync

/** @author android_zero */
internal class FileTreeViewModel : ViewModel() {
  private val _treeState = MutableLiveData<String?>()
  val treeState: MutableLiveData<String?>
    get() = _treeState

  val savedState: String
    get() = _treeState.value ?: ""

  fun saveState(treeView: FileTree?) {
    treeView?.let { tree ->
      // Use the two-lambda version of executeAsync:
      // executeAsync({ background task }, { ui callback })
      executeAsync(
          { tree.getSaveState() }, // Background task
          { result -> _treeState.value = result }, // UI thread callback
      )
    }
  }
}
