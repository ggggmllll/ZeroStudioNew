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

package com.itsaky.androidide.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.viewbinding.ViewBinding
import com.itsaky.androidide.databinding.FragmentEmptyStateBinding

/**
 * A fragment that shows a message when there is no data to show in the subclass fragment.
 *
 * @author Akash Yadav
 */
abstract class EmptyStateFragment<T : ViewBinding> : FragmentWithBinding<T> {

  constructor(layout: Int, bind: (View) -> T) : super(layout, bind)

  constructor(inflate: (LayoutInflater, ViewGroup?, Boolean) -> T) : super(inflate)

  protected var emptyStateBinding: FragmentEmptyStateBinding? = null
    private set

  private val emptyStateUiState = EmptyStateUiState()

  internal var isEmpty: Boolean
    get() = emptyStateUiState.isEmpty.value ?: false
    set(value) {
      emptyStateUiState.isEmpty.value = value
    }

  protected var emptyMessage: CharSequence?
    get() = emptyStateUiState.emptyMessage.value
    set(value) {
      emptyStateUiState.emptyMessage.value = value
    }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {

    return FragmentEmptyStateBinding.inflate(inflater, container, false)
        .also { emptyStateBinding ->
          this.emptyStateBinding = emptyStateBinding

          // add the main fragment view
          emptyStateBinding.root.addView(
              super.onCreateView(inflater, emptyStateBinding.root, savedInstanceState)
          )
        }
        .root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    emptyStateUiState.isEmpty.observe(viewLifecycleOwner) { isEmpty ->
      emptyStateBinding?.apply { root.displayedChild = if (isEmpty) 0 else 1 }
    }

    emptyStateUiState.emptyMessage.observe(viewLifecycleOwner) { message ->
      emptyStateBinding?.emptyView?.message = message ?: ""
    }
  }

  override fun onDestroyView() {
    this.emptyStateBinding = null
    super.onDestroyView()
  }

  override fun onDestroy() {
    clearLegacyEmptyStateDelegate()
    super.onDestroy()
  }

  private fun clearLegacyEmptyStateDelegate() {
    // Backward-safe cleanup: if an older build variant still has the delegated
    // `emptyStateViewModel` field, clear it to avoid retaining a cleared ViewModel.
    runCatching {
      val delegateField = findFieldInHierarchy("emptyStateViewModel\$delegate") ?: return@runCatching
      delegateField.isAccessible = true
      val delegate = delegateField.get(this@EmptyStateFragment) ?: return@runCatching

      // `ViewModelLazy.cached` holds the cleared ViewModel reference.
      val cachedField = delegate.javaClass.getDeclaredField("cached")
      cachedField.isAccessible = true
      cachedField.set(delegate, null)
      delegateField.set(this@EmptyStateFragment, null)
    }
  }

  private fun findFieldInHierarchy(name: String): java.lang.reflect.Field? {
    var type: Class<*>? = javaClass
    while (type != null) {
      runCatching { type.getDeclaredField(name) }.getOrNull()?.let { return it }
      type = type.superclass
    }
    return null
  }

  protected class EmptyStateUiState {
    val isEmpty = MutableLiveData(true)
    val emptyMessage = MutableLiveData<CharSequence?>("")
  }
}
