package com.rainguard.ai.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rainguard.ai.data.local.database.dao.RouteDao
import com.rainguard.ai.data.local.database.entities.RouteEntity
import com.rainguard.ai.data.model.Result
import com.rainguard.ai.data.model.Route
import com.rainguard.ai.data.model.RouteSegment
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val routeDao: RouteDao,
    private val gson: Gson
) {
    suspend fun getEvacuationRoute(
        fromLat: Double,
        fromLng: Double,
        shelterId: String
    ): Result<Route> {
        return try {
            // In production, call API
            // For now, load from mock data
            val json = context.assets.open("mock/routes.json").bufferedReader().use { it.readText() }
            val data = gson.fromJson<Map<String, Any>>(json, object : TypeToken<Map<String, Any>>() {}.type)

            @Suppress("UNCHECKED_CAST")
            val routes = data["routes"] as? List<Map<String, Any>>
            val routeData = routes?.firstOrNull()

            if (routeData != null) {
                val route = parseRoute(routeData)

                // Cache the route
                val entity = RouteEntity(
                    id = route.id,
                    shelterId = route.shelterId,
                    shelterName = route.shelterName,
                    pathJson = gson.toJson(route.path),
                    etaMinutes = route.etaMinutes,
                    distanceMeters = route.distanceMeters,
                    confidence = route.confidence,
                    segmentsJson = gson.toJson(route.segments),
                    sourcesJson = gson.toJson(route.sources),
                    rationaleJson = gson.toJson(route.rationale)
                )
                routeDao.insert(entity)

                Result.Success(route)
            } else {
                Result.Error(Exception("No route found"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting evacuation route")
            Result.Error(e)
        }
    }

    suspend fun getRouteById(routeId: String): Result<Route> {
        return try {
            val entity = routeDao.getRouteById(routeId)
            if (entity != null) {
                val path = gson.fromJson<List<List<Double>>>(
                    entity.pathJson,
                    object : TypeToken<List<List<Double>>>() {}.type
                )
                val segments = gson.fromJson<List<RouteSegment>>(
                    entity.segmentsJson,
                    object : TypeToken<List<RouteSegment>>() {}.type
                )
                val sources = gson.fromJson<List<String>>(
                    entity.sourcesJson,
                    object : TypeToken<List<String>>() {}.type
                )
                val rationale = gson.fromJson<List<String>>(
                    entity.rationaleJson,
                    object : TypeToken<List<String>>() {}.type
                )

                val route = Route(
                    id = entity.id,
                    shelterId = entity.shelterId,
                    shelterName = entity.shelterName,
                    path = path,
                    etaMinutes = entity.etaMinutes,
                    distanceMeters = entity.distanceMeters,
                    confidence = entity.confidence,
                    segments = segments,
                    sources = sources,
                    rationale = rationale
                )
                Result.Success(route)
            } else {
                Result.Error(Exception("Route not found"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting route by id")
            Result.Error(e)
        }
    }

    private fun parseRoute(data: Map<String, Any>): Route {
        @Suppress("UNCHECKED_CAST")
        return Route(
            id = data["id"] as String,
            shelterId = data["shelterId"] as String,
            shelterName = data["shelterName"] as String,
            path = data["path"] as List<List<Double>>,
            etaMinutes = (data["etaMinutes"] as Double).toInt(),
            distanceMeters = (data["distanceMeters"] as Double).toInt(),
            confidence = (data["confidence"] as Double).toFloat(),
            segments = parseSegments(data["segments"] as List<Map<String, Any>>),
            sources = data["sources"] as List<String>,
            rationale = data["rationale"] as List<String>
        )
    }

    private fun parseSegments(data: List<Map<String, Any>>): List<RouteSegment> {
        return data.map { seg ->
            @Suppress("UNCHECKED_CAST")
            RouteSegment(
                from = seg["from"] as String,
                to = seg["to"] as String,
                distanceMeters = (seg["distanceMeters"] as Double).toInt(),
                etaSeconds = (seg["etaSeconds"] as Double).toInt(),
                hazards = seg["hazards"] as List<String>
            )
        }
    }
}