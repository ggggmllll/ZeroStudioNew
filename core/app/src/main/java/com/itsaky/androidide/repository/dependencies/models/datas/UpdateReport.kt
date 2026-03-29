package com.itsaky.androidide.repository.dependencies.models.datas

import com.itsaky.androidide.repository.dependencies.models.enums.*
import com.itsaky.androidide.repository.dependencies.models.interfaces.*

/**
 * 更新检测报告。
 *
 * <p>
 * 当后台网络请求对比完成后，生成此报告交由 Compose UI 层进行渲染。 </p>
 *
 * @property dependency 需要更新的源依赖对象，其必须是 [ScopedDependencyInfo] 类型以携带足够上下文。
 * @property latestVersion 探测到的最新稳定版本。
 * @property availableVersions 该组件所有可用的历史版本列表（通常按倒序排列）。
 */
data class UpdateReport(
    val dependency: ScopedDependencyInfo,
    val latestVersion: String,
    val availableVersions: List<String>,
)
