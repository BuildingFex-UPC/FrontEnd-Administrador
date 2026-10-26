package com.example.buildingfexfrontend.incidents.application

import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.incidents.domain.model.Incident
import com.example.buildingfexfrontend.incidents.domain.model.IncidentInput
import com.example.buildingfexfrontend.incidents.domain.repository.IncidentsRepository

class IncidentsUseCases(
    private val repository: IncidentsRepository,
) {
    val listAll = ListIncidentsUseCase(repository)
    val create = CreateIncidentUseCase(repository)
    val update = UpdateIncidentUseCase(repository)
    val remove = RemoveIncidentUseCase(repository)
}

/** Admin creates incidents directly (no resident attached). */
class CreateIncidentUseCase(private val repository: IncidentsRepository) {
    suspend operator fun invoke(description: String, status: String, provider: String): Incident {
        val clean = description.trim()
        if (clean.isBlank()) {
            throw AppException("INCIDENT_DESCRIPTION_REQUIRED", "Descripción requerida")
        }
        return repository.add(
            IncidentInput(description = clean, status = status, provider = provider.trim()),
        )
    }
}

class ListIncidentsUseCase(private val repository: IncidentsRepository) {
    suspend operator fun invoke(): List<Incident> = repository.list()
}

class UpdateIncidentUseCase(private val repository: IncidentsRepository) {
    suspend operator fun invoke(incident: Incident): Incident {
        if (incident.description.isBlank()) {
            throw AppException("INCIDENT_DESCRIPTION_REQUIRED", "Descripción requerida")
        }
        return repository.update(incident)
    }
}

class RemoveIncidentUseCase(private val repository: IncidentsRepository) {
    suspend operator fun invoke(id: String) = repository.remove(id)
}
