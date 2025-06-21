package com.example.cakeorderingapp.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    if (!isValidEmail(email)) {
                        error = "Invalid email format"
                        return@Button
                    }
                    try {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                if (auth.currentUser != null) {
                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    error = "User not authenticated"
                                    Log.e("Login", "User is null after successful login")
                                }
                            }
                            .addOnFailureListener { exception ->
                                error = when (exception) {
                                    is FirebaseAuthInvalidCredentialsException -> "Invalid email or password"
                                    is FirebaseAuthInvalidUserException -> "No account found with this email"
                                    else -> exception.message ?: "Login failed"
                                }
                                Log.e("Login", "Error: ${exception.message}", exception)
                            }
                    } catch (e: Exception) {
                        error = "Unexpected error: ${e.message}"
                        Log.e("Login", "Unexpected error: ${e.message}", e)
                    }
                } else {
                    error = "Email and password cannot be empty"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { navController.navigate("sign_up") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Don't have an account? Sign Up")
        }
        TextButton(
            onClick = { navController.navigate("forgot_password") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Forgot Password?")
        }

        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun SignUpScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Sign Up", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank() && password == confirmPassword) {
                    if (!isValidEmail(email)) {
                        error = "Invalid email format"
                        return@Button
                    }
                    if (password.length < 6) {
                        error = "Password must be at least 6 characters"
                        return@Button
                    }
                    try {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                if (auth.currentUser != null) {
                                    navController.navigate("dashboard") {
                                        popUpTo("sign_up") { inclusive = true }
                                    }
                                } else {
                                    error = "User not authenticated"
                                    Log.e("SignUp", "User is null after successful signup")
                                }
                            }
                            .addOnFailureListener { exception ->
                                error = when (exception) {
                                    is FirebaseAuthWeakPasswordException -> "Password is too weak"
                                    is FirebaseAuthInvalidCredentialsException -> "Invalid email format"
                                    is FirebaseAuthUserCollisionException -> "Email already in use"
                                    else -> exception.message ?: "Registration failed"
                                }
                                Log.e("SignUp", "Error: ${exception.message}", exception)
                            }
                    } catch (e: Exception) {
                        error = "Unexpected error: ${e.message}"
                        Log.e("SignUp", "Unexpected error: ${e.message}", e)
                    }
                } else {
                    error = "Passwords do not match or fields are empty"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }
        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { navController.navigate("login") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Already have an account? Login")
        }

        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun ForgotPasswordScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Forgot Password", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isNotBlank()) {
                    if (!isValidEmail(email)) {
                        error = "Invalid email format"
                        return@Button
                    }
                    try {
                        auth.sendPasswordResetEmail(email)
                            .addOnSuccessListener {
                                successMessage = "Password reset email sent. Check your inbox."
                                error = ""
                            }
                            .addOnFailureListener { exception ->
                                error = when (exception) {
                                    is FirebaseAuthInvalidUserException -> "No account found with this email"
                                    else -> exception.message ?: "Failed to send reset email"
                                }
                                successMessage = ""
                                Log.e("ForgotPassword", "Error: ${exception.message}", exception)
                            }
                    } catch (e: Exception) {
                        error = "Unexpected error: ${e.message}"
                        Log.e("ForgotPassword", "Unexpected error: ${e.message}", e)
                    }
                } else {
                    error = "Please enter your email"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send Reset Email")
        }
        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { navController.navigate("login") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Login")
        }

        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        if (successMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(successMessage, color = MaterialTheme.colorScheme.primary)
        }
    }
}

fun isValidEmail(email: String): Boolean {
    return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex())
}