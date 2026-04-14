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

package com.itsaky.androidide.lsp.kotlin

import android.content.Context
import com.itsaky.androidide.lsp.api.ILanguageServerRegistry
import com.itsaky.androidide.lsp.kotlin.events.KotlinLanguageClientImpl
import com.itsaky.androidide.lsp.kotlin.events.KotlinTextDocumentSyncHandler
import com.itsaky.androidide.lsp.kotlin.settings.KotlinServerSettings
import com.itsaky.androidide.utils.ILogger

/**
 * Kotlin LSP 深度集成的中枢与大门。
 * 
 * 作用与功能：
 * 对外暴露唯一的初始化 API `setup()`。
 * 调用此方法后，将依次完成：
 * 1. 注册编辑器生命周期同步（TextDocument Sync）。
 * 2. 检查并启动后台 Kotlin 服务进程。
 * 3. 实例化并绑定 ILanguageClient（接收诊断信息等）。
 * 4. 推送初始的用户首选项配置（Settings）。
 *
 * @author android_zero
 */
object KotlinLspIntegration {

  private val log = ILogger.instance("KotlinLspIntegration")
  
  private var isInitialized = false

  /**
   * 启动并组装 Kotlin Language Server 环境。
   * 建议在 ProjectManagerImpl 完成 `setupProject` 或 Application onCreate 后调用。
   *
   * @param context Application 或 Activity Context
   */
  @Synchronized
  fun setup(context: Context) {
    if (isInitialized) {
      log.info("Kotlin LSP is already initialized. Skipping.")
      return
    }

    try {
      KotlinTextDocumentSyncHandler.init()

      // 启动或安装子进程
      val processManager = KotlinServerProcessManager(context.applicationContext)
      processManager.startServer()

      val clientImpl = KotlinLanguageClientImpl()
      ILanguageServerRegistry.getDefault().connectClient(clientImpl)
      
      // 下发初始化配置 (格式化、诊断规则等)
      Thread {
         var attempts = 0
         var server: KotlinLanguageServerImpl? = null
         while (attempts < 20 && server == null) {
            server = ILanguageServerRegistry.getDefault().getServer("kotlin-lsp") as? KotlinLanguageServerImpl
            if (server == null) Thread.sleep(500)
            attempts++
         }

         if (server != null) {
            server.connectClient(clientImpl)
            
            // 下发首选项设置
            val settings = KotlinServerSettings()
            server.applySettings(settings)
            log.info("Kotlin LSP Integration successfully stitched together!")
         } else {
            log.warn("Kotlin LSP Server failed to start within timeout.")
         }
      }.start()

      isInitialized = true
    } catch (e: Exception) {
      log.error("Critical error during Kotlin LSP Integration setup", e)
    }
  }
}