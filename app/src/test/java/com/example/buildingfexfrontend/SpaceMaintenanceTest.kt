package com.example.buildingfexfrontend

import com.example.buildingfexfrontend.dashboard.presentation.splitReservations
import com.example.buildingfexfrontend.socialspaces.data.remote.SpaceDto
import com.example.buildingfexfrontend.socialspaces.data.remote.SpacePatch
import com.example.buildingfexfrontend.socialspaces.domain.model.Reservation
import com.example.buildingfexfrontend.socialspaces.domain.model.Space
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** US19: rules and maintenance status of common areas. */
class SpaceMaintenanceTest {

    @Test
    fun spaceDtoMapsRulesAndMaintenance() {
        val dto = SpaceDto(
            id = "space-1",
            name = "Salón",
            rules = "No correr.",
            underMaintenance = true,
        ).toDomain()

        assertEquals("No correr.", dto.rules)
        assertTrue(dto.underMaintenance)
    }

    @Test
    fun spaceDtoDefaultsWhenBackendOmitsNewFields() {
        // Backward compatible: an older backend response without the fields.
        val dto = SpaceDto(id = "space-2", name = "Terraza").toDomain()

        assertEquals("", dto.rules)
        assertFalse(dto.underMaintenance)
    }

    @Test
    fun spaceDefaultsToAvailableWithNoRules() {
        val space = Space(id = "space-3", name = "Piscina")

        assertEquals("", space.rules)
        assertFalse(space.underMaintenance)
    }

    @Test
    fun spacePatchCarriesBothFields() {
        val patch = SpacePatch(
            name = "Salón",
            rules = "Regla nueva",
            underMaintenance = true,
        )

        assertEquals("Regla nueva", patch.rules)
        assertEquals(true, patch.underMaintenance)
    }

    @Test
    fun spacePatchKeepsFieldsNullWhenNotEdited() {
        val patch = SpacePatch(name = "Salón")

        assertNull(patch.rules)
        assertNull(patch.underMaintenance)
    }
}

/** US26: reservation summary shown on the admin dashboard. */
class AdminDashboardReservationsTest {

    private fun reservation(date: String, start: String, resident: String = "R") =
        Reservation(
            spaceId = "gym",
            residentName = resident,
            date = date,
            startTime = start,
            endTime = "10:00",
        )

    @Test
    fun splitsTodayAndUpcoming() {
        val reservations = listOf(
            reservation("2026-11-12", "18:00", "Hoy tarde"),
            reservation("2026-11-13", "09:00", "Mañana"),
            reservation("2026-11-11", "10:00", "Ayer"),
        )

        val (today, upcoming) = splitReservations(reservations, "2026-11-12")

        assertEquals(listOf("Hoy tarde"), today.map { it.residentName })
        assertEquals(listOf("Mañana"), upcoming.map { it.residentName })
    }

    @Test
    fun todayIsOrderedByStartTime() {
        val reservations = listOf(
            reservation("2026-11-12", "18:00", "Tarde"),
            reservation("2026-11-12", "09:00", "Mañana"),
        )

        val (today, _) = splitReservations(reservations, "2026-11-12")

        assertEquals(listOf("Mañana", "Tarde"), today.map { it.residentName })
    }

    @Test
    fun acceptsIsoDatesAndLimitsUpcomingToFive() {
        val reservations = (1..7).map { day ->
            reservation("2026-11-${(12 + day).toString().padStart(2, '0')}T00:00:00", "09:00", "d$day")
        }

        val (today, upcoming) = splitReservations(reservations, "2026-11-12")

        assertTrue(today.isEmpty())
        assertEquals(5, upcoming.size)
        assertEquals("d1", upcoming.first().residentName)
    }

    @Test
    fun emptyListYieldsEmptySummary() {
        val (today, upcoming) = splitReservations(emptyList(), "2026-11-12")

        assertTrue(today.isEmpty())
        assertTrue(upcoming.isEmpty())
    }
}
