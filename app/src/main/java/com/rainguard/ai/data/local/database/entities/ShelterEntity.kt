package com.rainguard.ai.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rainguard.ai.data.model.Shelter
import com.rainguard.ai.data.model.ShelterSupplies

@Entity(tableName = "shelters")
data class ShelterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val capacity: Int,
    val available: Int,
    val address: String,
    val phone: String,
    val open: Boolean,
    val wheelchairAccessible: Boolean,
    val hasFood: Boolean,
    val hasWater: Boolean,
    val hasMedical: Boolean,
    val hasBlankets: Boolean
) {
    fun toShelter(): Shelter {
        return Shelter(
            id = id,
            name = name,
            lat = lat,
            lng = lng,
            capacity = capacity,
            available = available,
            address = address,
            phone = phone,
            open = open,
            wheelchairAccessible = wheelchairAccessible,
            supplies = ShelterSupplies(hasFood, hasWater, hasMedical, hasBlankets)
        )
    }
}