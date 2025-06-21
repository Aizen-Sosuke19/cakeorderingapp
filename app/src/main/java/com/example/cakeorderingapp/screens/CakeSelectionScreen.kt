package com.example.cakeorderingapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.cakeorderingapp.data.Cake

@Composable
fun CakeSelectionScreen(navController: NavHostController) {
    val cakes = listOf(
        Cake(
            "chocolate", "Chocolate", 1500.0, "Rich chocolate cake", "Dark Chocolate",
            imageUrl =""
        ),
        Cake(
            "vanilla", "Vanilla", 1200.0, "Creamy vanilla cake", "Madagascar Vanilla",
            imageUrl =""
        ),
        Cake(
            "strawberry", "Strawberry", 1300.0, "Fresh strawberry cake", "Fresh Strawberry",
            imageUrl =""
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(cakes) { cake ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { navController.navigate("cake_details/${cake.id}") },
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(cake.name, style = MaterialTheme.typography.titleMedium)
                    Text("Price: KSh ${cake.price}", style = MaterialTheme.typography.bodyMedium)
                    Text("Flavour: ${cake.flavour}", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { navController.navigate("order_form/${cake.id}") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Order Now")
                    }
                }
            }
        }
    }
}