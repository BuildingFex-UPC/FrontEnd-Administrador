package com.example.buildingfexfrontend.notifications.data.remote

import com.example.buildingfexfrontend.notifications.domain.model.NotificationItem
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

data class NotificationDto(
    val id: String? = null,
    val recipientId: String? = null,
    val role: String? = null,
    val title: String? = null,
    val message: String? = null,
    val type: String? = null,
    val relatedEntityId: String? = null,
    val isRead: Boolean? = null,
    val createdAt: String? = null,
    val readAt: String? = null,
) {
    fun toDomain() = NotificationItem(
        id = id.orEmpty(),
        recipientId = recipientId.orEmpty(),
        role = role.orEmpty(),
        title = title.orEmpty(),
        message = message.orEmpty(),
        type = type ?: "general",
        relatedEntityId = relatedEntityId,
        isRead = isRead ?: false,
        createdAt = createdAt.orEmpty(),
        readAt = readAt,
    )
}

data class UnreadCountDto(val count: Int = 0)
data class MarkReadDto(val success: Boolean = false)
data class MarkAllReadDto(val markedCount: Int = 0)

interface NotificationsApi {
    @GET("notifications")
    suspend fun list(@QueryMap params: Map<String, @JvmSuppressWildcards String>): List<NotificationDto>

    @GET("notifications/unread-count")
    suspend fun unreadCount(@QueryMap params: Map<String, @JvmSuppressWildcards String>): UnreadCountDto

    @POST("notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: String): MarkReadDto

    @POST("notifications/read-all")
    suspend fun markAllAsRead(@QueryMap params: Map<String, @JvmSuppressWildcards String>): MarkAllReadDto
}

