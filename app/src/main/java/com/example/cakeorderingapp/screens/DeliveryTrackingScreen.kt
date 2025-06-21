package com.example.cakeorderingapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.cakeorderingapp.data.Order
import com.example.cakeorderingapp.viewmodel.OrderViewModel

@Composable
fun DeliveryTrackingScreen(navController: NavHostController) {
    val viewModel: OrderViewModel = viewModel()
    val orders = viewModel.orders.collectAsState() // Remove 'by' and use .value later

    LaunchedEffect(Unit) {
        viewModel.fetchOrders()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Order Tracking", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        if (orders.value.isEmpty()) { // Access value explicitly
            Text("No orders found", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn {
                items(orders.value) { order -> // Access value explicitly
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Order: ${order.cakeName}", style = MaterialTheme.typography.titleMedium)
                            Text("Quantity: ${order.quantity}", style = MaterialTheme.typography.bodyMedium)
                            Text("Total: KSh ${order.totalPrice}", style = MaterialTheme.typography.bodyMedium)
                            Text("Address: ${order.deliveryAddress}", style = MaterialTheme.typography.bodyMedium)
                            Text("Status: ${order.status}", style = MaterialTheme.typography.bodyMedium)
                            order.orderDate?.let {
                                Text("Ordered: $it", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}