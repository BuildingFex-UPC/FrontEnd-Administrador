package com.example.buildingfexfrontend.notifications.domain.model

data class NotificationItem(
    val id: String,
    val recipientId: String,
    val role: String,
    val title: String,
    val message: String,
    val type: String,
    val relatedEntityId: String?,
    val isRead: Boolean,
    val createdAt: String,
    val readAt: String?,
)

