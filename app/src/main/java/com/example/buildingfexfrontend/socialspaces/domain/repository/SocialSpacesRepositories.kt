package com.example.buildingfexfrontend.socialspaces.domain.repository

import com.example.buildingfexfrontend.socialspaces.domain.model.GuestInput
import com.example.buildingfexfrontend.socialspaces.domain.model.NewReservation
import com.example.buildingfexfrontend.socialspaces.domain.model.Reservation
import com.example.buildingfexfrontend.socialspaces.domain.model.Space

/**
 * Catalog of common areas administrators publish for residents.
 * Errors: `SPACE_NAME_REQUIRED`, `SPACE_NOT_FOUND`.
 */
interface SpacesRepository {

    suspend fun list(): List<Space>

    suspend fun add(
        name: String,
        description: String,
        capacity: Int?,
        imageUrl: String,
        cost: Double,
    ): Space

    suspend fun update(
        id: String,
        name: String,
        description: String,
        capacity: Int?,
        imageUrl: String?,
        cost: Double,
    ): Space

    suspend fun remove(id: String)
}

/**
 * Time-bounded reservations residents make against published spaces.
 * Errors: `RESERVATION_FIELDS_REQUIRED`, `RESERVATION_TIME_INVALID`,
 * `RESERVATION_OVERLAP`, `RESERVATION_NOT_FOUND`, `GUEST_NOT_FOUND`.
 */
interface ReservationsRepository {

    /** Reservations of one space (invite token stripped). */
    suspend fun listBySpace(spaceId: String): List<Reservation>

    suspend fun listByResident(residentId: String): List<Reservation>

    /** All reservations of the active admin (Generation oversight). */
    suspend fun listAllForAdmin(): List<Reservation>

    suspend fun removeBySpace(spaceId: String): Int

    suspend fun removeByResident(residentId: String): Int

    suspend fun add(request: NewReservation): Reservation

    /** Replaces the guest list (max 5 names) preserving check-in state by guest id. */
    suspend fun updateGuests(reservationId: String, guests: List<GuestInput>): Reservation

    suspend fun setGuestCheckedIn(reservationId: String, guestId: String, checkedIn: Boolean): Reservation
}
