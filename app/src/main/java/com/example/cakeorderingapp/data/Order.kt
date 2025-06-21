package com.example.cakeorderingapp.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val cakeId: String = "",
    val cakeName: String = "",
    val quantity: Int = 1,
    val totalPrice: Double = 0.0,
    val deliveryAddress: String = "",
    val status: String = "Pending",
    @ServerTimestamp val orderDate: Date? = null
)