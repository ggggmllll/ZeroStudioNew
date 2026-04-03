package com.itsaky.androidide.repository.sdkmanager.models

import androidx.compose.ui.state.ToggleableState
import java.util.UUID

enum class InstallStatus {
  NOT_INSTALLED,
  INSTALLED,
  UPDATE_AVAILABLE,
}

/**
 * 专为 SDK 管理器设计的独立树节点数据模型。 包含层级属性、组件信息以及级联状态逻辑。
 *
 * @author android_zero
 */
data class SdkTreeNode(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val apiLevel: String = "",
    val revision: String = "",
    val downloadUrl: String = "",
    val isGroup: Boolean = false,
    val level: Int = 0,
    val componentType: String = "",
    var isExpanded: Boolean = false,
    var checkedState: ToggleableState = ToggleableState.Off,
    var status: InstallStatus = InstallStatus.NOT_INSTALLED,
    val children: MutableList<SdkTreeNode> = mutableListOf(),
    var parent: SdkTreeNode? = null,
) {
  /** 级联更新所有子节点的复选框状态 */
  fun updateChildrenState(newState: ToggleableState) {
    checkedState = newState
    children.forEach { it.updateChildrenState(newState) }
  }

  /** 根据子节点状态，反向推导并更新父节点状态 (全选/半选/未选) */
  fun updateParentState() {
    var parentNode = this.parent
    while (parentNode != null) {
      val allChecked = parentNode.children.all { it.checkedState == ToggleableState.On }
      val allUnchecked = parentNode.children.all { it.checkedState == ToggleableState.Off }

      parentNode.checkedState =
          when {
            allChecked -> ToggleableState.On
            allUnchecked -> ToggleableState.Off
            else -> ToggleableState.Indeterminate
          }
      parentNode = parentNode.parent
    }
  }

  val originalCheckedState: ToggleableState
    get() = if (status == InstallStatus.INSTALLED) ToggleableState.On else ToggleableState.Off
}
