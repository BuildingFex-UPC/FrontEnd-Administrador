package com.example.buildingfexfrontend.incidents.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.i18n.stringOf
import com.example.buildingfexfrontend.incidents.application.IncidentsUseCases
import com.example.buildingfexfrontend.incidents.domain.model.Incident
import com.example.buildingfexfrontend.incidents.domain.model.IncidentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminIncidentsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val incidents: List<Incident> = emptyList(),
    val editorVisible: Boolean = false,
    val editingId: String? = null,
    val formDescription: String = "",
    val formStatus: String = IncidentStatus.OPEN,
    val formProvider: String = "",
    val saving: Boolean = false,
    val formError: String? = null,
    val deleteTarget: Incident? = null,
    val deleting: Boolean = false,
    val message: String? = null,
)

class AdminIncidentsViewModel(private val useCases: IncidentsUseCases) : ViewModel() {

    private val _state = MutableStateFlow(AdminIncidentsUiState())
    val state: StateFlow<AdminIncidentsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val items = useCases.listAll()
                _state.update { it.copy(loading = false, incidents = items) }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(loading = false, error = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun openCreate() = _state.update {
        it.copy(
            editorVisible = true,
            editingId = null,
            formDescription = "",
            formStatus = IncidentStatus.OPEN,
            formProvider = "",
            formError = null,
        )
    }

    fun openEdit(incident: Incident) = _state.update {
        it.copy(
            editorVisible = true,
            editingId = incident.id,
            formDescription = incident.description,
            formStatus = incident.status,
            formProvider = incident.provider,
            formError = null,
        )
    }

    fun closeEditor() = _state.update { it.copy(editorVisible = false, formError = null) }

    fun onDescriptionChange(value: String) =
        _state.update { it.copy(formDescription = value, formError = null) }

    fun onStatusChange(value: String) = _state.update { it.copy(formStatus = value) }

    fun onProviderChange(value: String) = _state.update { it.copy(formProvider = value) }

    fun save() {
        val current = _state.value
        _state.update { it.copy(saving = true, formError = null) }
        viewModelScope.launch {
            try {
                val editingId = current.editingId
                if (editingId == null) {
                    useCases.create(
                        description = current.formDescription,
                        status = current.formStatus,
                        provider = current.formProvider,
                    )
                } else {
                    val existing = current.incidents.firstOrNull { it.id == editingId }
                    if (existing != null) {
                        useCases.update(
                            existing.copy(
                                description = current.formDescription.trim(),
                                status = current.formStatus,
                                provider = current.formProvider.trim(),
                            ),
                        )
                    }
                }
                refreshAfterSave(stringOf("inc.msg.saved"))
            } catch (e: Throwable) {
                _state.update {
                    it.copy(saving = false, formError = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun confirmDelete() {
        val target = _state.value.deleteTarget ?: return
        _state.update { it.copy(deleting = true) }
        viewModelScope.launch {
            try {
                useCases.remove(target.id)
                refreshAfterSave(stringOf("inc.msg.deleted"))
            } catch (e: Throwable) {
                _state.update {
                    it.copy(deleting = false, deleteTarget = null, message = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun openDelete(incident: Incident) = _state.update { it.copy(deleteTarget = incident) }

    fun closeDelete() = _state.update { it.copy(deleteTarget = null) }

    fun dismissMessage() = _state.update { it.copy(message = null) }

    private suspend fun refreshAfterSave(message: String) {
        val items = runCatching { useCases.listAll() }.getOrDefault(emptyList())
        _state.update {
            it.copy(
                saving = false,
                deleting = false,
                editorVisible = false,
                deleteTarget = null,
                incidents = items,
                message = message,
            )
        }
    }
}
