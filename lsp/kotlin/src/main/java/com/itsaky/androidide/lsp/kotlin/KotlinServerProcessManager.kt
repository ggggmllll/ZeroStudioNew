/*
 *  This file is part of AndroidCodeStudio.
 *
 *  AndroidCodeStudio is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  AndroidCodeStudio is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidCodeStudio.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.itsaky.androidide.lsp.kotlin

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.itsaky.androidide.lsp.kotlin.events.LspEventBus
import com.itsaky.androidide.lsp.kotlin.events.LspInstallRequestEvent
import com.itsaky.androidide.lsp.models.DiagnosticResult
import com.itsaky.androidide.utils.Environment
import com.itsaky.androidide.utils.executioncommand.FireAndForgetRunner
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import org.slf4j.LoggerFactory

/*
 * @author Mohammed-baqer-null @ https://github.com/Mohammed-baqer-null
 */

class KotlinServerProcessManager(context: Context) {

  companion object {
    private val log = LoggerFactory.getLogger(KotlinServerProcessManager::class.java)
  }

  private val context: Context = context.applicationContext

  private val gson = Gson()
  private var process: Process? = null
  private var writer: OutputStreamWriter? = null
  private var reader: BufferedReader? = null
  private val nextId = AtomicInteger(1)
  private val pendingRequests = ConcurrentHashMap<Int, (JsonObject?) -> Unit>()
  private var diagnosticsCallback: ((DiagnosticResult) -> Unit)? = null
  private val notificationHandler = KotlinNotificationHandler()

  @Volatile private var isInstallPromptShowing = false
  @Volatile private var userDeclinedInstall = false

  fun setDiagnosticsCallback(callback: (DiagnosticResult) -> Unit) {
    diagnosticsCallback = callback
    notificationHandler.setDiagnosticsCallback(callback)
  }

  /** 检查安装是否就绪。由于我们通过网络下载，环境存在缺失的可能。 */
  fun isInstalled(): Boolean {
    val serverHome = Environment.SERVERS_KOTLIN_DIR
    val libDir = File(serverHome, "lib")
    val launcher = File(serverHome, "bin/${KotlinServerConstants.LAUNCHER_SCRIPT_NAME}")

    if (!serverHome.exists() || !libDir.exists() || !launcher.exists()) {
      return false
    }

    return KotlinServerConstants.REQUIRED_LIB_JARS.all { jarName -> File(libDir, jarName).exists() }
  }

  fun shouldPromptInstallFor(file: File): Boolean {
    val isKotlinFile = file.name.endsWith(".kt") || file.name.endsWith(".kts")
    if (!isKotlinFile) return false
    return !isInstalled()
  }

  /** 触发 UI 的安装弹窗 (基于 Kotlin SharedFlow)。 */
  fun install(onComplete: () -> Unit) {
    if (isInstallPromptShowing || userDeclinedInstall) {
      KslLogs.info(
          "Installation skipped. Prompt is already showing or user declined it previously."
      )
      return
    }

    isInstallPromptShowing = true
    KslLogs.info("Requesting KLS installation UI...")

    val downloadUrl = KotlinServerConstants.DOWNLOAD_URL
    val installDir = Environment.SERVERS_KOTLIN_DIR

    val event =
        LspInstallRequestEvent(
            serverId = "kotlin-lsp-manager",
            serverName = "Kotlin Language Server (v1.6.5)",
            dialogTitle = "Install Kotlin LSP",
            dialogMessage =
                "Kotlin Language Server is required to provide full code completion, auto-import, and diagnostics for .kt/.kts files.\n\nIt will be installed to: ${installDir.absolutePath}",
            downloadUrl = downloadUrl,
            installPath = installDir,
            isZipArchive = true,
            confirmButtonText = "Install",
            onInstallComplete = {
              KslLogs.info("Kotlin LSP Installation Complete. Preparing server...")
              isInstallPromptShowing = false
              userDeclinedInstall = false // 重置状态，因为已经成功安装

              val launcher = File(installDir, "bin/${KotlinServerConstants.LAUNCHER_SCRIPT_NAME}")
              if (launcher.exists()) {
                launcher.setExecutable(true, false)
              }

              // 执行回调，如：重试启动服务器
              onComplete()
            },
            onInstallCancelled = {
              KslLogs.warn("User cancelled the Kotlin LSP installation.")
              isInstallPromptShowing = false
              // 记录用户的拒绝，本次会话内不再触发烦人的连环弹窗
              userDeclinedInstall = true
            },
        )

    // 通过 Flow 分发给 Activity 的收集器 (Collector) 去显示 Compose
    LspEventBus.postInstallRequest(event)
  }

