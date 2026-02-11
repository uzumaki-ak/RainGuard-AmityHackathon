package com.rainguard.ai.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rainguard.ai.data.model.EmergencyContact

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String
) {
    fun toEmergencyContact(): EmergencyContact {
        return EmergencyContact(id, name, phone)
    }
}