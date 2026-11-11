package com.example.buildingfexfrontend.socialspaces.domain.model

/**
 * Social Spaces ubiquitous language: common areas published by admins and
 * time-bounded reservations residents create against them.
 */
data class Space(
    val id: String? = null,
    val name: String,
    val description: String = "",
    val capacity: Int? = null,
    val imageUrl: String = "",
    val cost: Double = 0.0,
)

data class Guest(
    val id: String,
    val name: String,
    val checkedIn: Boolean = false,
    val checkedInAt: String? = null,
)

/** Editable row of the guest-list dialog (id may be blank for new guests). */
data class GuestInput(val id: String = "", val name: String = "")

data class Reservation(
    val id: String? = null,
    val spaceId: String,
    val residentId: String = "",
    val residentName: String = "",
    val residentCode: String = "",
    val date: String,
    val startTime: String,
    val endTime: String,
    val guests: List<Guest> = emptyList(),
    val guestInviteToken: String? = null,
    val ownerAdminId: String? = null,
)

data class NewReservation(
    val spaceId: String,
    val residentId: String,
    val residentName: String,
    val residentCode: String,
    val date: String,
    val startTime: String,
    val endTime: String,
)
