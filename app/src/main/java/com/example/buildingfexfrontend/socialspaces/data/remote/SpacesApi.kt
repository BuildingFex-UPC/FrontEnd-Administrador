package com.example.buildingfexfrontend.socialspaces.data.remote

import com.example.buildingfexfrontend.socialspaces.domain.model.Space
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

data class SpaceDto(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val capacity: Int? = null,
    val imageUrl: String? = null,
    val ownerAdminId: String? = null,
    val cost: Double? = null,
    val rules: String? = null,
    val underMaintenance: Boolean? = null,
) {
    fun toDomain() = Space(
        id = id,
        name = name.orEmpty(),
        description = description.orEmpty(),
        capacity = capacity,
        imageUrl = imageUrl.orEmpty(),
        cost = cost ?: 0.0,
        rules = rules.orEmpty(),
        underMaintenance = underMaintenance ?: false,
    )
}

data class SpacePatch(
    val name: String? = null,
    val description: String? = null,
    val capacity: Int? = null,
    val imageUrl: String? = null,
    val cost: Double? = null,
    val rules: String? = null,
    val underMaintenance: Boolean? = null,
)

interface SpacesApi {

    @GET("socialSpaces")
    suspend fun list(@QueryMap params: Map<String, @JvmSuppressWildcards String>): List<SpaceDto>

    @POST("socialSpaces")
    suspend fun create(@Body body: SpaceDto): SpaceDto

    @PATCH("socialSpaces/{id}")
    suspend fun patch(
        @Path("id") id: String,
        @Body body: SpacePatch,
    ): SpaceDto

    @DELETE("socialSpaces/{id}")
    suspend fun delete(@Path("id") id: String)
}