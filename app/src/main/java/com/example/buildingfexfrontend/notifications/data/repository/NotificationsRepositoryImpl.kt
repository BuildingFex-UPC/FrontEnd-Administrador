package com.example.buildingfexfrontend.notifications.data.repository

import com.example.buildingfexfrontend.core.data.network.apiCall
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.notifications.data.remote.NotificationsApi
import com.example.buildingfexfrontend.notifications.domain.model.NotificationItem
import com.example.buildingfexfrontend.notifications.domain.repository.NotificationsRepository

class NotificationsRepositoryImpl(
    private val api: NotificationsApi,
    private val session: SessionRepository,
) : NotificationsRepository {

    private fun queryParams(role: String? = null, unreadOnly: Boolean? = null): Map<String, String> =
        buildMap {
            session.activeDataOwnerId?.let { put("recipientId", it) }
            role?.let { put("role", it) }
            unreadOnly?.let { put("unreadOnly", it.toString()) }
        }

    override suspend fun list(role: String?, unreadOnly: Boolean?): List<NotificationItem> = apiCall {
        api.list(queryParams(role, unreadOnly)).map { it.toDomain() }
    }

    override suspend fun unreadCount(role: String?): Int = apiCall {
        api.unreadCount(queryParams(role)).count
    }

    override suspend fun markAsRead(id: String): Boolean = apiCall {
        api.markAsRead(id).success
    }

    override suspend fun markAllAsRead(role: String?): Int = apiCall {
        api.markAllAsRead(queryParams(role)).markedCount
    }
}

