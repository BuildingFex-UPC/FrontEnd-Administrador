package com.example.buildingfexfrontend

import com.example.buildingfexfrontend.socialspaces.domain.model.Reservation
import com.example.buildingfexfrontend.socialspaces.presentation.GenerationUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** US32: reserved schedules shown to the administrator by space and date. */
class ReservedSchedulesTest {

    private fun reservation(spaceId: String, date: String, start: String, end: String) =
        Reservation(spaceId = spaceId, date = date, startTime = start, endTime = end)

    private val reservations = listOf(
        reservation("gym", "2026-11-10", "18:00", "19:00"),
        reservation("gym", "2026-11-10", "09:00", "10:00"),
        reservation("gym", "2026-11-11", "09:00", "10:00"),
        reservation("bbq", "2026-11-10", "12:00", "15:00"),
    )

    @Test
    fun onlyShowsSlotsOfSelectedSpaceAndDate() {
        val state = GenerationUiState(
            reservations = reservations,
            scheduleSpaceId = "gym",
            scheduleDate = "2026-11-10",
        )

        assertEquals(2, state.scheduleSlots.size)
        assertTrue(state.scheduleSlots.all { it.spaceId == "gym" && it.date == "2026-11-10" })
    }

    @Test
    fun slotsAreOrderedByStartTime() {
        val state = GenerationUiState(
            reservations = reservations,
            scheduleSpaceId = "gym",
            scheduleDate = "2026-11-10",
        )

        assertEquals(listOf("09:00", "18:00"), state.scheduleSlots.map { it.startTime })
    }

    @Test
    fun acceptsDatesSentWithTime() {
        val state = GenerationUiState(
            reservations = listOf(reservation("gym", "2026-11-10T00:00:00", "09:00", "10:00")),
            scheduleSpaceId = "gym",
            scheduleDate = "2026-11-10",
        )

        assertEquals(1, state.scheduleSlots.size)
    }

    @Test
    fun freeDayHasNoSlots() {
        val state = GenerationUiState(
            reservations = reservations,
            scheduleSpaceId = "bbq",
            scheduleDate = "2026-11-11",
        )

        assertTrue(state.scheduleSlots.isEmpty())
    }
}
