package com.example.buildingfexfrontend

import com.example.buildingfexfrontend.notifications.data.remote.NotificationDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationsTest {

    @Test
    fun notificationDto_mapsToDomainCorrectly() {
        val dto = NotificationDto(
            id = "notif-123",
            recipientId = "admin-1",
            role = "admin",
            title = "Nuevo incidente reportado",
            message = "El residente Juan reportó una avería",
            type = "incident_reported",
            relatedEntityId = "inc-456",
            isRead = false,
            createdAt = "2026-11-23T12:00:00Z",
            readAt = null,
        )

        val domain = dto.toDomain()

        assertEquals("notif-123", domain.id)
        assertEquals("admin-1", domain.recipientId)
        assertEquals("admin", domain.role)
        assertEquals("Nuevo incidente reportado", domain.title)
        assertEquals("El residente Juan reportó una avería", domain.message)
        assertEquals("incident_reported", domain.type)
        assertEquals("inc-456", domain.relatedEntityId)
        assertFalse(domain.isRead)
        assertEquals("2026-11-23T12:00:00Z", domain.createdAt)
    }

    @Test
    fun notificationDto_defaultsFallbackValues() {
        val dto = NotificationDto()
        val domain = dto.toDomain()

        assertEquals("", domain.id)
        assertEquals("general", domain.type)
        assertFalse(domain.isRead)
    }
}

