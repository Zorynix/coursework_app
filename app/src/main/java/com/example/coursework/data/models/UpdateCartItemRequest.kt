package com.example.coursework.data.models

data class UpdateCartItemRequest(
    val cartItemId: String,
    val quantity: Int,
)
