package com.example.cakeorderingapp


import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()
    var latestCakeName by remember { mutableStateOf("Cake: Not Available") }
    var latestDeliveryStatus by remember { mutableStateOf("Status: Not Available") }
    val defaultLocations = listOf("Westlands", "Muthaiga", "Kitisuru")
    var defaultIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var selectedCakeId by remember { mutableStateOf<String?>(null) }
    var cakesBoxClicked by remember { mutableStateOf(false) }

    // Listen for updates from CakeSelectionScreen
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow("refreshTracking", false)?.collect { shouldRefresh ->
            if (shouldRefresh) {
                coroutineScope.launch {
                    try {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            val latestOrder = db.collection("orders")
                                .whereEqualTo("userId", userId)
                                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .await()
                                .documents
                                .firstOrNull()
                            if (latestOrder != null) {
                                val cakeId = latestOrder.getString("cakeId")
                                latestDeliveryStatus = latestOrder.getString("deliveryStatus")?.let { "Status: $it" } ?: "Status: Not Available"
                                if (cakeId != null) {
                                    val cakeDoc = db.collection("cakes").document(cakeId).get().await()
                                    latestCakeName = cakeDoc.getString("name")?.let { "Cake: $it" } ?: "Cake: Unknown"
                                } else {
                                    Log.w("DashboardScreen", "No cakeId found in latest order")
                                }
                            } else {
                                Log.w("DashboardScreen", "No orders found for user $userId")
                            }
                        }
                    } catch (e: Exception) {
                        error = "Failed to load data: ${e.message}"
                        Log.e("DashboardScreen", "Error loading orders: ${e.message}", e)
                    }
                }
                navController.currentBackStackEntry?.savedStateHandle?.set("refreshTracking", false)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            navController.navigate("login") {
                popUpTo("dashboard") { inclusive = true }
            }
        } else {
            coroutineScope.launch {
                try {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val latestOrder = db.collection("orders")
                            .whereEqualTo("userId", userId)
                            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                            .limit(1)
                            .get()
                            .await()
                            .documents
                            .firstOrNull()
                        if (latestOrder != null) {
                            val cakeId = latestOrder.getString("cakeId")
                            latestDeliveryStatus = latestOrder.getString("deliveryStatus")?.let { "Status: $it" } ?: "Status: Not Available"
                            if (cakeId != null) {
                                val cakeDoc = db.collection("cakes").document(cakeId).get().await()
                                latestCakeName = cakeDoc.getString("name")?.let { "Cake: $it" } ?: "Cake: Unknown"
                            } else {
                                Log.w("DashboardScreen", "No cakeId found in latest order")
                            }
                        } else {
                            Log.w("DashboardScreen", "No orders found for user $userId")
                        }
                    }
                } catch (e: Exception) {
                    error = "Failed to load data: ${e.message}"
                    Log.e("DashboardScreen", "Error loading orders: ${e.message}", e)
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(end = 8.dp)
                        .clickable {
                            cakesBoxClicked = true
                            navController.navigate("cake_list") {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error // Red color
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Cakes", style = MaterialTheme.typography.titleLarge)
                        Text("Click to see cakes", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 8.dp)
                        .clickable { navController.navigate("flavour_screen") },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error // Red color
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Flavour", style = MaterialTheme.typography.titleLarge)
                        Text("Click to see flavours", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(end = 8.dp)
                        .clickable {
                            navController.navigate("delivery_tracking")
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Tracking", style = MaterialTheme.typography.titleLarge)
                        Text(latestCakeName, style = MaterialTheme.typography.bodyMedium)
                        Text(latestDeliveryStatus, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Logout", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                auth.signOut()
                                navController.navigate("login") {
                                    popUpTo("dashboard") { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Logout") }
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (error.isNotEmpty()) {
                Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}