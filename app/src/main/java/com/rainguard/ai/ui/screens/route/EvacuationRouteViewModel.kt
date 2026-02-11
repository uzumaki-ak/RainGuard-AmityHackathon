package com.rainguard.ai.ui.screens.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainguard.ai.data.model.Result
import com.rainguard.ai.data.model.Route
import com.rainguard.ai.data.repository.RouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class EvacuationRouteState(
    val route: Route? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val lowConfidenceWarning: Boolean = false
)

@HiltViewModel
class EvacuationRouteViewModel @Inject constructor(
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EvacuationRouteState())
    val state: StateFlow<EvacuationRouteState> = _state.asStateFlow()

    fun loadRoute(routeId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            when (val result = routeRepository.getRouteById(routeId)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        route = result.data,
                        isLoading = false,
                        lowConfidenceWarning = result.data.confidence < 0.5f
                    )
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error loading route")
                    _state.value = _state.value.copy(
                        error = result.exception.message,
                        isLoading = false
                    )
                }
                is Result.Loading -> {}
            }
        }
    }
}