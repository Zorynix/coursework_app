package com.example.coursework.data.models

data class ConfirmPaymentResponse(
    val clientSecret: String,
    val message: String,
    val orderId: String,
    val orderStatus: String,
    val requiresAction: Boolean,
    val status: String
)