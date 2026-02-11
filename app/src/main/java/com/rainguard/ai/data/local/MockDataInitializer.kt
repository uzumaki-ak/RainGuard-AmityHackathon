package com.rainguard.ai.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rainguard.ai.data.local.database.RainGuardDatabase
import com.rainguard.ai.data.local.database.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class MockDataInitializer @Inject constructor(
    private val context: Context,
    private val database: RainGuardDatabase,
    private val gson: Gson
) {
    suspend fun initializeMockData() = withContext(Dispatchers.IO) {
        try {
            // Load and insert risk zones
            loadRiskZones()

            // Load and insert shelters
            loadShelters()

            // Load and insert reports
            loadReports()

            // Load and insert alerts
            loadAlerts()

            Timber.d("Mock data initialized successfully")
        } catch (e: Exception) {
            Timber.e(e, "Error initializing mock data")
        }
    }

    private suspend fun loadRiskZones() {
        val json = context.assets.open("mock/risk_zones.json").bufferedReader().use { it.readText() }
        val data = gson.fromJson<Map<String, Any>>(json, object : TypeToken<Map<String, Any>>() {}.type)

        @Suppress("UNCHECKED_CAST")
        val zones = (data["zones"] as? List<Map<String, Any>>)?.map { zone ->
            val geojson = zone["geojson"] as? Map<String, Any>
            val geometry = geojson?.get("geometry") as? Map<String, Any>
            val coordinates = geometry?.get("coordinates") as? List<List<List<Double>>>

            RiskZoneEntity(
                id = zone["id"] as String,
                name = zone["name"] as String,
                severity = zone["severity"] as String,
                confidence = (zone["confidence"] as Double).toFloat(),
                coordinatesJson = gson.toJson(coordinates?.firstOrNull() ?: emptyList<List<Double>>()),
                sourcesJson = gson.toJson(zone["sources"]),
                updated = zone["updated"] as String,
                reason = zone["reason"] as String
            )
        } ?: emptyList()

        database.riskZoneDao().insertAll(zones)
    }

    private suspend fun loadShelters() {
        val json = context.assets.open("mock/shelters.json").bufferedReader().use { it.readText() }
        val data = gson.fromJson<Map<String, Any>>(json, object : TypeToken<Map<String, Any>>() {}.type)

        @Suppress("UNCHECKED_CAST")
        val shelters = (data["shelters"] as? List<Map<String, Any>>)?.map { shelter ->
            val supplies = shelter["supplies"] as? Map<String, Boolean> ?: emptyMap()

            ShelterEntity(
                id = shelter["id"] as String,
                name = shelter["name"] as String,
                lat = shelter["lat"] as Double,
                lng = shelter["lng"] as Double,
                capacity = (shelter["capacity"] as Double).toInt(),
                available = (shelter["available"] as Double).toInt(),
                address = shelter["address"] as String,
                phone = shelter["phone"] as String,
                open = shelter["open"] as Boolean,
                wheelchairAccessible = shelter["wheelchairAccessible"] as Boolean,
                hasFood = supplies["food"] ?: false,
                hasWater = supplies["water"] ?: false,
                hasMedical = supplies["medical"] ?: false,
                hasBlankets = supplies["blankets"] ?: false
            )
        } ?: emptyList()

        database.shelterDao().insertAll(shelters)
    }

    private suspend fun loadReports() {
        val json = context.assets.open("mock/reports.json").bufferedReader().use { it.readText() }
        val data = gson.fromJson<Map<String, Any>>(json, object : TypeToken<Map<String, Any>>() {}.type)

        @Suppress("UNCHECKED_CAST")
        val reports = (data["reports"] as? List<Map<String, Any>>)?.map { report ->
            ReportEntity(
                id = report["id"] as String,
                type = report["type"] as String,
                lat = report["lat"] as Double,
                lng = report["lng"] as Double,
                photoUrl = report["photoUrl"] as? String,
                description = report["description"] as String,
                timestamp = report["timestamp"] as String,
                verified = report["verified"] as Boolean,
                reporterContact = report["reporterContact"] as? String,
                uploaded = true
            )
        } ?: emptyList()

        database.reportDao().run {
            reports.forEach { insert(it) }
        }
    }

    private suspend fun loadAlerts() {
        val json = context.assets.open("mock/alerts.json").bufferedReader().use { it.readText() }
        val data = gson.fromJson<Map<String, Any>>(json, object : TypeToken<Map<String, Any>>() {}.type)

        @Suppress("UNCHECKED_CAST")
        val alerts = (data["alerts"] as? List<Map<String, Any>>)?.map { alert ->
            AlertEntity(
                id = alert["id"] as String,
                title = alert["title"] as String,
                message = alert["message"] as String,
                severity = alert["severity"] as String,
                confidence = (alert["confidence"] as Double).toFloat(),
                zoneId = alert["zoneId"] as String,
                timestamp = alert["timestamp"] as String,
                expiresAt = alert["expiresAt"] as String,
                mapThumbnail = alert["mapThumbnail"] as String,
                actionsJson = gson.toJson(alert["recommendedActions"]),
                acknowledged = false
            )
        } ?: emptyList()

        database.alertDao().insertAll(alerts)
    }
}