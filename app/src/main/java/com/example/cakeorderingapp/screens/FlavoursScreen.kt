package com.example.cakeorderingapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.cakeorderingapp.data.CakeFlavour
import com.example.cakeorderingapp.viewmodel.FlavourViewModel

@Composable
fun FlavourScreen(flavourId: String?,navController: NavController, viewModel: FlavourViewModel = viewModel()) {
    val flavours by viewModel.flavours.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var flavourName by remember { mutableStateOf(TextFieldValue()) }
    var description by remember { mutableStateOf(TextFieldValue()) }
    var placeName by remember { mutableStateOf(TextFieldValue()) }
    var price by remember { mutableStateOf(TextFieldValue()) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.fetchFlavours()
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
        contentColor = Color(0xFFFFD700) // Gold
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Add Cake Flavour",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFFFD700)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = flavourName,
                onValueChange = { flavourName = it },
                label = { Text("Flavour Name", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color.White,
                    focusedLabelColor = Color(0xFFFFD700),
                    unfocusedLabelColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color.White,
                    focusedLabelColor = Color(0xFFFFD700),
                    unfocusedLabelColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = placeName,
                onValueChange = { placeName = it },
                label = { Text("Place Name", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color.White,
                    focusedLabelColor = Color(0xFFFFD700),
                    unfocusedLabelColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price (KES)", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color.White,
                    focusedLabelColor = Color(0xFFFFD700),
                    unfocusedLabelColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (flavourName.text.isNotBlank() && placeName.text.isNotBlank() && price.text.isNotBlank()) {
                        val priceValue = price.text.toDoubleOrNull() ?: 0.0
                        viewModel.addFlavour(
                            CakeFlavour(
                                name = flavourName.text,
                                description = description.text,
                                placeName = placeName.text,
                                ownerId = "current_user_id", // Replace with Firebase Auth user ID
                                price = priceValue
                            )
                        )
                        flavourName = TextFieldValue()
                        description = TextFieldValue()
                        placeName = TextFieldValue()
                        price = TextFieldValue()
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Please fill all required fields")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color.Black
                )
            ) {
                Text("Add Flavour")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Available Flavours",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFFFFD700)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(flavours) { flavour ->
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
                                text = flavour.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFFFD700)
                            )
                            Text(
                                text = flavour.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Place: ${flavour.placeName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Price: KES ${flavour.price}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    navController.navigate("purchase/${flavour.id}")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFD700),
                                    contentColor = Color.Black
                                )
                            ) {
                                Text("Buy Now")
                            }
                        }
                    }
                }
            }
        }
    }
}