package com.rainguard.ai.data.repository

import com.rainguard.ai.data.local.database.dao.EmergencyContactDao
import com.rainguard.ai.data.local.database.entities.EmergencyContactEntity
import com.rainguard.ai.data.model.EmergencyContact
import com.rainguard.ai.data.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyContactRepository @Inject constructor(
    private val contactDao: EmergencyContactDao
) {
    fun getContacts(): Flow<List<EmergencyContact>> {
        return contactDao.getAllContacts().map { entities ->
            entities.map { it.toEmergencyContact() }
        }
    }

    suspend fun addContact(name: String, phone: String): Result<Unit> {
        return try {
            val contact = EmergencyContactEntity(
                id = "ec_${System.currentTimeMillis()}",
                name = name,
                phone = phone
            )
            contactDao.insert(contact)
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error adding contact")
            Result.Error(e)
        }
    }

    suspend fun deleteContact(contact: EmergencyContact): Result<Unit> {
        return try {
            val entity = EmergencyContactEntity(contact.id, contact.name, contact.phone)
            contactDao.delete(entity)
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting contact")
            Result.Error(e)
        }
    }
}