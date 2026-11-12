package com.example.buildingfexfrontend.socialspaces.data.repository

import com.example.buildingfexfrontend.core.data.network.apiCall
import com.example.buildingfexfrontend.core.data.network.ownerParams
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.socialspaces.data.remote.SpaceDto
import com.example.buildingfexfrontend.socialspaces.data.remote.SpacePatch
import com.example.buildingfexfrontend.socialspaces.data.remote.SpacesApi
import com.example.buildingfexfrontend.socialspaces.domain.model.Space
import com.example.buildingfexfrontend.socialspaces.domain.repository.SpacesRepository

class SpacesRepositoryImpl(
    private val api: SpacesApi,
    private val session: SessionRepository,
) : SpacesRepository {

    override suspend fun list(): List<Space> = apiCall {
        api.list(ownerParams(session)).map { it.toDomain() }
    }

    override suspend fun add(
        name: String,
        description: String,
        capacity: Int?,
        imageUrl: String,
        cost: Double,
        rules: String,
        underMaintenance: Boolean,
    ): Space = apiCall {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) {
            throw AppException("SPACE_NAME_REQUIRED", "El nombre del espacio es obligatorio.")
        }
        val ownerId = session.activeDataOwnerId
        api.create(
            SpaceDto(
                id = "space-${System.currentTimeMillis()}",
                name = cleanName,
                description = description.trim(),
                capacity = capacity,
                imageUrl = imageUrl.trim(),
                ownerAdminId = ownerId,
                cost = cost,
                rules = rules.trim(),
                underMaintenance = underMaintenance,
            ),
        ).toDomain()
    }

    override suspend fun update(
        id: String,
        name: String,
        description: String,
        capacity: Int?,
        imageUrl: String?,
        cost: Double,
        rules: String,
        underMaintenance: Boolean,
    ): Space = apiCall {
        val cleanId = id.trim()
        if (cleanId.isEmpty()) {
            throw AppException("SPACE_NOT_FOUND", "Espacio no encontrado.")
        }
        val cleanName = name.trim()
        if (cleanName.isEmpty()) {
            throw AppException("SPACE_NAME_REQUIRED", "El nombre del espacio es obligatorio.")
        }
        api.patch(
            cleanId,
            SpacePatch(
                name = cleanName,
                description = description.trim(),
                capacity = capacity,
                imageUrl = imageUrl?.trim(),
                cost = cost,
                rules = rules.trim(),
                underMaintenance = underMaintenance,
            ),
        ).toDomain()
    }

    override suspend fun remove(id: String) = apiCall {
        val cleanId = id.trim()
        if (cleanId.isEmpty()) {
            throw AppException("SPACE_NOT_FOUND", "Espacio no encontrado.")
        }
        api.delete(cleanId)
        Unit
    }
}