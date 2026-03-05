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

package com.itsaky.androidide.fragments.onboarding

import com.itsaky.androidide.app.configuration.CpuArch

private val ARM_ONLY = arrayOf(CpuArch.AARCH64, CpuArch.ARM)
private val ARM_AARCH64 = arrayOf(CpuArch.AARCH64)
private val ALL = arrayOf(CpuArch.AARCH64, CpuArch.ARM, CpuArch.X86_64)


/**
 * Android sdk versions.
 *
 * @author Akash Yadav
 */
enum class SdkVersion(val version: String, val supportedArchs: Array<CpuArch>) {

  SDK_33_0_1("33.0.1", ARM_ONLY),
  SDK_34_0_0("34.0.0", ARM_ONLY),
  SDK_34_0_1("34.0.1", ARM_ONLY),
  SDK_34_0_3("34.0.3", ARM_ONLY),
  SDK_34_0_4("34.0.4", ALL),
  SDK_35_0_1("35.0.1", ARM_ONLY),
  SDK_35_0_2("35.0.2", ALL),
  SDK_36_0_0("36.0.0", ALL),
  ;

  val displayName = "SDK $version"

  companion object {

    @JvmStatic
    fun fromDisplayName(displayName: CharSequence) =
      entries.first { it.displayName.contentEquals(displayName) }

    @JvmStatic
    fun fromVersion(version: CharSequence) = entries.first { it.version.contentEquals(version) }
  }
}

/**
 * Android NDK versions.
 *
 * @author android_zero
 */
enum class NdkVersion(val version: String, val supportedArchs: Array<CpuArch>) {

  NDK_R29_0_2("r29_0_14033849_beta4", ALL),
  NDK_R29_0_1("r29.0.14206865", ALL),
  NDK_R28_0_1("r28_2_13676358", ALL),
  NDK_R27_0_1("r27_3_13750724", ALL),
  NDK_R26_0_1("r26_3_11579264", ALL),
  // NDK_R25_0_1("xxx", ARM_ONLY),
  NDK_R24_0_1("r24.0.8215888", ARM_AARCH64),
  NDK_R23_0_1("r23.2.8568313", ARM_AARCH64),
  NDK_R22_0_1("r22.1.7171670", ARM_AARCH64),
  NDK_R21_0_1("r21.4.7075529", ARM_AARCH64),
  NDK_R20_0_1("r20.1.5948944", ARM_AARCH64),
  NDK_R19_0_1("r19.2.5345600", ARM_AARCH64),
  NDK_R18_0_1("r18.1.5063045", ARM_AARCH64),
  NDK_R17_0_1("r17.2.4988734", ARM_AARCH64),
  
  ;

  val displayName = "NDK $version"

  companion object {

    @JvmStatic
    fun fromDisplayName(displayName: CharSequence) =
      entries.first { it.displayName.contentEquals(displayName) }

    @JvmStatic
    fun fromVersion(version: CharSequence) = entries.first { it.version.contentEquals(version) }
  }
}


/**
 * JDK versions.
 *
 * @author Akash Yadav
 */
enum class JdkVersion(val version: String) {

  JDK_17("17"),
  JDK_21("21"),
  ;

  val displayName = "JDK $version"

  companion object {

    @JvmStatic
    fun fromDisplayName(displayName: CharSequence) =
      entries.first { it.displayName.contentEquals(displayName) }

    @JvmStatic
    fun fromVersion(version: CharSequence) = entries.first { it.version.contentEquals(version) }
  }
}