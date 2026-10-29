package com.example.buildingfexfrontend.residents.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.i18n.stringOf
import com.example.buildingfexfrontend.residents.application.ResidentsUseCases
import com.example.buildingfexfrontend.residents.domain.DepartmentNumber
import com.example.buildingfexfrontend.residents.domain.model.LinkedDataPreview
import com.example.buildingfexfrontend.residents.domain.model.Resident
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResidentsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val residents: List<Resident> = emptyList(),
    val query: String = "",
    val planUsage: Pair<Int, Int>? = null,
    val showAddDialog: Boolean = false,
    val addName: String = "",
    val addDepartment: String = "",
    val addFloorHint: String? = null,
    val adding: Boolean = false,
    val addError: String? = null,
    val deleteTarget: Resident? = null,
    val deletePreview: LinkedDataPreview? = null,
    val deleteLoading: Boolean = false,
    val deleting: Boolean = false,
    val message: String? = null,
) {
    val filtered: List<Resident>
        get() = residents.filter {
            query.isBlank() ||
                it.name.contains(query, true) ||
                it.code.contains(query, true) ||
                it.floor.contains(query, true)
        }
}

class ResidentsViewModel(private val useCases: ResidentsUseCases) : ViewModel() {

    private val _state = MutableStateFlow(ResidentsUiState())
    val state: StateFlow<ResidentsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val residents = useCases.list()
                val usage = runCatching { useCases.planUsage() }.getOrNull()
                _state.update { it.copy(loading = false, residents = residents, planUsage = usage) }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(loading = false, error = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun onQueryChange(value: String) = _state.update { it.copy(query = value) }

    fun openAddDialog() = _state.update {
        it.copy(
            showAddDialog = true,
            addName = "",
            addDepartment = "",
            addFloorHint = null,
            addError = null,
        )
    }

    fun closeAddDialog() = _state.update { it.copy(showAddDialog = false, addError = null) }

    fun onAddNameChange(value: String) =
        _state.update { it.copy(addName = value, addError = null) }

    fun onAddDepartmentChange(value: String) = _state.update {
        val floor = DepartmentNumber.parse(value)?.second
        it.copy(addDepartment = value, addFloorHint = floor, addError = null)
    }

    fun confirmAdd() {
        val current = _state.value
        _state.update { it.copy(adding = true, addError = null) }
        viewModelScope.launch {
            try {
                useCases.add(current.addDepartment, current.addName)
                val residents = runCatching { useCases.list() }.getOrDefault(current.residents)
                val usage = runCatching { useCases.planUsage() }.getOrNull()
                _state.update {
                    it.copy(
                        adding = false,
                        showAddDialog = false,
                        residents = residents,
                        planUsage = usage,
                        message = stringOf("res.msg.added"),
                    )
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(adding = false, addError = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun openDeleteDialog(resident: Resident) {
        _state.update { it.copy(deleteTarget = resident, deletePreview = null, deleteLoading = true) }
        viewModelScope.launch {
            val preview = try {
                useCases.previewDelete(resident.id)
            } catch (e: Throwable) {
                LinkedDataPreview()
            }
            _state.update { it.copy(deleteLoading = false, deletePreview = preview) }
        }
    }

    fun closeDeleteDialog() =
        _state.update { it.copy(deleteTarget = null, deletePreview = null) }

    fun confirmDelete() {
        val target = _state.value.deleteTarget ?: return
        _state.update { it.copy(deleting = true) }
        viewModelScope.launch {
            try {
                val result = useCases.removeCascade(target.id)
                val residents = runCatching { useCases.list() }.getOrDefault(emptyList())
                _state.update {
                    it.copy(
                        deleting = false,
                        deleteTarget = null,
                        deletePreview = null,
                        residents = residents,
                        message = stringOf("res.msg.removed")
                            .replace("{count}", result.reservationsRemoved.toString()),
                    )
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(deleting = false, deleteTarget = null, message = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun dismissMessage() = _state.update { it.copy(message = null) }

    /** Confirms that the invite code was copied (only the code, verbatim). */
    fun notifyCodeCopied() =
        _state.update { it.copy(message = stringOf("res.msg.codeCopied")) }
}
