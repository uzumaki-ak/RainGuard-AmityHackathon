package com.rainguard.ai.ui.screens.authority

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.rainguard.ai.data.model.Report
import com.rainguard.ai.data.model.Route
import com.rainguard.ai.data.model.Shelter
import com.rainguard.ai.data.repository.ReportRepository
import com.rainguard.ai.data.repository.ShelterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TimeFilter { DAILY, WEEKLY, MONTHLY }

data class SafeSignal(
    val id: String = "",
    val userName: String = "",
    val status: String = "",
    val timestamp: String = ""
)

data class AuthorityDashboardState(
    val isLoading: Boolean = false,
    val totalReports: Int = 0,
    val pendingReports: Int = 0,
    val approvedReports: Int = 0,
    val activeAlerts: Int = 0,
    val reports: List<Report> = emptyList(),
    val allReports: List<Report> = emptyList(),
    val shelters: List<Shelter> = emptyList(),
    val safeSignals: List<SafeSignal> = emptyList(),
    val riskTrends: List<Float> = listOf(0.2f, 0.4f, 0.3f, 0.7f, 0.9f, 0.8f, 0.6f, 0.9f),
    val selectedFilter: TimeFilter = TimeFilter.DAILY,
    val lastSyncTime: String = "Just now",
    val suggestedRoute: Route? = null
)

@HiltViewModel
class AuthorityDashboardViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val shelterRepository: ShelterRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AuthorityDashboardState())
    val state: StateFlow<AuthorityDashboardState> = _state.asStateFlow()
    
    private val firestore = FirebaseFirestore.getInstance()

    init {
        observeReports()
        observeShelters()
        observeSafeSignals()
        loadSuggestedRoute()
    }

    private fun observeReports() {
        viewModelScope.launch {
            reportRepository.getReports().collect { allReports ->
                val pending = allReports.filter { !it.verified }
                val approved = allReports.filter { it.verified }
                
                _state.update { it.copy(
                    reports = pending,
                    allReports = allReports,
                    totalReports = allReports.size,
                    pendingReports = pending.size,
                    approvedReports = approved.size,
                    activeAlerts = pending.size
                ) }
            }
        }
    }

    private fun observeShelters() {
        viewModelScope.launch {
            shelterRepository.getShelters().collect { shelters ->
                _state.update { it.copy(shelters = shelters) }
            }
        }
    }
    
    private fun observeSafeSignals() {
        // LISTEN LIVE FOR "I'M SAFE" BUTTON CLICKS
        viewModelScope.launch {
            firestore.collection("safe_signals")
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        try {
                            SafeSignal(
                                id = doc.id,
                                userName = doc.getString("userName") ?: "Citizen",
                                status = doc.getString("status") ?: "SAFE",
                                timestamp = doc.getString("timestamp") ?: ""
                            )
                        } catch (e: Exception) { null }
                    }
                }
                .collect { signals ->
                    _state.update { it.copy(safeSignals = signals.sortedByDescending { it.timestamp }) }
                }
        }
    }
    
    private fun loadSuggestedRoute() {
        _state.update {
            it.copy(
                suggestedRoute = Route(
                    id = "route-approve-1",
                    shelterId = "s2",
                    shelterName = "Community Hall",
                    path = emptyList(),
                    etaMinutes = 25,
                    distanceMeters = 3400,
                    confidence = 0.82f,
                    segments = emptyList(),
                    sources = listOf("satellite", "citizen-report-45"),
                    rationale = listOf("Avoids flooded main road", "Uses elevated highway")
                )
            )
        }
    }

    fun setFilter(filter: TimeFilter) {
        _state.update { it.copy(selectedFilter = filter) }
    }

    fun approveReport(reportId: String) {
        viewModelScope.launch {
            reportRepository.verifyReport(reportId)
        }
    }

    fun deleteReport(reportId: String) {
        viewModelScope.launch {
            reportRepository.deleteReport(reportId)
        }
    }
    
    fun approveRoute(routeId: String) {
        _state.update { it.copy(suggestedRoute = null) }
    }

    fun rejectRoute(routeId: String) {
        _state.update { it.copy(suggestedRoute = null) }
    }
    
    fun updateShelter(shelter: Shelter) {
        viewModelScope.launch {
            shelterRepository.updateShelter(shelter)
        }
    }
}
