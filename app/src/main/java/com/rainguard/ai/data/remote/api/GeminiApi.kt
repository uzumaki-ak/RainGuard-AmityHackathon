package com.rainguard.ai.data.remote.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApi {
    @POST("v1beta/models/gemini-2.0-flash-exp:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: Map<String, Any>
    ): Map<String, Any>
}