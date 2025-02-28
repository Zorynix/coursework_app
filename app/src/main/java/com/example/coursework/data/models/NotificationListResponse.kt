package com.example.coursework.data.models

data class NotificationListResponse(
    val notifications: List<Notification>,
    val unreadCount: Int,
)
