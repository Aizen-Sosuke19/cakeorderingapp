package com.example.cakeorderingapp


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Order(
    val id: String,
    val cakeId: String,
    val quantity: Int,
    val deliveryAddress: String,
    val deliveryDate: String,
    val deliveryTime: String,
    val deliveryFee: Double,
    val totalPrice: Double,
    val status: String,
    val deliveryStatus: String
)

@Composable
fun DeliveryTrackingScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val coroutineScope = rememberCoroutineScope()
    var orders by remember { mutableStateOf<List<Pair<Order, String>>>(emptyList()) } // Pair of Order and cakeName
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            navController.navigate("login") {
                popUpTo("delivery_tracking") { inclusive = true }
            }
        } else {
            coroutineScope.launch {
                try {
                    val orderDocs = db.collection("orders")
                        .whereEqualTo("userId", auth.currentUser?.uid)
                        .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .get()
                        .await()
                    orders = orderDocs.documents.mapNotNull { doc ->
                        val order = Order(
                            id = doc.id,
                            cakeId = doc.getString("cakeId") ?: "",
                            quantity = doc.getLong("quantity")?.toInt() ?: 0,
                            deliveryAddress = doc.getString("deliveryAddress") ?: "",
                            deliveryDate = doc.getString("deliveryDate") ?: "",
                            deliveryTime = doc.getString("deliveryTime") ?: "",
                            deliveryFee = doc.getDouble("deliveryFee") ?: 0.0,
                            totalPrice = doc.getDouble("totalPrice") ?: 0.0,
                            status = doc.getString("status") ?: "",
                            deliveryStatus = doc.getString("deliveryStatus") ?: ""
                        )
                        // Fetch cake name
                        val cakeDoc = db.collection("cakes").document(order.cakeId).get().await()
                        val cakeName = cakeDoc.getString("name") ?: "Unknown Cake"
                        Pair(order, cakeName)
                    }
                } catch (e: Exception) {
                    error = e.message ?: "Failed to load orders"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Track Deliveries", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        } else if (orders.isEmpty()) {
            Text("No orders found", style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyColumn {
                items(orders) { (order, cakeName) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Order: $cakeName", style = MaterialTheme.typography.titleMedium)
                            Text("Order ID: ${order.id.take(8)}...")
                            Text("Delivery Address: ${order.deliveryAddress}")
                            Text("Delivery Date: ${order.deliveryDate}")
                            Text("Delivery Time: ${order.deliveryTime}")
                            Text("Delivery Status: ${order.deliveryStatus}")
                            Text("Total: KSh ${order.totalPrice}")
                        }
                    }
                }
            }
        }
    }
}