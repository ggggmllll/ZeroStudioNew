package me.rerere.rikkahub.ui.pages.share.handler

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import me.rerere.rikkahub.data.datastore.Settings
import me.rerere.rikkahub.data.datastore.SettingsStore

class ShareHandlerVM(text: String, private val settingsStore: SettingsStore) : ViewModel() {
  val shareText = checkNotNull(text)
  val settings =
      settingsStore.settingsFlow.stateIn(viewModelScope, SharingStarted.Eagerly, Settings.dummy())

  suspend fun updateAssistant(assistantId: Uuid) {
    settingsStore.updateAssistant(assistantId)
  }
}
