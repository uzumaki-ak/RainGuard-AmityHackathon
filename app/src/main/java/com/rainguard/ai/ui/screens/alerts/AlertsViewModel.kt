package com.rainguard.ai.ui.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainguard.ai.data.model.Alert
import com.rainguard.ai.data.repository.AlertRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class AlertsState(
    val alerts: List<Alert> = emptyList(),
    val filter: AlertFilter = AlertFilter.ACTIVE,
    val isLoading: Boolean = true,
    val selectedAlert: Alert? = null,
    val showSuccessMessage: String? = null
)

enum class AlertFilter {
    ACTIVE, ALL
}

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val alertRepository: AlertRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AlertsState())
    val state: StateFlow<AlertsState> = _state.asStateFlow()

    init {
        loadAlerts()
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            alertRepository.getAllAlerts()
                .catch { e ->
                    Timber.e(e, "Error loading alerts")
                }
                .collect { alerts ->
                    _state.value = _state.value.copy(
                        alerts = alerts,
                        isLoading = false
                    )
                }
        }
    }

    fun setFilter(filter: AlertFilter) {
        _state.value = _state.value.copy(filter = filter)
    }

    fun selectAlert(alert: Alert) {
        _state.value = _state.value.copy(selectedAlert = alert)
    }

    fun dismissAlert() {
        _state.value = _state.value.copy(selectedAlert = null)
    }

    fun acknowledgeAlert(alertId: String) {
        viewModelScope.launch {
            alertRepository.acknowledgeAlert(alertId)
            _state.value = _state.value.copy(
                showSuccessMessage = "Alert acknowledged ✓"
            )
            loadAlerts() // Refresh
        }
    }

    fun sendSafeStatus(alertId: String) {
        viewModelScope.launch {
            alertRepository.sendSafeStatus(alertId, null, null)
            _state.value = _state.value.copy(
                showSuccessMessage = "Safe status sent! ✓"
            )
            loadAlerts() // Refresh
        }
    }

    fun clearSuccessMessage() {
        _state.value = _state.value.copy(showSuccessMessage = null)
    }
}