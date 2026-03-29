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

package com.itsaky.androidide.lsp.connection

import com.itsaky.androidide.lsp.util.LspStatusMonitor
import io.github.rosemoe.sora.lsp.client.connection.StreamConnectionProvider
import java.io.FilterInputStream
import java.io.FilterOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * A wrapper around any [StreamConnectionProvider] that intercepts IO to provide real-time traffic
 * monitoring and logging for the [LspStatusMonitor].
 *
 * @author android_zero
 */
class MonitoringStreamProvider(
    private val delegate: StreamConnectionProvider,
    private val serverId: String,
    private val serverName: String,
) : StreamConnectionProvider {

  private val monitoredInput by lazy {
    MonitoredInputStream(delegate.inputStream) { bytes ->
      LspStatusMonitor.updateTraffic(serverId, 0, bytes.toLong())
    }
  }

  private val monitoredOutput by lazy {
    MonitoredOutputStream(delegate.outputStream) { bytes ->
      LspStatusMonitor.updateTraffic(serverId, bytes.toLong(), 0)
    }
  }

  override fun start() {
    LspStatusMonitor.updateStatus(serverId, serverName, LspStatusMonitor.ServerStatus.STARTING)
    try {
      delegate.start()
      LspStatusMonitor.updateStatus(serverId, serverName, LspStatusMonitor.ServerStatus.RUNNING)
    } catch (e: Exception) {
      LspStatusMonitor.logError(serverId, "Failed to start connection", e)
      throw e
    }
  }

  override val inputStream: InputStream
    get() = monitoredInput

  override val outputStream: OutputStream
    get() = monitoredOutput

  override val isClosed: Boolean
    get() = delegate.isClosed

  override fun close() {
    LspStatusMonitor.updateStatus(serverId, serverName, LspStatusMonitor.ServerStatus.STOPPED)
    delegate.close()
  }

  // --- Inner Streams ---

  private class MonitoredInputStream(inStream: InputStream, private val onRead: (Int) -> Unit) :
      FilterInputStream(inStream) {
    override fun read(): Int {
      val b = super.read()
      if (b != -1) onRead(1)
      return b
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
      val count = super.read(b, off, len)
      if (count > 0) onRead(count)
      return count
    }
  }

  private class MonitoredOutputStream(outStream: OutputStream, private val onWrite: (Int) -> Unit) :
      FilterOutputStream(outStream) {
    override fun write(b: Int) {
      super.write(b)
      onWrite(1)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
      super.write(b, off, len)
      onWrite(len)
    }
  }
}
