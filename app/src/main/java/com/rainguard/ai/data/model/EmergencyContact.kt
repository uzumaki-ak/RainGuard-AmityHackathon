package com.rainguard.ai.data.model

data class EmergencyContact(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val phone: String
)