package com.rainguard.ai.data.remote.api

import retrofit2.http.*

interface RainGuardApi {
    // These are mock endpoints - will be replaced with real backend URLs

    @GET("risk-zones")
    suspend fun getRiskZones(): Map<String, Any>

    @GET("shelters")
    suspend fun getShelters(): Map<String, Any>

    @GET("reports")
    suspend fun getReports(): Map<String, Any>

    @GET("alerts")
    suspend fun getAlerts(): Map<String, Any>

    @POST("evacuation-route")
    suspend fun getEvacuationRoute(@Body request: Map<String, Any>): Map<String, Any>

    @POST("citizen-report")
    suspend fun submitReport(@Body report: Map<String, Any>): Map<String, Any>

    @POST("alerts/{id}/ack")
    suspend fun acknowledgeAlert(@Path("id") alertId: String): Map<String, Any>

    @POST("help-requests")
    suspend fun sendHelpRequest(@Body request: Map<String, Any>): Map<String, Any>
}