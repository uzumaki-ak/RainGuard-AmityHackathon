package com.rainguard.ai.ui.screens.report

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainguard.ai.data.model.ReportType
import com.rainguard.ai.data.model.Result
import com.rainguard.ai.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ReportState(
    val selectedType: ReportType? = null,
    val location: Pair<Double, Double>? = null,
    val photoUri: Uri? = null,
    val description: String = "",
    val contact: String = "",
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReportState())
    val state: StateFlow<ReportState> = _state.asStateFlow()

    fun setType(type: ReportType) {
        _state.value = _state.value.copy(selectedType = type)
    }

    fun setLocation(lat: Double, lng: Double) {
        _state.value = _state.value.copy(location = Pair(lat, lng))
    }

    fun setPhoto(uri: Uri) {
        _state.value = _state.value.copy(photoUri = uri)
    }

    fun setDescription(text: String) {
        _state.value = _state.value.copy(description = text)
    }

    fun setContact(text: String) {
        _state.value = _state.value.copy(contact = text)
    }

    fun submitReport(isImmediateDanger: Boolean = false) {
        val currentState = _state.value

        if (currentState.selectedType == null && !isImmediateDanger) {
            _state.value = currentState.copy(error = "Please select type")
            return
        }

        // Mock location if not set for demo
        val reportLat = currentState.location?.first ?: 28.7041
        val reportLng = currentState.location?.second ?: 77.1025

        viewModelScope.launch {
            _state.value = currentState.copy(isSubmitting = true, error = null)

            val type = if (isImmediateDanger) ReportType.REQUEST_HELP else currentState.selectedType!!
            val desc = if (isImmediateDanger) "IMMEDIATE DANGER REPORTED" else currentState.description

            when (reportRepository.submitReport(
                type = type,
                lat = reportLat,
                lng = reportLng,
                photoUrl = currentState.photoUri?.toString(),
                description = desc,
                contact = currentState.contact.ifEmpty { null }
            )) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        submitSuccess = true
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        error = "Failed to submit report"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun resetSuccess() {
        _state.value = _state.value.copy(submitSuccess = false)
    }
}