package com.rainguard.ai.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainguard.ai.data.local.datastore.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val currentPage: Int = 0,
    val locationPermissionGranted: Boolean = false,
    val notificationPermissionGranted: Boolean = false,
    val demoMode: Boolean = false,
    val selectedLanguage: String = "English",
    val canContinue: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun setPage(page: Int) {
        _state.value = _state.value.copy(currentPage = page)
    }

    fun setLocationPermission(granted: Boolean) {
        _state.value = _state.value.copy(
            locationPermissionGranted = granted,
            canContinue = true
        )
        viewModelScope.launch {
            preferencesManager.setLocationAllowed(granted)
        }
    }

    fun setNotificationPermission(granted: Boolean) {
        _state.value = _state.value.copy(notificationPermissionGranted = granted)
        viewModelScope.launch {
            preferencesManager.setNotificationsAllowed(granted)
        }
    }

    fun setDemoMode(enabled: Boolean) {
        _state.value = _state.value.copy(demoMode = enabled)
        viewModelScope.launch {
            preferencesManager.setDemoMode(enabled)
        }
    }

    fun setLanguage(language: String) {
        _state.value = _state.value.copy(selectedLanguage = language)
        viewModelScope.launch {
            preferencesManager.setLanguage(language.lowercase())
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesManager.setFirstRunComplete()
        }
    }
}