package com.itsaky.androidide.repository.sdkmanager.models

import androidx.compose.ui.state.ToggleableState
import com.google.gson.annotations.SerializedName
import java.util.UUID

/**
 * JSON 结构的 SDK 清单模型。
 * @author android_zero
 */
data class SdkManifest(
    @SerializedName("android_sdk") val androidSdk: String?,
    @SerializedName("cmdline_tools") val cmdlineTools: String?,
    @SerializedName("build_tools") val buildTools: Map<String, Map<String, String>>?,
    @SerializedName("platform_tools") val platformTools: Map<String, Map<String, String>>?,
    @SerializedName("android_ndk") val androidNdk: Map<String, Map<String, String>>?,
    @SerializedName("android_cmake") val androidCmake: Map<String, Map<String, String>>?,
    @SerializedName("jdk_11") val jdk11: Map<String, String>?,
    @SerializedName("jdk_17") val jdk17: Map<String, String>?,
    @SerializedName("jdk_21") val jdk21: Map<String, String>?,
    @SerializedName("jdk_22") val jdk22: Map<String, String>?,
    @SerializedName("jdk_23") val jdk23: Map<String, String>?,
    @SerializedName("jdk_24") val jdk24: Map<String, String>?,
    @SerializedName("jdk_25") val jdk25: Map<String, String>?,
    @SerializedName("jdk_26") val jdk26: Map<String, String>?
)

enum class InstallStatus {
    NOT_INSTALLED,
    INSTALLED,
    UPDATE_AVAILABLE
}

/**
 * 树形列表设计的数据节点。
 * 支持多层级、状态级联（父节点半选/全选推导）。
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
    val componentType: String = "", // e.g., "build-tools", "ndk"
    var isExpanded: Boolean = false,
    var checkedState: ToggleableState = ToggleableState.Off,
    var status: InstallStatus = InstallStatus.NOT_INSTALLED,
    val children: MutableList<SdkTreeNode> = mutableListOf(),
    var parent: SdkTreeNode? = null
) {
    /** 级联更新所有子节点的复选框状态 */
    fun updateChildrenState(newState: ToggleableState) {
        checkedState = newState
        children.forEach { child ->
            child.updateChildrenState(newState)
        }
    }

    /** 根据子节点状态，反向推导并更新父节点状态 (全选/半选/未选) */
    fun updateParentState() {
        var parentNode = this.parent
        while (parentNode != null) {
            val allChecked = parentNode.children.all { it.checkedState == ToggleableState.On }
            val allUnchecked = parentNode.children.all { it.checkedState == ToggleableState.Off }
            
            parentNode.checkedState = when {
                allChecked -> ToggleableState.On
                allUnchecked -> ToggleableState.Off
                else -> ToggleableState.Indeterminate
            }
            parentNode = parentNode.parent
        }
    }

    /** 获取原始的未修改的 CheckedState，用于判断是否发生了用户变更 */
    val originalCheckedState: ToggleableState
        get() = if (status == InstallStatus.INSTALLED) ToggleableState.On else ToggleableState.Off
}