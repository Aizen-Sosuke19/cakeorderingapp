package com.example.cakeorderingapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.cakeorderingapp.data.Cake
import com.example.cakeorderingapp.data.Order
import com.example.cakeorderingapp.viewmodel.OrderViewModel

@Composable
fun OrderFormScreen(
    navController: NavHostController,
    cakeId: String,
    selectedLocation: String?
) {
    val viewModel: OrderViewModel = viewModel()
    val cakes = listOf(
        Cake(id = "chocolate", "Chocolate", 1500.0, "Rich chocolate cake", "Dark Chocolate", imageUrl = ""),
        Cake(id = "vanilla", "Vanilla", 1200.0, "Creamy vanilla cake", "Madagascar Vanilla", imageUrl = ""),
        Cake(id = "strawberry", "Strawberry", 1300.0, "Fresh strawberry cake", "Fresh Strawberry", imageUrl ="" )
    )
    val cake = cakes.find { it.id == cakeId } ?: return
    var quantity by remember { mutableStateOf("1") }
    var deliveryAddress by remember { mutableStateOf(selectedLocation ?: "") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Order ${cake.name}", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = deliveryAddress,
            onValueChange = { deliveryAddress = it },
            label = { Text("Delivery Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val qty = quantity.toIntOrNull()
                if (qty != null && qty > 0 && deliveryAddress.isNotBlank()) {
                    val order = Order(
                        cakeId = cake.id,
                        cakeName = cake.name,
                        quantity = qty,
                        totalPrice = cake.price * qty,
                        deliveryAddress = deliveryAddress
                    )
                    viewModel.submitOrder(
                        order = order,
                        onSuccess = {
                            navController.navigate("delivery_tracking") {
                                popUpTo("order_form/$cakeId") { inclusive = true }
                            }
                        },
                        onFailure = { errorMessage ->
                            error = errorMessage
                        }
                    )
                } else {
                    error = "Please enter a valid quantity and delivery address"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Order")
        }

        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}