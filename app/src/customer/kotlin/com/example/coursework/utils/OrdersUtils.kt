package com.example.coursework.utils

object OrdersUtils {

    enum class OrderStatus {
        PENDING_ACCEPTANCE,
        ACCEPTED,
        PREPARING,
        READY,
        ASSIGNED,
        OUT_FOR_DELIVERY,
        DELIVERED,
        REJECTED,
        CANCELLED
    }
}