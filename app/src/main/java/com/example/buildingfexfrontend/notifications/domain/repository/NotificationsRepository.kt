package com.example.buildingfexfrontend.notifications.domain.repository

import com.example.buildingfexfrontend.notifications.domain.model.NotificationItem

interface NotificationsRepository {
    suspend fun list(role: String? = null, unreadOnly: Boolean? = null): List<NotificationItem>
    suspend fun unreadCount(role: String? = null): Int
    suspend fun markAsRead(id: String): Boolean
    suspend fun markAllAsRead(role: String? = null): Int
}

