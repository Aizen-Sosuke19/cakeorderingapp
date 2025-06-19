package com.example.cakeorderingapp

import androidx.compose.foundation.clickable
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun FlavoursScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val coroutineScope = rememberCoroutineScope()
    var flavours by remember { mutableStateOf<List<String>>(emptyList()) }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            navController.navigate("login") {
                popUpTo("flavours") { inclusive = true }
            }
        } else {
            coroutineScope.launch {
                try {
                    val result = db.collection("cakes").get().await()
                    flavours = result.documents
                        .mapNotNull { it.getString("flavour") }
                        .distinct()
                        .sorted()
                } catch (e: Exception) {
                    error = e.message ?: "Failed to load flavours"
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
        Text("Cake Flavours", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        } else if (flavours.isEmpty()) {
            Text("No flavours available", style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyColumn {
                items(flavours) { flavour ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { navController.navigate("cake_list/$flavour") }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(flavour, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}