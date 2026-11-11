package com.example.buildingfexfrontend.socialspaces.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.i18n.stringOf
import com.example.buildingfexfrontend.socialspaces.application.SpacesUseCases
import com.example.buildingfexfrontend.socialspaces.domain.model.Space
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SpaceDialog { NONE, ADD, EDIT }

data class SpacesUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val spaces: List<Space> = emptyList(),
    val dialog: SpaceDialog = SpaceDialog.NONE,
    val editId: String? = null,
    val name: String = "",
    val description: String = "",
    val capacity: String = "",
    val imageUrl: String = "",
    val cost: String = "0.00",
    val photoProcessing: Boolean = false,
    val busy: Boolean = false,
    val deleteTarget: Space? = null,
    val message: String? = null,
    val formError: String? = null,
)

class SpacesViewModel(private val useCases: SpacesUseCases) : ViewModel() {

    private val _state = MutableStateFlow(SpacesUiState())
    val state: StateFlow<SpacesUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val spaces = useCases.list()
                _state.update { it.copy(loading = false, spaces = spaces) }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(loading = false, error = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun openAdd() = _state.update {
        it.copy(
            dialog = SpaceDialog.ADD,
            editId = null,
            name = "",
            description = "",
            capacity = "",
            imageUrl = "",
            cost = "0.00",
            photoProcessing = false,
            formError = null,
            message = null,
        )
    }

    fun openEdit(space: Space) = _state.update {
        it.copy(
            dialog = SpaceDialog.EDIT,
            editId = space.id,
            name = space.name,
            description = space.description,
            capacity = space.capacity?.toString().orEmpty(),
            imageUrl = space.imageUrl,
            cost = space.cost.toString(),
            photoProcessing = false,
            formError = null,
            message = null,
        )
    }

    fun closeDialog() = _state.update {
        it.copy(dialog = SpaceDialog.NONE, photoProcessing = false, formError = null)
    }

    fun onFieldChange(field: String, value: String) = _state.update {
        when (field) {
            "name" -> it.copy(name = value, formError = null)
            "description" -> it.copy(description = value, formError = null)
            "capacity" -> it.copy(capacity = value, formError = null)
            "cost" -> it.copy(cost = value, formError = null)
            else -> it
        }
    }

    fun onPhotoProcessing(value: Boolean) = _state.update { it.copy(photoProcessing = value) }

    fun setPhoto(dataUrl: String) = _state.update {
        it.copy(imageUrl = dataUrl, photoProcessing = false, formError = null)
    }

    fun clearPhoto() = _state.update {
        it.copy(imageUrl = "", photoProcessing = false, formError = null)
    }

    fun photoProcessingFailed(message: String) = _state.update {
        it.copy(photoProcessing = false, formError = message)
    }

    fun submit() {
        val s = _state.value
        val parsedCost = s.cost.trim().replace(",", ".").toDoubleOrNull()

        if (parsedCost == null || !parsedCost.isFinite() || parsedCost < 0) {
            _state.update {
                it.copy(formError = "Ingresa un costo válido mayor o igual a cero.")
            }
            return
        }

        _state.update { it.copy(busy = true, message = null, formError = null) }

        viewModelScope.launch {
            try {
                if (s.dialog == SpaceDialog.EDIT && s.editId != null) {
                    useCases.update(
                        s.editId,
                        s.name,
                        s.description,
                        s.capacity,
                        s.imageUrl,
                        parsedCost,
                    )
                } else {
                    useCases.add(
                        s.name,
                        s.description,
                        s.capacity,
                        s.imageUrl,
                        parsedCost,
                    )
                }

                val spaces = useCases.list()
                _state.update {
                    it.copy(
                        busy = false,
                        dialog = SpaceDialog.NONE,
                        spaces = spaces,
                        message = stringOf("spaces.savedMessage"),
                    )
                }
            } catch (e: Throwable) {
                val error = AppException.unexpected(e).userMessage()
                _state.update {
                    if (it.dialog != SpaceDialog.NONE) it.copy(busy = false, formError = error)
                    else it.copy(busy = false, message = error)
                }
            }
        }
    }

    fun requestDelete(space: Space) = _state.update { it.copy(deleteTarget = space) }

    fun cancelDelete() = _state.update { it.copy(deleteTarget = null) }

    fun confirmDelete() {
        val target = _state.value.deleteTarget ?: return
        _state.update { it.copy(busy = true, deleteTarget = null, message = null) }
        viewModelScope.launch {
            try {
                val removed = useCases.remove(target)
                val spaces = useCases.list()
                val deletedMessage = if (removed > 0) {
                    stringOf("spaces.deletedCountMessage").replace("{n}", removed.toString())
                } else {
                    stringOf("spaces.deletedMessage")
                }
                _state.update {
                    it.copy(busy = false, spaces = spaces, message = deletedMessage)
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(busy = false, message = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun dismissMessage() = _state.update { it.copy(message = null) }
}