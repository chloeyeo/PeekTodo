package com.chloeyeo.peektodo.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.chloeyeo.peektodo.PeekTodoApp
import com.chloeyeo.peektodo.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settings: SettingsRepository) : ViewModel() {

    val blurMode: StateFlow<Boolean> = settings.blurMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val pinNotification: StateFlow<Boolean> = settings.pinNotification
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setBlurMode(enabled: Boolean) {
        viewModelScope.launch { settings.setBlurMode(enabled) }
    }

    /** NotificationSync observes this setting, so the notification updates in place on toggle. */
    fun setPinNotification(enabled: Boolean) {
        viewModelScope.launch { settings.setPinNotification(enabled) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PeekTodoApp
                SettingsViewModel(app.container.settingsRepository)
            }
        }
    }
}
