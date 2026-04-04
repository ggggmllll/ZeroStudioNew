package com.itsaky.androidide.lsp.clangd

import java.util.concurrent.CopyOnWriteArraySet

class ClangdHealthMonitor {

  enum class State {
    STARTING,
    RUNNING,
    DEGRADED,
    STOPPED,
  }

  data class Event(
      val type: String,
      val message: String,
      val timestamp: Long = System.currentTimeMillis(),
  )

  fun interface Listener {
    fun onEvent(event: Event, state: State)
  }

  private val listeners = CopyOnWriteArraySet<Listener>()

  @Volatile
  var state: State = State.STOPPED
    private set

  fun onInitialize() {
    state = State.STARTING
    notify(Event("INIT", "clangd initialize requested"))
  }

  fun onInitialized(ok: Boolean) {
    state = if (ok) State.RUNNING else State.DEGRADED
    notify(Event("INIT_RESULT", if (ok) "clangd ready" else "clangd init failed"))
  }

  fun onNativeHealth(type: String, message: String) {
    state =
        when (type) {
          "CLANGD_EXIT",
          "IO_ERROR",
          "INIT_FAILURE" -> State.DEGRADED
          else -> state
        }
    notify(Event(type, message))
  }

  fun onShutdown() {
    state = State.STOPPED
    notify(Event("SHUTDOWN", "clangd shutdown"))
  }

  fun addListener(listener: Listener) {
    listeners.add(listener)
  }

  fun removeListener(listener: Listener) {
    listeners.remove(listener)
  }

  private fun notify(event: Event) {
    listeners.forEach { it.onEvent(event, state) }
  }
}
