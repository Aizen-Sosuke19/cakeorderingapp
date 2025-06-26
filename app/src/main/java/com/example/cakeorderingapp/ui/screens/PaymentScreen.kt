package com.example.cakeorderingapp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cakeorderingapp.ui.data.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PurchaseScreen(flavourId: String, navController: NavController) {
    val db = Firebase.database.reference
    val auth = FirebaseAuth.getInstance()
    var flavour by remember { mutableStateOf<CakeFlavour?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Fetch flavour details
    LaunchedEffect(flavourId) {
        db.child("flavours").child(flavourId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                flavour = snapshot.getValue(CakeFlavour::class.java)?.copy(id = snapshot.key ?: "")
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                errorMessage = "Failed to load flavour: ${error.message}"
                isLoading = false
            }
        })
    }

    // Show error snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
            }
            errorMessage = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black,
        contentColor = Color(0xFFFFD700)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFFFFD700))
            } else if (flavour != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .animateContentSize(animationSpec = tween(300)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1C2526)
                        ),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = flavour!!.name,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp
                                ),
                                color = Color(0xFFFFD700)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = flavour!!.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Place: ${flavour!!.placeName}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Price: KES ${flavour!!.price}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color(0xFFFFD700)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { showDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFD700),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Pay with M-Pesa",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Flavour not found",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
        }
    }

    // Confirmation Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = "Confirm Payment",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Proceed to pay KES ${flavour?.price} for ${flavour?.name}?",
                    color = Color.White
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val userId = auth.currentUser?.uid
                                if (userId == null) {
                                    snackbarHostState.showSnackbar("Please log in to place an order")
                                    navController.navigate("login")
                                    return@launch
                                }
                                val orderId = db.child("orders").push().key ?: return@launch
                                val order = Order(
                                    id = orderId,
                                    flavourId = flavourId,
                                    userId = userId,
                                    amount = flavour?.price ?: 0.0,
                                    status = "Pending",
                                    timestamp = Instant.now().toString()
                                )
                                // Use await() to handle Firebase Task in a suspending manner
                                db.child("orders").child(orderId).setValue(order).await()
                                snackbarHostState.showSnackbar("Payment initiated and order created!")
                                showDialog = false
                                navController.navigate("delivery_tracking/$flavourId")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Failed to create order: ${e.message}")
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFFFD700)
                    )
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = Color(0xFF1C2526),
            shape = RoundedCornerShape(12.dp)
        )
    }
}