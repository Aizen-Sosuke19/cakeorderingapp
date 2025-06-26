package com.example.cakeorderingapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cakeorderingapp.ui.data.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    fun submitOrder(order: Order, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: run {
            onFailure("User not logged in")
            return
        }
        val orderWithUserId = order.copy(userId = userId, orderId = db.collection("orders").document().id)

        viewModelScope.launch {
            try {
                db.collection("orders").document(orderWithUserId.orderId)
                    .set(orderWithUserId)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Failed to submit order")
                        Log.e("OrderViewModel", "Error: ${e.message}", e)
                    }
            } catch (e: Exception) {
                onFailure(e.message ?: "Unexpected error")
                Log.e("OrderViewModel", "Unexpected error: ${e.message}", e)
            }
        }
    }

    fun fetchOrders() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("orders")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("OrderViewModel", "Error fetching orders: ${e.message}", e)
                    return@addSnapshotListener
                }
                val ordersList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Order::class.java)
                } ?: emptyList()
                _orders.value = ordersList
            }
    }
}