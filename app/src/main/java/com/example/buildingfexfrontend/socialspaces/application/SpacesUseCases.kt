package com.example.buildingfexfrontend.socialspaces.application

import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.socialspaces.domain.model.Reservation
import com.example.buildingfexfrontend.socialspaces.domain.model.Space
import com.example.buildingfexfrontend.socialspaces.domain.repository.ReservationsRepository
import com.example.buildingfexfrontend.socialspaces.domain.repository.SpacesRepository

class SpacesUseCases(
    private val spaces: SpacesRepository,
    private val reservations: ReservationsRepository,
    @Suppress("unused") private val session: SessionRepository,
) {
    val list = ListSpacesUseCase(spaces)
    val add = AddSpaceUseCase(spaces)
    val update = UpdateSpaceUseCase(spaces)
    val remove = RemoveSpaceUseCase(spaces, reservations)
}

class ListSpacesUseCase(private val spaces: SpacesRepository) {
    suspend operator fun invoke(): List<Space> = spaces.list()
}

class AddSpaceUseCase(private val spaces: SpacesRepository) {
    suspend operator fun invoke(
        name: String,
        description: String,
        capacityText: String,
        imageUrl: String,
        cost: Double,
        rules: String = "",
        underMaintenance: Boolean = false,
    ): Space = spaces.add(
        name = name,
        description = description,
        capacity = capacityText.trim().toIntOrNull(),
        imageUrl = imageUrl,
        cost = cost,
        rules = rules,
        underMaintenance = underMaintenance,
    )
}

class UpdateSpaceUseCase(private val spaces: SpacesRepository) {
    suspend operator fun invoke(
        id: String,
        name: String,
        description: String,
        capacityText: String,
        imageUrl: String,
        cost: Double,
        rules: String = "",
        underMaintenance: Boolean = false,
    ): Space = spaces.update(
        id = id,
        name = name,
        description = description,
        capacity = capacityText.trim().toIntOrNull(),
        imageUrl = imageUrl,
        cost = cost,
        rules = rules,
        underMaintenance = underMaintenance,
    )
}

class RemoveSpaceUseCase(
    private val spaces: SpacesRepository,
    private val reservations: ReservationsRepository,
) {
    suspend operator fun invoke(space: Space): Int {
        val id = space.id.orEmpty()
        val removed = reservations.removeBySpace(id)
        spaces.remove(id)
        return removed
    }
}