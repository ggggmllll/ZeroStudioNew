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
package com.itsaky.androidide.repository.sdkmanager.viewmodel

import androidx.compose.ui.state.ToggleableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsaky.androidide.repository.sdkmanager.SdkRepository
import com.itsaky.androidide.repository.sdkmanager.models.InstallStatus
import com.itsaky.androidide.repository.sdkmanager.models.SdkTreeNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 专为 Compose UI 优化的状态管理器。
 *
 * @author android_zero
 */
class SdkManagerViewModel : ViewModel() {

  private val _platformsTree = MutableStateFlow<List<SdkTreeNode>>(emptyList())
  val platformsTree: StateFlow<List<SdkTreeNode>> = _platformsTree.asStateFlow()

  private val _toolsTree = MutableStateFlow<List<SdkTreeNode>>(emptyList())
  val toolsTree: StateFlow<List<SdkTreeNode>> = _toolsTree.asStateFlow()

  private val _isLoading = MutableStateFlow(true)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _hasPendingChanges = MutableStateFlow(false)
  val hasPendingChanges: StateFlow<Boolean> = _hasPendingChanges.asStateFlow()

  init {
    loadData()
  }

  fun loadData() {
    viewModelScope.launch {
      _isLoading.value = true
      try {
        val platforms = SdkRepository.getSdkPlatformsTree()
        val tools = SdkRepository.getSdkToolsTree()

        initCheckboxStates(platforms)
        initCheckboxStates(tools)

        _platformsTree.value = platforms
        _toolsTree.value = tools
        checkPendingChanges()
      } catch (e: Exception) {
        e.printStackTrace()
      } finally {
        _isLoading.value = false
      }
    }
  }

  private fun initCheckboxStates(nodes: List<SdkTreeNode>) {
    nodes.forEach { node ->
      if (node.isGroup) {
        initCheckboxStates(node.children)
        node.updateParentState()
      } else {
        node.checkedState =
            if (node.status == InstallStatus.INSTALLED) ToggleableState.On else ToggleableState.Off
      }
    }
  }

  fun toggleExpand(node: SdkTreeNode, isPlatformsTab: Boolean) {
    node.isExpanded = !node.isExpanded
    forceTreeUpdate(isPlatformsTab)
  }

  fun toggleCheck(node: SdkTreeNode, isPlatformsTab: Boolean) {
    val nextState =
        when (node.checkedState) {
          ToggleableState.On -> ToggleableState.Off
          ToggleableState.Off,
          ToggleableState.Indeterminate -> ToggleableState.On
        }

    node.updateChildrenState(nextState)
    node.updateParentState()

    checkPendingChanges()
    forceTreeUpdate(isPlatformsTab)
  }

  private fun forceTreeUpdate(isPlatformsTab: Boolean) {
    if (isPlatformsTab) {
      _platformsTree.value = _platformsTree.value.toList()
    } else {
      _toolsTree.value = _toolsTree.value.toList()
    }
  }

  private fun checkPendingChanges() {
    var hasChanges = false
    fun checkNode(node: SdkTreeNode) {
      if (!node.isGroup) {
        if (node.checkedState != node.originalCheckedState) {
          hasChanges = true
        }
      }
      node.children.forEach { checkNode(it) }
    }

    _platformsTree.value.forEach { checkNode(it) }
    _toolsTree.value.forEach { checkNode(it) }

    _hasPendingChanges.value = hasChanges
  }

  fun getPendingTasks(): Pair<List<SdkTreeNode>, List<SdkTreeNode>> {
    val toInstall = mutableListOf<SdkTreeNode>()
    val toDelete = mutableListOf<SdkTreeNode>()

    fun collect(node: SdkTreeNode) {
      if (!node.isGroup) {
        val wantsInstall = node.checkedState == ToggleableState.On
        val isInstalled = node.status == InstallStatus.INSTALLED

        if (wantsInstall && !isInstalled) {
          toInstall.add(node)
        } else if (!wantsInstall && isInstalled) {
          toDelete.add(node)
        }
      }
      node.children.forEach { collect(it) }
    }

    _platformsTree.value.forEach { collect(it) }
    _toolsTree.value.forEach { collect(it) }

    return Pair(toInstall, toDelete)
  }
}
