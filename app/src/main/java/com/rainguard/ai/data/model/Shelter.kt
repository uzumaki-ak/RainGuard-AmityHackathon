package com.rainguard.ai.data.model

data class Shelter(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val capacity: Int,
    val available: Int,
    val address: String,
    val phone: String,
    val open: Boolean,
    val wheelchairAccessible: Boolean,
    val supplies: ShelterSupplies
)

data class ShelterSupplies(
    val food: Boolean = false,
    val water: Boolean = false,
    val medical: Boolean = false,
    val blankets: Boolean = false
)