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

package com.itsaky.androidide.repository.sdkmanager

import com.itsaky.androidide.utils.Environment
import java.io.File

/**
 * 统一的环境验证与检查中心
 *
 * @author android_zero
 */
object SdkChecker {

  /** 同步检查是否满足基础开发环境要求。 */
  fun isEnvironmentReadySync(): Boolean {
    return hasValidJdk() && hasValidSdk()
  }

  fun hasValidJdk(): Boolean {
    // 检查 pkg install 安装的 JDK (通常在 PREFIX/lib/jvm)
    val jvmDir = File(Environment.PREFIX, "lib/jvm")
    if (jvmDir.exists() && jvmDir.isDirectory) {
      val jdks = jvmDir.listFiles()
      if (jdks != null) {
        for (jdk in jdks) {
          val javaBin = File(jdk, "bin/java")
          if (javaBin.exists() && javaBin.canExecute()) {
            return true
          }
        }
      }
    }

    // 检查可能解压到 opt 的 JDK (PREFIX/opt/openjdk-*)
    val optDir = File(Environment.PREFIX, "opt")
    if (optDir.exists() && optDir.isDirectory) {
      val jdks = optDir.listFiles { _, name -> name.startsWith("openjdk") }
      if (jdks != null) {
        for (jdk in jdks) {
          val javaBin = File(jdk, "bin/java")
          if (javaBin.exists() && javaBin.canExecute()) {
            return true
          }
        }
      }
    }

    return false
  }

  fun hasValidSdk(): Boolean {
    val sdkDir = Environment.ANDROID_HOME
    if (!sdkDir.exists() || !sdkDir.isDirectory) return false

    // 检查 SDK 内部是否包含了必要的子目录（如 build-tools 或 platform-tools 等）
    // 只要有任意一个核心组件，就认为 SDK 基础目录存在且有效
    val buildTools = File(sdkDir, "build-tools")
    val platformTools = File(sdkDir, "platform-tools")
    val cmdlineTools = File(sdkDir, "cmdline-tools")
    val platforms = File(sdkDir, "platforms")

    return buildTools.exists() ||
        platformTools.exists() ||
        cmdlineTools.exists() ||
        platforms.exists()
  }
}
