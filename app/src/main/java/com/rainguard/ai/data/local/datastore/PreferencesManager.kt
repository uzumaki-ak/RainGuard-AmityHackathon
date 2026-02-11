package com.rainguard.ai.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.rainguard.ai.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rainguard_prefs")

@Singleton
class PreferencesManager @Inject constructor(
    private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val IS_FIRST_RUN = booleanPreferencesKey("is_first_run")
        val DEMO_MODE = booleanPreferencesKey("demo_mode")
        val LOCATION_ALLOWED = booleanPreferencesKey("location_allowed")
        val NOTIFICATIONS_ALLOWED = booleanPreferencesKey("notifications_allowed")
        val USER_ROLE = stringPreferencesKey("user_role")
        val REMEMBER_ROLE = booleanPreferencesKey("remember_role")
        val LANGUAGE = stringPreferencesKey("language")
        val TEXT_SIZE = stringPreferencesKey("text_size")
    }

    val isFirstRun: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[IS_FIRST_RUN] ?: true
    }

    val demoMode: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[DEMO_MODE] ?: false
    }

    val locationAllowed: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[LOCATION_ALLOWED] ?: false
    }

    val userRole: Flow<UserRole?> = dataStore.data.map { prefs ->
        prefs[USER_ROLE]?.let { UserRole.fromString(it) }
    }

    val language: Flow<String> = dataStore.data.map { prefs ->
        prefs[LANGUAGE] ?: "en"
    }

    val textSize: Flow<String> = dataStore.data.map { prefs ->
        prefs[TEXT_SIZE] ?: "default"
    }

    suspend fun setFirstRunComplete() {
        dataStore.edit { prefs ->
            prefs[IS_FIRST_RUN] = false
        }
    }

    suspend fun setDemoMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[DEMO_MODE] = enabled
        }
    }

    suspend fun setLocationAllowed(allowed: Boolean) {
        dataStore.edit { prefs ->
            prefs[LOCATION_ALLOWED] = allowed
        }
    }

    suspend fun setNotificationsAllowed(allowed: Boolean) {
        dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ALLOWED] = allowed
        }
    }

    suspend fun setUserRole(role: UserRole, remember: Boolean) {
        dataStore.edit { prefs ->
            if (remember) {
                prefs[USER_ROLE] = role.name
                prefs[REMEMBER_ROLE] = true
            } else {
                prefs.remove(USER_ROLE)
                prefs[REMEMBER_ROLE] = false
            }
        }
    }

    suspend fun setLanguage(lang: String) {
        dataStore.edit { prefs ->
            prefs[LANGUAGE] = lang
        }
    }

    suspend fun setTextSize(size: String) {
        dataStore.edit { prefs ->
            prefs[TEXT_SIZE] = size
        }
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}