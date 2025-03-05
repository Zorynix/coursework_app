package com.example.coursework.data.models

import kotlinx.serialization.Serializable

@Serializable
data class FoodItem(
    val arModelUrl: String? = null,
    val createdAt: String? = null,
    val description: String,
    val id: String,
    val imageUrl: String,
    val name: String,
    val price: Double,
    val restaurantId: String
)
