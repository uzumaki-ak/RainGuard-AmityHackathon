package com.rainguard.ai.ui.screens.shelter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainguard.ai.data.model.Result
import com.rainguard.ai.data.model.Shelter
import com.rainguard.ai.data.repository.ShelterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShelterDetailsState(
    val shelter: Shelter? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val distance: Double = 0.0
)

@HiltViewModel
class ShelterDetailsViewModel @Inject constructor(
    private val shelterRepository: ShelterRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ShelterDetailsState())
    val state: StateFlow<ShelterDetailsState> = _state.asStateFlow()

    fun loadShelter(shelterId: String, userLat: Double = 28.7041, userLng: Double = 77.1025) {
        viewModelScope.launch {
            when (val result = shelterRepository.getShelterById(shelterId)) {
                is Result.Success -> {
                    val shelter = result.data
                    val distance = calculateDistance(userLat, userLng, shelter.lat, shelter.lng)
                    _state.value = _state.value.copy(
                        shelter = shelter,
                        distance = distance,
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        error = result.exception.message,
                        isLoading = false
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return R * c
    }
}