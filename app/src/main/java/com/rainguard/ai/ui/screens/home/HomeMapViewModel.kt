package com.rainguard.ai.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainguard.ai.data.model.*
import com.rainguard.ai.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class HomeMapState(
    val riskZones: List<RiskZone> = emptyList(),
    val shelters: List<Shelter> = emptyList(),
    val reports: List<Report> = emptyList(),
    val currentRisk: RiskSeverity = RiskSeverity.LOW,
    val currentConfidence: Float = 0f,
    val nearestShelter: Shelter? = null,
    val lastUpdated: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val userLocation: Pair<Double, Double>? = null
)

@HiltViewModel
class HomeMapViewModel @Inject constructor(
    private val riskZoneRepository: RiskZoneRepository,
    private val shelterRepository: ShelterRepository,
    private val reportRepository: ReportRepository,
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeMapState())
    val state: StateFlow<HomeMapState> = _state.asStateFlow()

    init {
        loadData()
        startPolling()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                riskZoneRepository.getRiskZones(),
                shelterRepository.getShelters(),
                reportRepository.getReports()
            ) { zones, shelters, reports ->
                Triple(zones, shelters, reports)
            }.catch { e ->
                Timber.e(e, "Error loading map data")
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }.collect { (zones, shelters, reports) ->
                val currentZone = zones.maxByOrNull { it.confidence }

                _state.value = _state.value.copy(
                    riskZones = zones,
                    shelters = shelters,
                    reports = reports,
                    currentRisk = currentZone?.severity ?: RiskSeverity.LOW,
                    currentConfidence = currentZone?.confidence ?: 0f,
                    nearestShelter = shelters.firstOrNull(),
                    lastUpdated = currentZone?.updated ?: "",
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    private fun startPolling() {
        // In production, poll every 30 seconds
        // For now, just refresh once
        viewModelScope.launch {
            riskZoneRepository.refreshZones()
        }
    }

    fun setUserLocation(lat: Double, lng: Double) {
        _state.value = _state.value.copy(userLocation = Pair(lat, lng))
        findNearestShelter(lat, lng)
    }

    private fun findNearestShelter(userLat: Double, userLng: Double) {
        val shelters = _state.value.shelters
        if (shelters.isEmpty()) return

        val nearest = shelters.minByOrNull { shelter ->
            calculateDistance(userLat, userLng, shelter.lat, shelter.lng)
        }

        _state.value = _state.value.copy(nearestShelter = nearest)
    }

    fun requestEvacuationRoute(shelterId: String) {
        val userLoc = _state.value.userLocation ?: return

        viewModelScope.launch {
            when (val result = routeRepository.getEvacuationRoute(
                userLoc.first,
                userLoc.second,
                shelterId
            )) {
                is Result.Success -> {
                    // Route will be navigated to by the UI
                    Timber.d("Route created: ${result.data.id}")
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error creating route")
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

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true)
        loadData()
    }
}