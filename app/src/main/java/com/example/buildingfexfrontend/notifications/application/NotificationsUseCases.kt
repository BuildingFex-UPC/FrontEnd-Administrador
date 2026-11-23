package com.example.buildingfexfrontend.notifications.application

import com.example.buildingfexfrontend.notifications.domain.model.NotificationItem
import com.example.buildingfexfrontend.notifications.domain.repository.NotificationsRepository

class NotificationsUseCases(private val repository: NotificationsRepository) {
    suspend fun listAdminNotifications(unreadOnly: Boolean? = null): List<NotificationItem> =
        repository.list(role = "admin", unreadOnly = unreadOnly)

    suspend fun getUnreadCount(): Int =
        repository.unreadCount(role = "admin")

    suspend fun markAsRead(id: String): Boolean =
        repository.markAsRead(id)

    suspend fun markAllAsRead(): Int =
        repository.markAllAsRead(role = "admin")
}

