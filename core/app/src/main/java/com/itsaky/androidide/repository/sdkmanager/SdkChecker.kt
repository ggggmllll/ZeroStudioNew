package com.itsaky.androidide.repository.sdkmanager

import com.itsaky.androidide.app.configuration.IJdkDistributionProvider
import com.itsaky.androidide.utils.Environment

/** @author android_zero SDK 和 JDK 环境统管检查中心 */
object SdkChecker {

  /** 同步检查是否满足基础开发环境要求。 直接重载缓存判定是否具有JDK环境及 Android SDK 根目录 */
  fun isEnvironmentReadySync(): Boolean {
    // 强制触发一次实时的分布读取，防止包管理器后台安装后尚未通知 IDE
    val jdkProvider = IJdkDistributionProvider.getInstance()
    jdkProvider.loadDistributions()

    val hasJdk = jdkProvider.installedDistributions.isNotEmpty()

    val sdkDir = Environment.ANDROID_HOME
    val hasSdk = sdkDir.exists() && sdkDir.isDirectory

    return hasJdk && hasSdk
  }
}
