package com.rainguard.ai.data.model

enum class UserRole {
    CITIZEN,
    AUTHORITY;

    companion object {
        fun fromString(value: String?): UserRole? {
            return values().firstOrNull { it.name.equals(value, ignoreCase = true) }
        }
    }
}