package com.example.cakeorderingapp.ui.screens

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CakeFlavour(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val placeName: String = "",
    val ownerId: String = "",
    val price: Double = 0.0
)

class FlavourViewModel : ViewModel() {
    private val db = Firebase.database.reference
    private val auth = FirebaseAuth.getInstance()
    private val _flavours = MutableStateFlow<List<CakeFlavour>>(emptyList())
    val flavours: StateFlow<List<CakeFlavour>> = _flavours
    internal val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun addFlavour(flavour: CakeFlavour) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "Please log in to add a flavour"
            return
        }
        val flavourId = db.child("flavours").push().key ?: return
        val newFlavour = flavour.copy(id = flavourId, ownerId = userId)
        db.child("flavours").child(flavourId).setValue(newFlavour)
            .addOnSuccessListener { fetchFlavours() }
            .addOnFailureListener { e ->
                _errorMessage.value = "Failed to add flavour: ${e.message}"
            }
    }

    fun deleteFlavour(flavourId: String) {
        if (auth.currentUser?.uid == null) {
            _errorMessage.value = "Please log in to delete a flavour"
            return
        }
        db.child("flavours").child(flavourId).removeValue()
            .addOnSuccessListener { fetchFlavours() }
            .addOnFailureListener { e ->
                _errorMessage.value = "Failed to delete flavour: ${e.message}"
            }
    }

    fun fetchFlavours() {
        println("Fetching flavours...")
        db.child("flavours").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                println("Fetched ${snapshot.childrenCount} flavours: ${snapshot.value}")
                val flavoursList = snapshot.children.mapNotNull { it.getValue(CakeFlavour::class.java) }
                _flavours.value = flavoursList
            }

            override fun onCancelled(error: DatabaseError) {
                println("Fetch flavours error: ${error.message}, code: ${error.code}")
                _errorMessage.value = "Failed to fetch flavours: ${error.message}"
            }
        })
    }
}

@Composable
fun FlavourScreen(
    navController: NavController,
    viewModel: FlavourViewModel = viewModel(),
    flavourId: String
) {
    val flavours by viewModel.flavours.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var flavourName by remember { mutableStateOf(TextFieldValue()) }
    var description by remember { mutableStateOf(TextFieldValue()) }
    var placeName by remember { mutableStateOf(TextFieldValue()) }
    var price by remember { mutableStateOf(TextFieldValue()) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
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
                                ownerId = "",
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
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
                                Button(
                                    onClick = { showDeleteDialog = flavour.id },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Red,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    showDeleteDialog?.let { flavourId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = {
                Text(
                    text = "Delete Flavour",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this flavour?",
                    color = Color.White
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFlavour(flavourId)
                        showDeleteDialog = null
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
                    onClick = { showDeleteDialog = null },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = Color(0xFF1C2526),
            shape = MaterialTheme.shapes.medium
        )
    }
}