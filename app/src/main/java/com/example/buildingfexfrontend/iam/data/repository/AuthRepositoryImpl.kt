package com.example.buildingfexfrontend.iam.data.repository

import com.example.buildingfexfrontend.core.data.network.apiCall
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.model.UserProfile
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.iam.data.remote.AuthApi
import com.example.buildingfexfrontend.iam.data.remote.FinanceSettingsSeed
import com.example.buildingfexfrontend.iam.data.remote.KpiSeed
import com.example.buildingfexfrontend.iam.data.remote.RegisterAdminBody
import com.example.buildingfexfrontend.iam.data.remote.SignInBody
import com.example.buildingfexfrontend.iam.data.remote.UpdateCredentialsBody
import com.example.buildingfexfrontend.iam.data.remote.UpdateProfileBody
import com.example.buildingfexfrontend.iam.domain.repository.AuthRepository
import com.example.buildingfexfrontend.iam.domain.repository.AuthResult

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val session: SessionRepository,
) : AuthRepository {

    override suspend fun isEmailRegistered(email: String): Boolean = apiCall {
        api.checkEmail(email).exists == true
    }

    override suspend fun login(email: String, password: String): AuthResult = apiCall {
        try {
            api.signIn(SignInBody(email, password)).toPair().toAuthResult()
        } catch (e: AppException) {
            throw e
        }
    }

    override suspend fun registerAdmin(
        name: String,
        email: String,
        password: String,
        dni: String,
        address: String,
        company: String,
        ruc: String,
    ): AuthResult = apiCall {
        val (profile, token) = api.registerAdmin(
            RegisterAdminBody(
                name = name,
                email = email,
                password = password,
                dni = dni,
                address = address,
                company = company,
                ruc = ruc,
            ),
        ).toPair()
        seedAdminDefaults(profile.id, token)
        AuthResult(profile, token)
    }

    override suspend fun getProfileById(userId: String): UserProfile? = try {
        apiCall { api.userById(userId).toDomain() }
    } catch (e: AppException) {
        null
    }

    override suspend fun findResidentByEmail(email: String): UserProfile? = apiCall {
        api.users(mapOf("email" to email)).firstOrNull()?.toDomain()
    }

    override suspend fun updateProfile(profile: UserProfile): UserProfile = apiCall {
        api.updateProfile(
            profile.id,
            UpdateProfileBody(
                name = profile.name,
                dni = profile.dni,
                address = profile.address,
                floor = profile.floor,
                email = profile.email,
                company = profile.company,
                ruc = profile.ruc,
            ),
        ).toDomain()
    }

    override suspend fun updateCredentials(
        id: String,
        email: String?,
        password: String?,
    ): UserProfile = apiCall {
        api.updateCredentials(
            id,
            UpdateCredentialsBody(email = email, password = password),
        ).toDomain()
    }

    /** Mirrors the web `seedAdminDefaults` (best effort, never blocks signup). */
    private suspend fun seedAdminDefaults(ownerId: String, token: String) {
        try {
            api.seedFinanceSettings(FinanceSettingsSeed(ownerAdminId = ownerId))
        } catch (e: Exception) {
            // ignore: duplicate or unavailable seed
        }
        try {
            api.seedKpi(KpiSeed(ownerAdminId = ownerId))
        } catch (e: Exception) {
            // ignore
        }
    }
}

private fun Pair<UserProfile, String>.toAuthResult() = AuthResult(first, second)
