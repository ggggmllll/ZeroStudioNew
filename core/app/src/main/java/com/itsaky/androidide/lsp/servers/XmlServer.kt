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
package com.itsaky.androidide.lsp.servers

import android.content.Context
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.connection.ProcessStreamProvider
import com.itsaky.androidide.lsp.core.LspConnectionFactory
import com.itsaky.androidide.lsp.util.Logger
import com.itsaky.androidide.lsp.util.LspShellUtils
import com.itsaky.androidide.utils.Environment
import java.io.File

/**
 * An implementation of [BaseLspServer] for XML, utilizing the Lemminx language server.
 *
 * This server is run as a Java process, executing the `org.eclipse.lemminx.uber-jar.jar`.
 *
 * @author android_zero
 */
class XmlServer : BaseLspServer() {
  override val id: String = "xml-lsp"
  override val languageName: String = "XML"
  override val serverName: String = "lemminx"
  override val supportedExtensions: List<String> =
      listOf("xml", "xaml", "dtd", "plist", "ascx", "csproj", "wxi", "wxl", "wxs", "svg")

  /** Path to the Lemminx executable JAR within the AndroidIDE environment. */
  private val jarFile: File
    get() = File(Environment.ANDROIDIDE_HOME, "ideplugin/org.eclipse.lemminx.uber-jar.jar")

  /** Path to the Java executable. */
  private val javaBin: File
    get() = Environment.JAVA

  override fun isInstalled(context: Context): Boolean {
    return LspShellUtils.isTerminalEnvironmentReady() && jarFile.exists() && javaBin.exists()
  }

  override fun install(context: Context) {
    // Installation of Lemminx JAR is typically handled by ToolsManager or a similar setup utility.
    // This method can be used to trigger a re-installation or verification if needed.
    val installScript = File(Environment.HOME, ".androidide/local/bin/lsp/xml")
    if (installScript.exists()) {
      LspShellUtils.installPackage(installScript.absolutePath, "$id-installer")
    } else {
      Logger.instance(javaClass.simpleName)
          .error("Installation script for XML LSP not found at ${installScript.path}")
    }
  }

  override fun getConnectionFactory(): LspConnectionFactory {
    return LspConnectionFactory { workingDir ->
      ProcessStreamProvider(
          command = listOf(javaBin.absolutePath, "-jar", jarFile.absolutePath),
          workingDir = workingDir,
      )
    }
  }

  override fun isSupported(file: File): Boolean {
    return supportedExtensions.contains(file.getName().substringAfterLast("."))
  }
}
