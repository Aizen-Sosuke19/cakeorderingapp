package com.example.cakeorderingapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Order(
    val id: String = "",
    val flavourId: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val status: String = "Pending",
    val timestamp: String = ""
)

class DeliveryTrackingViewModel : ViewModel() {
    private val db = Firebase.database.reference
    private val auth = FirebaseAuth.getInstance()
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders
    internal val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchOrders(flavourId: String) {
        val userId = auth.currentUser?.uid ?: run {
            _errorMessage.value = "Please log in to view orders"
            return
        }
        println("Fetching orders for user: $userId, flavourId: $flavourId")
        db.child("orders")
            .orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    println("Fetched ${snapshot.childrenCount} orders: ${snapshot.value}")
                    val ordersList = snapshot.children
                        .mapNotNull { it.getValue(Order::class.java)?.copy(id = it.key ?: "") }
                        .filter { it.flavourId == flavourId }
                    _orders.value = ordersList
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Fetch orders error: ${error.message}, code: ${error.code}")
                    _errorMessage.value = "Failed to fetch orders: ${error.message}"
                }
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryTrackingScreen(
    navController: NavHostController,
    flavourId: String = "",
    viewModel: DeliveryTrackingViewModel = viewModel()
) {
    val auth = FirebaseAuth.getInstance()
    val orders by viewModel.orders.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Please log in to view orders")
                navController.navigate("login") {
                    popUpTo("dashboard") { inclusive = true }
                }
            }
        } else if (flavourId.isEmpty()) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Invalid flavour selected")
                navController.navigate("flavour")
            }
        } else {
            viewModel.fetchOrders(flavourId)
            isLoading = false
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
            }
            viewModel._errorMessage.value = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black,
        contentColor = Color(0xFFFFD700),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Track Your Orders",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFFFFD700)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color(0xFFFFD700)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Orders for ${flavourId.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFFFFD700)
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFFFFD700),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else if (orders.isEmpty()) {
                Text(
                    text = "No orders found for this flavour",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(orders) { order ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1C2526)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Order #${order.id.takeLast(6)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFFFFD700)
                                )
                                Text(
                                    text = "Flavour: ${order.flavourId}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = "Amount: KES ${order.amount}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = "Status: ${order.status}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = "Placed: ${order.timestamp}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}