  fun startServer(classpathProvider: KotlinClasspathProvider) {
    if (process?.isAlive == true) {
      KslLogs.debug("Server already running")
      return
    }

    if (!isInstalled()) {
      KslLogs.warn("Kotlin LSP is not installed. Requesting install UI.")
      install { startServer(classpathProvider) }
      return
    }

    KslLogs.info("Starting Kotlin Language Server...")

    val serverHome = Environment.SERVERS_KOTLIN_DIR
    val launcherScript = File(serverHome, "bin/${KotlinServerConstants.LAUNCHER_SCRIPT_NAME}")

    if (!serverHome.exists() || !launcherScript.exists()) {
      KslLogs.error("Server not found at: {}", serverHome.absolutePath)
      return
    }

    launcherScript.setExecutable(true, false)
    val androidClasspath = classpathProvider.getClasspath()

    // Get Java home directory
    val javaHome =
        if (Environment.JAVA_HOME != null) {
          Environment.JAVA_HOME.absolutePath
        } else {
          Environment.PREFIX.absolutePath
        }

    val command =
        listOf(
            Environment.BASH_SHELL.absolutePath,
            launcherScript.absolutePath,
            "-DkotlinLanguageServer.version=1.3.13",
            "-DkotlinLanguageServer.skipClasspathResolution=true",
            "-DkotlinLanguageServer.predefinedClasspath=$androidClasspath",
        )

    val processBuilder =
        ProcessBuilder(command).apply {
          redirectErrorStream(false)
          directory(serverHome)
          environment().apply {
            put("JAVA_HOME", javaHome)

            val currentPath = get("PATH") ?: ""
            put("PATH", "${Environment.BIN_DIR.absolutePath}:$currentPath")

            put("KOTLIN_LSP_DISABLE_DEPENDENCY_RESOLUTION", "true")
            put("KOTLIN_LSP_USE_PREDEFINED_CLASSPATH", "true")
            put("KOTLIN_LSP_CLASSPATH", androidClasspath)
            put("CLASSPATH", androidClasspath)

            val sdkPath = classpathProvider.getAndroidSdkPath()
            if (sdkPath.isNotEmpty()) {
              put("ANDROID_SDK_ROOT", sdkPath)
              put("ANDROID_HOME", sdkPath)
            }

            KslLogs.info("Environment: JAVA_HOME={}", javaHome)
            KslLogs.info("Environment: PATH={}", get("PATH"))
          }
        }

    try {
      process = processBuilder.start()
      writer = OutputStreamWriter(process!!.outputStream, StandardCharsets.UTF_8)
      reader = BufferedReader(InputStreamReader(process!!.inputStream, StandardCharsets.UTF_8))

      startReaderThread()
      startErrorReaderThread()

      // Send initialization with completion capabilities
      sendInitialization()

      KslLogs.info("Server started successfully with JAVA_HOME: {}", javaHome)
    } catch (e: Exception) {
      KslLogs.error("Failed to start server", e)
      startLauncherInBackground(launcherScript, androidClasspath, classpathProvider)
    }
  }

  private fun startLauncherInBackground(
      launcherScript: File,
      androidClasspath: String,
      classpathProvider: KotlinClasspathProvider,
  ) {
    try {
      val args =
          arrayOf(
              launcherScript.absolutePath,
              "-DkotlinLanguageServer.version=1.3.13",
              "-DkotlinLanguageServer.skipClasspathResolution=true",
              "-DkotlinLanguageServer.predefinedClasspath=$androidClasspath",
          )
      FireAndForgetRunner.fire(context, Environment.BASH_SHELL.absolutePath, args)
      val sdkPath = classpathProvider.getAndroidSdkPath()
      KslLogs.warn(
          "Fallback: launched Kotlin LSP in background via Termux shell API. script={}, sdkPath={}",
          launcherScript.absolutePath,
          sdkPath,
      )
    } catch (fallbackError: Exception) {
      KslLogs.error("Fallback start via Termux shell API failed", fallbackError)
    }
  }

