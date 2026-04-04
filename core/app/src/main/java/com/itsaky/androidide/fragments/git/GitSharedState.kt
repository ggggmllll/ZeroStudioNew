package com.itsaky.androidide.fragments.git

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object GitSharedState {
  private val _selectedDiffPath = MutableStateFlow<String?>(null)
  val selectedDiffPath = _selectedDiffPath.asStateFlow()

  private val _selectedCommitHash = MutableStateFlow<String?>(null)
  val selectedCommitHash = _selectedCommitHash.asStateFlow()

  fun openDiffForPath(path: String) {
    _selectedDiffPath.value = path
  }

  fun openDiffForCommit(hash: String) {
    _selectedCommitHash.value = hash
  }
}

