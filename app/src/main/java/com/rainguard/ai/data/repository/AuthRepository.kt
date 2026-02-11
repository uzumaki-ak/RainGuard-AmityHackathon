package com.rainguard.ai.data.repository

import com.rainguard.ai.data.local.datastore.PreferencesManager
import com.rainguard.ai.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val preferencesManager: PreferencesManager
) {
    val userRole: Flow<UserRole?> = preferencesManager.userRole

    suspend fun setUserRole(role: UserRole, remember: Boolean) {
        preferencesManager.setUserRole(role, remember)
    }

    suspend fun logout() {
        preferencesManager.setUserRole(UserRole.CITIZEN, false)
    }
}