package moe.shizuku.manager.starter

import java.io.File
import moe.shizuku.manager.application

object Starter {

  private val starterFile = File(application.applicationInfo.nativeLibraryDir, "libshizuku.so")

  val userCommand: String = starterFile.absolutePath

  val adbCommand = "adb shell $userCommand"

  val internalCommand = "$userCommand --apk=${application.applicationInfo.sourceDir}"
}
