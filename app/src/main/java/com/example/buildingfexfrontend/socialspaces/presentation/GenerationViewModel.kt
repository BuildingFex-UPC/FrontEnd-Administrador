package com.example.buildingfexfrontend.socialspaces.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.socialspaces.application.ReservationsUseCases
import com.example.buildingfexfrontend.socialspaces.application.SpacesUseCases
import com.example.buildingfexfrontend.socialspaces.domain.model.Guest
import com.example.buildingfexfrontend.socialspaces.domain.model.Reservation
import com.example.buildingfexfrontend.socialspaces.domain.model.Space
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GenerationUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val reservations: List<Reservation> = emptyList(),
    val spacesById: Map<String, Space> = emptyMap(),
    val savingKey: String? = null,
    val message: String? = null,
    val scheduleSpaceId: String? = null,
    val scheduleDate: String = Dates.todayYmd(),
) {
    val scheduleSlots: List<Reservation>
        get() = reservations
            .filter { it.spaceId == scheduleSpaceId && it.date.take(10) == scheduleDate }
            .sortedBy { it.startTime }
}

class GenerationViewModel(
    private val reservations: ReservationsUseCases,
    private val spaces: SpacesUseCases,
) : ViewModel() {

    private val _state = MutableStateFlow(GenerationUiState())
    val state: StateFlow<GenerationUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val list = reservations.adminList()
                val spaceList = spaces.list()
                val byId = spaceList.filterNotNullIds()
                _state.update { current ->
                    current.copy(
                        loading = false,
                        reservations = list.sortedWith(
                            compareByDescending<Reservation> { it.date }
                                .thenByDescending { it.startTime },
                        ),
                        spacesById = byId,
                        scheduleSpaceId = current.scheduleSpaceId?.takeIf { id -> byId.containsKey(id) }
                            ?: byId.keys.firstOrNull(),
                    )
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(loading = false, error = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun selectScheduleSpace(spaceId: String) = _state.update { it.copy(scheduleSpaceId = spaceId) }

    fun setScheduleDate(date: String) {
        if (date.isNotBlank()) _state.update { it.copy(scheduleDate = date) }
    }

    fun toggleCheckIn(reservation: Reservation, guest: Guest, checkedIn: Boolean) {
        val id = reservation.id ?: return
        val key = "$id:${guest.id}"
        if (_state.value.savingKey != null) return
        _state.update { it.copy(savingKey = key) }
        viewModelScope.launch {
            try {
                val updated = reservations.checkIn(id, guest.id, checkedIn)
                _state.update { s ->
                    s.copy(
                        savingKey = null,
                        reservations = s.reservations.map { r -> if (r.id == updated.id) updated else r },
                    )
                }
            } catch (e: Throwable) {
                _state.update { it.copy(savingKey = null) }
                load()
            }
        }
    }

    fun dismissMessage() = _state.update { it.copy(message = null) }
}

private fun List<Space>.filterNotNullIds(): Map<String, Space> =
    mapNotNull { space -> space.id?.let { it to space } }.toMap()