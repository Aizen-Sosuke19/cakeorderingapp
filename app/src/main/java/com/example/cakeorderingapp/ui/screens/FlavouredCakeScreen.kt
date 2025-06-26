package com.example.cakeorderingapp.ui.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.cakeorderingapp.ui.data.Cake
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun FlavouredCakeScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()
    var cakes by remember { mutableStateOf<List<Cake>>(emptyList()) }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            navController.navigate("login") {
                popUpTo("flavored_cakes") { inclusive = true }
            }
        } else {
            coroutineScope.launch {
                try {
                    val result = db.collection("cakes")
                        .whereIn("flavour", listOf("Chocolate", "Vanilla", "Strawberry"))
                        .get()
                        .await()
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
                    error = e.message ?: "Failed to load cakes"
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
        Text("Flavored Cakes", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        } else if (cakes.isEmpty()) {
            Text("No cakes available", style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cakes) { cake ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("order_form/${cake.id}") }
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = cake.imageUrl.ifEmpty { "https://via.placeholder.com/150" },
                                contentDescription = "Image of ${cake.name}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(cake.name, style = MaterialTheme.typography.titleSmall)
                            Text("Flavour: ${cake.flavour}", style = MaterialTheme.typography.bodySmall)
                            Text("KSh ${cake.price}", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { navController.navigate("order_form/${cake.id}") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Order")
                            }
                        }
                    }
                }
            }
        }
    }
}