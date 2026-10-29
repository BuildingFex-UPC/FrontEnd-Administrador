package com.example.buildingfexfrontend.iam.data.remote

import com.example.buildingfexfrontend.core.data.network.apiCall
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.model.UserProfile
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

/** Row returned by the identity endpoints (web `publicUser` mapping). */
data class AuthUserDto(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val role: String? = null,
    val floor: String? = null,
    val code: String? = null,
    val ownerAdminId: String? = null,
    val dni: String? = null,
    val address: String? = null,
    val company: String? = null,
    val ruc: String? = null,
    val admissionDate: String? = null,
) {
    fun toDomain(): UserProfile = UserProfile(
        id = id.orEmpty(),
        name = name.orEmpty(),
        email = email,
        role = role,
        floor = floor,
        code = code,
        ownerAdminId = ownerAdminId,
        dni = dni,
        address = address,
        company = company,
        ruc = ruc,
        admissionDate = admissionDate,
    )
}

data class AuthResponseDto(
    @SerializedName(value = "user", alternate = ["User"])
    val user: AuthUserDto? = null,
    @SerializedName(value = "token", alternate = ["Token"])
    val token: String? = null,
) {
    fun toPair(): Pair<UserProfile, String> {
        val user = user?.toDomain()
            ?: throw AppException("AUTH_ERROR", "Respuesta de autenticación inválida.")
        val jwt = token?.takeIf { it.isNotBlank() }
            ?: throw AppException("AUTH_ERROR", "Respuesta de autenticación inválida.")
        return user to jwt
    }
}

data class SignInBody(val email: String, val password: String)

data class RegisterAdminBody(
    val name: String,
    val email: String,
    val password: String,
    val dni: String,
    val address: String,
    val company: String,
    val ruc: String,
)

data class CheckEmailDto(val exists: Boolean? = null)

data class UpdateProfileBody(
    val name: String,
    val dni: String? = null,
    val address: String? = null,
    val floor: String? = null,
    val email: String? = null,
    val avatar: String? = null,
    val company: String? = null,
    val ruc: String? = null,
)
data class FinanceSettingsSeed(
    val ownerAdminId: String?,
    val baseMonthlyExpense: Double = 150.0,
    val lateFeeRate: Double = 0.05,
)

data class KpiSeed(
    val ownerAdminId: String?,
    val totalResidents: Int = 0,
    val occupiedUnits: Int = 0,
    val emptyUnits: Int = 0,
    val totalDebt: Double = 0.0,
)

/**
 * IAM bounded-context endpoints.
 * Owns authentication, admin registration and seeding.
 */
interface AuthApi {

    @POST("api/v1/authentication/sign-in")
    suspend fun signIn(@Body body: SignInBody): AuthResponseDto

    @POST("api/v1/authentication/register-admin")
    suspend fun registerAdmin(@Body body: RegisterAdminBody): AuthResponseDto

    @GET("api/v1/authentication/check-email")
    suspend fun checkEmail(@Query("email") email: String): CheckEmailDto

    @GET("users/{id}")
    suspend fun userById(@Path("id") id: String): AuthUserDto

    /** `PATCH users/{id}/profile` — editable profile data (both roles). */
    @PATCH("users/{id}/profile")
    suspend fun updateProfile(
        @Path("id") id: String,
        @Body body: UpdateProfileBody,
    ): AuthUserDto

    @GET("users")
    suspend fun users(
        @QueryMap params: Map<String, @JvmSuppressWildcards String>,
    ): List<AuthUserDto>

    @POST("financeSettings")
    suspend fun seedFinanceSettings(@Body body: FinanceSettingsSeed): AuthUserDto

    @POST("kpi")
    suspend fun seedKpi(@Body body: KpiSeed): AuthUserDto

    /** GET /users helper used by the shared `findResidentByCode` flow. */
    suspend fun findUserByQuery(params: Map<String, String>): List<AuthUserDto> = apiCall {
        users(params)
    }
}
