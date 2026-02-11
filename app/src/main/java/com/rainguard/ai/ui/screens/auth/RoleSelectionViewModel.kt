package com.rainguard.ai.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainguard.ai.data.model.UserRole
import com.rainguard.ai.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoleSelectionState(
    val selectedRole: UserRole? = null,
    val rememberRole: Boolean = false,
    val canContinue: Boolean = false
)

@HiltViewModel
class RoleSelectionViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RoleSelectionState())
    val state: StateFlow<RoleSelectionState> = _state.asStateFlow()

    fun selectRole(role: UserRole) {
        _state.value = _state.value.copy(
            selectedRole = role,
            canContinue = true
        )
    }

    fun setRememberRole(remember: Boolean) {
        _state.value = _state.value.copy(rememberRole = remember)
    }

    fun confirmRole() {
        viewModelScope.launch {
            val role = _state.value.selectedRole ?: return@launch
            authRepository.setUserRole(role, _state.value.rememberRole)
        }
    }
}