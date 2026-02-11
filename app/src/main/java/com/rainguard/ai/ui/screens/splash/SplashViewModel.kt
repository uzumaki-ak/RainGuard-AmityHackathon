package com.rainguard.ai.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainguard.ai.data.local.MockDataInitializer
import com.rainguard.ai.data.local.datastore.PreferencesManager
import com.rainguard.ai.data.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed class SplashDestination {
    object Onboarding : SplashDestination()
    object LoginRole : SplashDestination()
    object HomeMap : SplashDestination()
    object AuthorityDashboard : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val mockDataInitializer: MockDataInitializer
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<SplashDestination>()
    val navigationEvent: SharedFlow<SplashDestination> = _navigationEvent.asSharedFlow()

    init {
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch {
            try {
                // Initialize mock data in background
                launch {
                    mockDataInitializer.initializeMockData()
                }

                // Wait minimum splash time
                delay(2000)

                // Determine navigation destination
                val isFirstRun = preferencesManager.isFirstRun.first()
                val userRole = preferencesManager.userRole.first()

                val destination = when {
                    isFirstRun -> SplashDestination.Onboarding
                    userRole == null -> SplashDestination.LoginRole
                    userRole == UserRole.CITIZEN -> SplashDestination.HomeMap
                    userRole == UserRole.AUTHORITY -> SplashDestination.AuthorityDashboard
                    else -> SplashDestination.LoginRole
                }

                _navigationEvent.emit(destination)

            } catch (e: Exception) {
                Timber.e(e, "Error during splash initialization")
                // On error, go to login
                delay(500)
                _navigationEvent.emit(SplashDestination.LoginRole)
            }
        }
    }
}