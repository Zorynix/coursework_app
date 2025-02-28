package com.example.coursework.data.models

data class Notification(
    val createdAt: String,
    val id: String,
    val isRead: Boolean,
    val message: String,
    val orderId: String,
    val title: String,
    val type: String,
    val userId: String,
)
