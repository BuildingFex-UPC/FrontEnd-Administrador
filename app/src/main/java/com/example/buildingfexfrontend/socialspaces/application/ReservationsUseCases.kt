package com.example.buildingfexfrontend.socialspaces.application

import com.example.buildingfexfrontend.socialspaces.domain.model.Reservation
import com.example.buildingfexfrontend.socialspaces.domain.repository.ReservationsRepository

/** Reservation use cases (admin side): listing every reservation and guest check-in. */
class ReservationsUseCases(
    private val repository: ReservationsRepository,
) {
    val adminList = AdminReservationsUseCase(repository)
    val checkIn = CheckInGuestUseCase(repository)
}

class AdminReservationsUseCase(private val repository: ReservationsRepository) {
    suspend operator fun invoke(): List<Reservation> = repository.listAllForAdmin()
}

class CheckInGuestUseCase(private val repository: ReservationsRepository) {
    suspend operator fun invoke(
        reservationId: String,
        guestId: String,
        checkedIn: Boolean,
    ): Reservation = repository.setGuestCheckedIn(reservationId, guestId, checkedIn)
}
