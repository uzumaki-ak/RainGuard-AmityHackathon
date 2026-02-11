package com.rainguard.ai.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rainguard.ai.BuildConfig
import com.rainguard.ai.data.model.ChatMessage
import com.rainguard.ai.data.model.Result
import com.rainguard.ai.data.remote.api.GeminiApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geminiApi: GeminiApi,
    private val gson: Gson
) {
    private val templates: List<Map<String, Any>> by lazy {
        loadTemplates()
    }

    private fun loadTemplates(): List<Map<String, Any>> {
        return try {
            val json = context.assets.open("mock/kb_templates.json").bufferedReader().use { it.readText() }
            val data = gson.fromJson<Map<String, Any>>(json, object : TypeToken<Map<String, Any>>() {}.type)
            @Suppress("UNCHECKED_CAST")
            data["templates"] as? List<Map<String, Any>> ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Error loading templates")
            emptyList()
        }
    }

    suspend fun sendMessage(query: String, context: String = ""): Result<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            // First try local KB matching
            val localResponse = matchLocalTemplate(query)
            if (localResponse != null) {
                return@withContext Result.Success(localResponse)
            }

            // If no match, use Gemini API
            val geminiResponse = callGeminiApi(query, context)
            Result.Success(geminiResponse)
        } catch (e: Exception) {
            Timber.e(e, "Error sending message")
            Result.Error(e)
        }
    }

    private fun matchLocalTemplate(query: String): ChatMessage? {
        val queryLower = query.lowercase()

        for (template in templates) {
            @Suppress("UNCHECKED_CAST")
            val keywords = template["keywords"] as? List<String> ?: continue

            if (keywords.any { queryLower.contains(it.lowercase()) }) {
                return ChatMessage(
                    text = template["response"] as String,
                    isUser = false,
                    sources = template["sources"] as? List<String> ?: emptyList(),
                    confidence = (template["confidence"] as? Double)?.toFloat()
                )
            }
        }

        // Return default template if no match
        val defaultTemplate = templates.find { it["id"] == "default" }
        return defaultTemplate?.let {
            ChatMessage(
                text = it["response"] as String,
                isUser = false,
                sources = it["sources"] as? List<String> ?: emptyList(),
                confidence = (it["confidence"] as? Double)?.toFloat()
            )
        }
    }

    private suspend fun callGeminiApi(query: String, contextInfo: String): ChatMessage {
        return try {
            val request = mapOf(
                "contents" to listOf(
                    mapOf(
                        "parts" to listOf(
                            mapOf(
                                "text" to buildPrompt(query, contextInfo)
                            )
                        )
                    )
                )
            )

            val response = geminiApi.generateContent(
                apiKey = BuildConfig.GEMINI_API_KEY,
                request = request
            )

            @Suppress("UNCHECKED_CAST")
            val candidates = response["candidates"] as? List<Map<String, Any>>
            val content = candidates?.firstOrNull()?.get("content") as? Map<String, Any>
            val parts = content?.get("parts") as? List<Map<String, Any>>
            val text = parts?.firstOrNull()?.get("text") as? String ?: "I'm here to help with flood safety information."

            ChatMessage(
                text = text,
                isUser = false,
                sources = listOf("gemini-ai"),
                confidence = 0.85f
            )
        } catch (e: Exception) {
            Timber.e(e, "Gemini API call failed, using fallback")
            ChatMessage(
                text = "I can help with: finding shelters, evacuation routes, flood preparation tips. For emergencies, call 112 immediately.",
                isUser = false,
                sources = listOf("system"),
                confidence = 0.5f
            )
        }
    }

    private fun buildPrompt(query: String, contextInfo: String): String {
        return """
You are RainGuardAI Assistant, an AI helping people during flood emergencies.

Context: $contextInfo

User Question: $query

Provide a concise, actionable response (max 40 words). Focus on:
- Immediate safety
- Evacuation guidance
- Shelter locations
- Flood preparation

If the question is about medical/legal advice, direct them to professionals.
For life-threatening situations, tell them to call 112 immediately.
        """.trimIndent()
    }
}