  private fun sendInitialization() {
    val initParams =
        JsonObject().apply {
          addProperty("processId", android.os.Process.myPid())
          add("rootUri", null)

          add(
              "capabilities",
              JsonObject().apply {
                add(
                    "textDocument",
                    JsonObject().apply {
                      add(
                          "completion",
                          JsonObject().apply {
                            add(
                                "completionItem",
                                JsonObject().apply {
                                  addProperty("snippetSupport", true)
                                  addProperty("commitCharactersSupport", true)
                                  add(
                                      "documentationFormat",
                                      gson.toJsonTree(listOf("markdown", "plaintext")),
                                  )
                                  addProperty("deprecatedSupport", true)
                                  addProperty("preselectSupport", true)

                                  add(
                                      "resolveSupport",
                                      gson.toJsonTree(
                                          mapOf(
                                              "properties" to
                                                  listOf(
                                                      "documentation",
                                                      "detail",
                                                      "additionalTextEdits",
                                                  )
                                          )
                                      ),
                                  )
                                },
                            )
                            addProperty("contextSupport", true)
                          },
                      )

                      add(
                          "signatureHelp",
                          JsonObject().apply {
                            add(
                                "signatureInformation",
                                JsonObject().apply {
                                  add(
                                      "documentationFormat",
                                      gson.toJsonTree(listOf("markdown", "plaintext")),
                                  )
                                  add(
                                      "parameterInformation",
                                      JsonObject().apply {
                                        addProperty("labelOffsetSupport", true)
                                      },
                                  )
                                  addProperty("activeParameterSupport", true)
                                },
                            )
                            addProperty("contextSupport", true)
                          },
                      )
                    },
                )
                add(
                    "workspace",
                    JsonObject().apply {
                      addProperty("applyEdit", true)
                      add("workspaceEdit", gson.toJsonTree(mapOf("documentChanges" to true)))
                    },
                )
              },
          )
        }

    sendRequest("initialize", initParams) { result ->
      KslLogs.info("Server initialized: {}", result != null)
      sendNotification("initialized", JsonObject())
    }
  }

  fun sendRequest(method: String, params: JsonObject, callback: (JsonObject?) -> Unit) {
    val id = nextId.getAndIncrement()
    pendingRequests[id] = callback

    val payload =
        JsonObject().apply {
          addProperty("jsonrpc", "2.0")
          addProperty("id", id)
          addProperty("method", method)
          add("params", params)
        }

    KslLogs.debug("Sending request ID {}: {}", id, method)
    sendMessage(payload)
  }

  fun sendNotification(method: String, params: JsonObject) {
    val payload =
        JsonObject().apply {
          addProperty("jsonrpc", "2.0")
          addProperty("method", method)
          add("params", params)
        }

    KslLogs.debug("Sending notification: {}", method)
    sendMessage(payload)
  }

  private fun sendMessage(payload: JsonObject) {
    val data = gson.toJson(payload)
    val w =
        writer
            ?: run {
              KslLogs.error("Cannot send message: writer is null")
              return
            }

    synchronized(w) {
      try {
        val contentBytes = data.toByteArray(StandardCharsets.UTF_8)
        w.write("Content-Length: ${contentBytes.size}\r\n\r\n")
        w.write(data)
        w.flush()
      } catch (e: Exception) {
        KslLogs.error("Failed to send message", e)
      }
    }
  }

