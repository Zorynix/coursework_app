package com.example.coursework.data.models

data class ConfirmPaymentRequest(
    val paymentIntentId: String,
    val addressId: String,
)
