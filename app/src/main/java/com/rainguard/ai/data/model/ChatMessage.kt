package com.rainguard.ai.data.model

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val sources: List<String> = emptyList(),
    val confidence: Float? = null
)