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

package com.itsaky.androidide.templates.base.root

import com.itsaky.androidide.templates.base.ProjectTemplateBuilder

/** @author android_zero */
private fun ProjectTemplateBuilder.mavenUrl(url: String): String {
  return if (data.useKts) {
    "maven { url = uri(\"$url\") }"
  } else {
    "maven { url \"$url\" }"
  }
}

/** @author android_zero */
private val ProjectTemplateBuilder.repositoriesBlock: String
  get() =
      """
    ${mavenUrl("https://maven.aliyun.com/repository/gradle-plugin")}
    ${mavenUrl("https://maven.aliyun.com/repository/public")}
    ${mavenUrl("https://maven.aliyun.com/repository/google")}
    ${mavenUrl("https://cache-redirector.jetbrains.com/intellij-third-party-dependencies/")}
    ${mavenUrl("https://www.jetbrains.com/intellij-repository/releases/")}
    ${mavenUrl("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-ide-plugin-dependencies/")}
    ${mavenUrl("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap/")}
    ${mavenUrl("https://cache-redirector.jetbrains.com/kotlin.bintray.com/kotlin-plugin/")}
    ${mavenUrl("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-ide/")}
    ${mavenUrl("https://repo.itextsupport.com/android")}
    ${mavenUrl("https://repo1.maven.org/maven2/")}
    ${mavenUrl("https://jitpack.io")}
"""
          .trimIndent()

internal fun ProjectTemplateBuilder.settingsGradleSrcStr(): String {
  return """
pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    $repositoriesBlock
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    $repositoriesBlock
  }
}

rootProject.name = "${data.name}"

${modules.joinToString(separator = ", ") { "include(\"${it.name}\")" }}    
  """
      .trim()
}
