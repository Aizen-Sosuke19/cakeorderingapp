package com.example.cakeorderingapp


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Cake(
    val id: String,
    val name: String,
    val price: Double,
    val description: String,
    val flavour: String,
    val imageUrl: String = ""
)

@Composable
fun CakeListScreen(navController: NavHostController, flavour: String?) {
    LaunchedEffect(Unit) {
        navController.navigate("cake_selection")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun CakeSelectionScreen(navController: NavHostController, categoryId: String?) {
    val cakes = listOf(
        Cake("1", "Strawberry Cake", 1500.0, "Fresh strawberry cake", "Strawberry"),
        Cake("2", "Vanilla Cake", 1300.0, "Creamy vanilla cake", "Vanilla"),
        Cake("3", "Chocolate Cake", 1500.0, "Rich chocolate cake", "Chocolate")
    )
    val cakesBoxClicked by navController.previousBackStackEntry?.savedStateHandle?.getStateFlow("cakesBoxClicked", false)?.collectAsState() ?: mutableStateOf(false)
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Available Cakes", style = MaterialTheme.typography.headlineMedium)
        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(cakes) { cake ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(cake.name, style = MaterialTheme.typography.titleMedium)
                        Text("Price: KSh ${cake.price}", style = MaterialTheme.typography.bodyMedium)
                        Text("Description: ${cake.description}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                try {
                                    navController.navigate("delivery_setup/${cake.id}") {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                } catch (e: Exception) {
                                    error = "Navigation failed: ${e.message}"
                                    Log.e("CakeSelectionScreen", "Navigation error: ${e.message}", e)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = cakesBoxClicked
                        ) {
                            Text("Order Now")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeliverySetupScreen(navController: NavHostController, cakeId: String?) {
    val locations = listOf("Westlands", "Kitisuru", "Muthaiga")
    var selectedLocation by remember { mutableStateOf(locations.first()) }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val coroutineScope = rememberCoroutineScope()
    var cake by remember { mutableStateOf<Cake?>(null) }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(cakeId) {
        if (cakeId != null) {
            coroutineScope.launch {
                try {
                    val cakes = mapOf(
                        "1" to Cake("1", "Strawberry Cake", 1500.0, "Fresh strawberry cake", "Strawberry"),
                        "2" to Cake("2", "Vanilla Cake", 1300.0, "Creamy vanilla cake", "Vanilla"),
                        "3" to Cake("3", "Chocolate Cake", 1500.0, "Rich chocolate cake", "Chocolate")
                    )
                    cake = cakes[cakeId] ?: throw Exception("Cake not found for ID: $cakeId")
                } catch (e: Exception) {
                    error = "Failed to load cake: ${e.message}"
                    Log.e("DeliverySetupScreen", "Error loading cake: ${e.message}", e)
                } finally {
                    isLoading = false
                }
            }
        } else {
            error = "Invalid cake ID"
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        } else {
            cake?.let {
                Text("Select Delivery Location for ${it.name}", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Column {
                    locations.forEach { location ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedLocation = location }
                        ) {
                            RadioButton(
                                selected = (selectedLocation == location),
                                onClick = { selectedLocation = location }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(location, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
                                val order = hashMapOf(
                                    "userId" to userId,
                                    "cakeId" to it.id,
                                    "quantity" to 1,
                                    "deliveryAddress" to selectedLocation,
                                    "deliveryDate" to "",
                                    "deliveryTime" to "",
                                    "deliveryFee" to 200.0,
                                    "totalPrice" to (it.price + 200.0),
                                    "phoneNumber" to "",
                                    "timestamp" to System.currentTimeMillis(),
                                    "status" to "Pending",
                                    "deliveryStatus" to "Pending"
                                )
                                val orderRef = db.collection("orders").add(order).await()
                                Log.d("DeliverySetupScreen", "Order created with ID: ${orderRef.id}")
                                // Trigger dashboard refresh
                                navController.previousBackStackEntry?.savedStateHandle?.set("refreshTracking", true)
                                navController.navigate("dashboard") {
                                    popUpTo("dashboard") { inclusive = false }
                                }
                            } catch (e: Exception) {
                                error = "Order failed: ${e.message}"
                                Log.e("DeliverySetupScreen", "Error creating order: ${e.message}", e)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirm Order")
                }
            }
        }
    }
}

@Composable
fun DeliveryTrackingScreen(navController: NavHostController, categoryId: String?) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val coroutineScope = rememberCoroutineScope()
    var orders by remember { mutableStateOf<List<Pair<Order, String>>>(emptyList()) }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val orderDocs = db.collection("orders")
                        .whereEqualTo("userId", userId)
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
                        val cakeDoc = db.collection("cakes").document(order.cakeId).get().await()
                        val cakeName = cakeDoc.getString("name") ?: "Unknown Cake"
                        Pair(order, cakeName)
                    }
                }
            } catch (e: Exception) {
                error = "Failed to load tracking data: ${e.message}"
                Log.e("DeliveryTrackingScreen", "Error loading orders: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        } else if (orders.isEmpty()) {
            Text("No orders to track", modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Text("Delivery Tracking", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(orders) { (order, cakeName) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Order: $cakeName", style = MaterialTheme.typography.titleMedium)
                            Text("Order ID: ${order.id.take(8)}...", style = MaterialTheme.typography.bodyMedium)
                            Text("Delivery Address: ${order.deliveryAddress}", style = MaterialTheme.typography.bodyMedium)
                            Text("Status: ${order.deliveryStatus}", style = MaterialTheme.typography.bodyMedium)
                            Text("Total: KSh ${order.totalPrice}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlavourScreen(navController: NavHostController) {
    val flavours = mapOf(
        "Chocolate" to "Dark Chocolate",
        "Vanilla" to "Madagascar Vanilla",
        "Strawberry" to "Fresh Strawberry"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Flavours", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(flavours.toList()) { (flavour, description) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(flavour, style = MaterialTheme.typography.titleMedium)
                        Text(description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun CakeDetailsScreen(navController: NavHostController, cakeId: String?) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text("Not used in current flow", modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun DeliverySelectionScreen(navController: NavHostController, cakeId: String?) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text("Not used in current flow", modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun OrderFormScreen(navController: NavHostController, cakeId: String?, selectedLocation: String?) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text("Not used in current flow", modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun AdminCakeScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()
    var cakes by remember { mutableStateOf<List<Cake>>(emptyList()) }
    var orders by remember { mutableStateOf<List<Pair<Order, String>>>(emptyList()) }
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var flavour by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val cakeResult = db.collection("cakes").get().await()
                cakes = cakeResult.documents.mapNotNull { doc ->
                    Cake(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        price = doc.getDouble("price") ?: 0.0,
                        description = doc.getString("description") ?: "",
                        flavour = doc.getString("flavour") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: ""
                    )
                }
                val orderDocs = db.collection("orders").get().await()
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
                    val cakeDoc = db.collection("cakes").document(order.cakeId).get().await()
                    val cakeName = cakeDoc.getString("name") ?: "Unknown Cake"
                    Pair(order, cakeName)
                }
            } catch (e: Exception) {
                error = "Failed to load data: ${e.message}"
                Log.e("AdminCakeScreen", "Error loading data: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Manage Cakes", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Cake Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Price (KSh)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = flavour,
            onValueChange = { flavour = it },
            label = { Text("Flavour") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("Image URL") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                when {
                    name.isBlank() -> error = "Name cannot be empty"
                    price.isBlank() || price.toDoubleOrNull() == null || price.toDouble() <= 0 -> {
                        error = "Invalid price"
                    }
                    description.isBlank() -> error = "Description cannot be empty"
                    flavour.isBlank() -> error = "Flavour cannot be empty"
                    else -> {
                        coroutineScope.launch {
                            try {
                                val newCake = hashMapOf(
                                    "name" to name,
                                    "price" to price.toDouble(),
                                    "description" to description,
                                    "flavour" to flavour,
                                    "imageUrl" to imageUrl
                                )
                                db.collection("cakes").add(newCake).await()
                                name = ""
                                price = ""
                                description = ""
                                flavour = ""
                                imageUrl = ""
                                val result = db.collection("cakes").get().await()
                                cakes = result.documents.mapNotNull { doc ->
                                    Cake(
                                        id = doc.id,
                                        name = doc.getString("name") ?: "",
                                        price = doc.getDouble("price") ?: 0.0,
                                        description = doc.getString("description") ?: "",
                                        flavour = doc.getString("flavour") ?: "",
                                        imageUrl = doc.getString("imageUrl") ?: ""
                                    )
                                }
                            } catch (e: Exception) {
                                error = "Failed to add cake: ${e.message}"
                                Log.e("AdminCakeScreen", "Error adding cake: ${e.message}", e)
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Cake")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Manage Orders", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
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
                            Text("Delivery Status: ${order.deliveryStatus}")
                            Row {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            try {
                                                db.collection("orders").document(order.id)
                                                    .update("deliveryStatus", "Out for Delivery").await()
                                                orders = orders.map {
                                                    if (it.first.id == order.id) {
                                                        Pair(it.first.copy(deliveryStatus = "Out for Delivery"), it.second)
                                                    } else it
                                                }
                                            } catch (e: Exception) {
                                                error = "Failed to update status: ${e.message}"
                                                Log.e("AdminCakeScreen", "Error updating status: ${e.message}", e)
                                            }
                                        }
                                    }
                                ) {
                                    Text("Set Out for Delivery")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            try {
                                                db.collection("orders").document(order.id)
                                                    .update("deliveryStatus", "Delivered").await()
                                                orders = orders.map {
                                                    if (it.first.id == order.id) {
                                                        Pair(it.first.copy(deliveryStatus = "Delivered"), it.second)
                                                    } else it
                                                }
                                            } catch (e: Exception) {
                                                error = "Failed to update status: ${e.message}"
                                                Log.e("AdminCakeScreen", "Error updating status: ${e.message}", e)
                                            }
                                        }
                                    }
                                ) {
                                    Text("Set Delivered")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}