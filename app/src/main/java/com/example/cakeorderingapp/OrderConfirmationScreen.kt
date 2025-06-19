package com.example.cakeorderingapp


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun OrderConfirmationScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Order Confirmed!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Your order has been placed successfully.", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate("dashboard") {
                popUpTo("dashboard") { inclusive = true }
            } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Dashboard")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { navController.navigate("delivery_tracking") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Track Delivery")
        }
    }
}