package com.rainguard.ai.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainguard.ai.data.local.datastore.PreferencesManager
import com.rainguard.ai.data.model.EmergencyContact
import com.rainguard.ai.data.model.UserRole
import com.rainguard.ai.data.repository.EmergencyContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val demoMode: Boolean = false,
    val language: String = "en",
    val textSize: String = "default",
    val contacts: List<EmergencyContact> = emptyList(),
    val userRole: UserRole? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val contactRepository: EmergencyContactRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                preferencesManager.demoMode,
                preferencesManager.language,
                preferencesManager.textSize,
                contactRepository.getContacts(),
                preferencesManager.userRole
            ) { demo, lang, size, contacts, role ->
                SettingsState(demo, lang, size, contacts, role)
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun setDemoMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setDemoMode(enabled)
        }
    }

    fun setUserRole(role: UserRole) {
        viewModelScope.launch {
            preferencesManager.setUserRole(role, true)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            preferencesManager.setLanguage(lang)
        }
    }

    fun setTextSize(size: String) {
        viewModelScope.launch {
            preferencesManager.setTextSize(size)
        }
    }

    fun addContact(name: String, phone: String) {
        viewModelScope.launch {
            contactRepository.addContact(name, phone)
        }
    }

    fun deleteContact(contact: EmergencyContact) {
        viewModelScope.launch {
            contactRepository.deleteContact(contact)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            preferencesManager.clearAll()
        }
    }
}