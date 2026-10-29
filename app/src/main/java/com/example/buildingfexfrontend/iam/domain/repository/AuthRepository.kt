package com.example.buildingfexfrontend.iam.domain.repository

import com.example.buildingfexfrontend.core.domain.model.UserProfile

data class AuthResult(val profile: UserProfile, val token: String)

/**
 * IAM domain contract: who the user is and how they prove it.
 */
interface AuthRepository {

    suspend fun isEmailRegistered(email: String): Boolean

    suspend fun login(email: String, password: String): AuthResult

    suspend fun registerAdmin(
        name: String,
        email: String,
        password: String,
        dni: String,
        address: String,
        company: String,
        ruc: String,
    ): AuthResult

    suspend fun getProfileById(userId: String): UserProfile?

    suspend fun findResidentByEmail(email: String): UserProfile?

    /** Updates the editable profile fields; returns the fresh profile. */
    suspend fun updateProfile(profile: UserProfile): UserProfile
}
