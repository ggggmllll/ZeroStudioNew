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
/*
 *  @author android_zero
 */
package com.itsaky.androidide.editor.lsp

import com.itsaky.androidide.lsp.api.*
import com.itsaky.androidide.lsp.rpc.LspServiceGrpc
import io.grpc.ManagedChannelBuilder
import org.slf4j.LoggerFactory

class LspServiceConnector(
    private val host: String,
    private val port: Int,
    private val client: ILanguageClient
) {
    private val log = LoggerFactory.getLogger(LspServiceConnector::class.java)
    
    private val channel = ManagedChannelBuilder.forAddress(host, port)
        .usePlaintext() 
        .build()

    private val connectionManager = LspConnectionManager(client)
    private val stub = LspServiceGrpc.newStub(channel)

    fun connect(): LspConnectionManager {
        val requestObserver = stub.connection(connectionManager)
        connectionManager.bindStream(requestObserver)
        log.info("Connected to LSP Server gRPC at $host:$port")
        return connectionManager
    }

    fun shutdown() {
        channel.shutdown()
        log.info("LSP gRPC Channel Shutdown")
    }
}