package com.example.cakeorderingapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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


class DashboardViewModel : ViewModel() {
    private val db = Firebase.database.reference
    private val auth = FirebaseAuth.getInstance()
    private val _latestOrder = MutableStateFlow<Order?>(null)
    val latestOrder: StateFlow<Order?> = _latestOrder
    private val _latestCakeName = MutableStateFlow("Cake: Not Available")
    val latestCakeName: StateFlow<String> = _latestCakeName
    internal val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchOrders() {
        val userId = auth.currentUser?.uid ?: run {
            _errorMessage.value = "Please log in to view orders"
            return
        }
        println("Fetching orders for user: $userId, flavourId: chocolate")
        db.child("orders")
            .orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    println("Fetched ${snapshot.childrenCount} orders: ${snapshot.value}")
                    val latestOrder = snapshot.children
                        .mapNotNull { it.getValue(Order::class.java)?.copy(id = it.key ?: "") }
                        .filter { it.flavourId == "chocolate" }
                        .maxByOrNull { it.timestamp }
                    _latestOrder.value = latestOrder
                    _latestCakeName.value = if (latestOrder != null) {
                        "Cake: Chocolate (Order #${latestOrder.id.takeLast(6)})"
                    } else {
                        "Cake: Not Available"
                    }
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
fun DashboardScreen(navController: NavHostController, viewModel: DashboardViewModel = viewModel()) {
    val auth = FirebaseAuth.getInstance()
    val latestOrder by viewModel.latestOrder.collectAsState()
    val latestCakeName by viewModel.latestCakeName.collectAsState()
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
        } else {
            viewModel.fetchOrders()
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
                        text = "Dashboard",
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
                            navController.navigate("cake_selection")
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1C2526)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Cakes",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = "Click to see cakes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 8.dp)
                        .clickable {
                            navController.navigate("flavour")
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1C2526)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Flavours",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = "Click to see flavours",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
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
                            latestOrder?.flavourId?.let { flavourId ->
                                navController.navigate("delivery_tracking/$flavourId")
                            } ?: run {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("No order selected")
                                }
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1C2526)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tracking",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = latestCakeName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            text = latestOrder?.status?.let { "Status: $it" } ?: "Status: Not Available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1C2526)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Logout",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                auth.signOut()
                                navController.navigate("login") {
                                    popUpTo("dashboard") { inclusive = true }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD700),
                                contentColor = Color.Black
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Logout")
                        }
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFFFFD700),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}