  private fun startReaderThread() {
    val r = reader ?: return
    Thread(
            {
              try {
                while (true) {
                  var contentLength = -1

                  while (true) {
                    val line = r.readLine() ?: return@Thread
                    if (line.isEmpty()) break

                    if (line.startsWith("Content-Length:", ignoreCase = true)) {
                      contentLength = line.substringAfter(":").trim().toIntOrNull() ?: -1
                    }
                  }

                  if (contentLength <= 0) continue

                  val buffer = CharArray(contentLength)
                  var totalRead = 0
                  while (totalRead < contentLength) {
                    val read = r.read(buffer, totalRead, contentLength - totalRead)
                    if (read < 0) break
                    totalRead += read
                  }

                  val json = String(buffer)
                  handleMessage(json)
                }
              } catch (e: Exception) {
                KslLogs.error("Error in reader thread", e)
              }
            },
            "kls-stdio-reader",
        )
        .start()
  }

  private fun startErrorReaderThread() {
    val errorReader =
        BufferedReader(InputStreamReader(process!!.errorStream, StandardCharsets.UTF_8))
    Thread(
            {
              try {
                var line: String?
                while (errorReader.readLine().also { line = it } != null) {
                  KslLogs.warn("KLS stderr: {}", line)
                }
              } catch (e: Exception) {
                KslLogs.error("Error in error reader thread", e)
              }
            },
            "kls-error-reader",
        )
        .start()
  }

  private fun handleMessage(json: String) {
    try {
      val obj = gson.fromJson(json, JsonObject::class.java)

      if (obj.has("id")) {
        val id = obj.get("id").asInt
        val callback = pendingRequests.remove(id)

        if (callback == null) {
          KslLogs.warn("No callback found for request ID: {}", id)
          return
        }

        if (obj.has("error")) {
          val error = obj.getAsJsonObject("error")
          val errorMsg = error.get("message")?.asString ?: "Unknown error"
          val errorCode = error.get("code")?.asInt ?: -1
          KslLogs.error("LSP error response for request {}: [{}] {}", id, errorCode, errorMsg)
          callback.invoke(null)
        } else if (obj.has("result")) {
          val result = obj.get("result")

          when {
            result.isJsonNull -> {
              KslLogs.debug("Request {} returned null result", id)
              callback.invoke(null)
            }
            result.isJsonArray -> {
              KslLogs.debug(
                  "Request {} returned array result with {} items",
                  id,
                  result.asJsonArray.size(),
              )
              val wrappedResult = JsonObject().apply { add("result", result) }
              callback.invoke(wrappedResult)
            }
            result.isJsonObject -> {
              KslLogs.debug("Request {} returned object result", id)
              callback.invoke(result.asJsonObject)
            }
            result.isJsonPrimitive -> {
              KslLogs.debug("Request {} returned primitive result", id)
              val wrappedResult = JsonObject().apply { add("result", result) }
              callback.invoke(wrappedResult)
            }
            else -> {
              KslLogs.warn("Request {} returned unknown result type", id)
              callback.invoke(null)
            }
          }
        } else {
          KslLogs.warn("Request {} has neither result nor error", id)
          callback.invoke(null)
        }
      } else if (obj.has("method")) {
        notificationHandler.handle(obj)
      } else {
        KslLogs.warn("Message has neither id nor method: {}", json)
      }
    } catch (e: Exception) {
      KslLogs.error("Error handling message: {}", json.take(200), e)
    }
  }

  private fun findJavaExecutable(): String {
    val candidates =
        listOf(
            "/data/data/com.itsaky.androidide/files/usr/bin/java",
            System.getenv("JAVA_HOME")?.let { "$it/bin/java" },
            "java",
        )

    return candidates.filterNotNull().firstOrNull { path ->
      try {
        File(path).exists() || Runtime.getRuntime().exec(arrayOf(path, "-version")).waitFor() == 0
      } catch (e: Exception) {
        false
      }
    } ?: "java"
  }

  fun shutdown() {
    try {
      sendRequest("shutdown", JsonObject()) {}
      sendNotification("exit", JsonObject())
    } catch (e: Exception) {
      KslLogs.error("Error during shutdown", e)
    }

    try {
      writer?.close()
    } catch (e: Exception) {}
    try {
      reader?.close()
    } catch (e: Exception) {}
    try {
      process?.destroy()
    } catch (e: Exception) {}

    process = null
    pendingRequests.clear()
  }
}
