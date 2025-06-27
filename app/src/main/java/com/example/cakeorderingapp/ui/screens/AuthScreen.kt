package com.example.cakeorderingapp.ui.screens


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
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()
    val coroutineScope = rememberCoroutineScope()

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
            onValueChange = { email = it.trim() },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = error.isNotEmpty()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = error.isNotEmpty()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    error = "Email and password cannot be empty"
                    return@Button
                }
                if (!isValidEmail(email)) {
                    error = "Invalid email format"
                    return@Button
                }
                isLoading = true
                coroutineScope.launch {
                    try {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful && auth.currentUser != null) {
                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                    email = ""
                                    password = ""
                                    error = ""
                                } else {
                                    error = when (val exception = task.exception) {
                                        is FirebaseAuthInvalidCredentialsException -> "Invalid email or password"
                                        is FirebaseAuthInvalidUserException -> "No account found with this email"
                                        else -> exception?.message ?: "Login failed"
                                    }
                                    Log.e("Login", "Error: ${task.exception?.message}", task.exception)
                                }
                            }
                    } catch (e: Exception) {
                        isLoading = false
                        error = "Unexpected error: ${e.message}"
                        Log.e("Login", "Unexpected error: ${e.message}", e)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Login")
            }
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
    var isLoading by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()
    val coroutineScope = rememberCoroutineScope()

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
            onValueChange = { email = it.trim() },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = error.isNotEmpty()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = error.isNotEmpty()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = error.isNotEmpty()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    error = "All fields are required"
                    return@Button
                }
                if (!isValidEmail(email)) {
                    error = "Invalid email format"
                    return@Button
                }
                if (password != confirmPassword) {
                    error = "Passwords do not match"
                    return@Button
                }
                if (password.length < 6) {
                    error = "Password must be at least 6 characters"
                    return@Button
                }
                isLoading = true
                coroutineScope.launch {
                    try {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful && auth.currentUser != null) {
                                    navController.navigate("dashboard") {
                                        popUpTo("sign_up") { inclusive = true }
                                    }
                                    email = ""
                                    password = ""
                                    confirmPassword = ""
                                    error = ""
                                } else {
                                    error = when (val exception = task.exception) {
                                        is FirebaseAuthWeakPasswordException -> "Password is too weak"
                                        is FirebaseAuthInvalidCredentialsException -> "Invalid email format"
                                        is FirebaseAuthUserCollisionException -> "Email already in use"
                                        else -> exception?.message ?: "Registration failed"
                                    }
                                    Log.e("SignUp", "Error: ${task.exception?.message}", task.exception)
                                }
                            }
                    } catch (e: Exception) {
                        isLoading = false
                        error = "Unexpected error: ${e.message}"
                        Log.e("SignUp", "Unexpected error: ${e.message}", e)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Sign Up")
            }
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
    var isLoading by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()
    val coroutineScope = rememberCoroutineScope()

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
            onValueChange = { email = it.trim() },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = error.isNotEmpty()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isBlank()) {
                    error = "Please enter your email"
                    successMessage = ""
                    return@Button
                }
                if (!isValidEmail(email)) {
                    error = "Invalid email format"
                    successMessage = ""
                    return@Button
                }
                isLoading = true
                coroutineScope.launch {
                    try {
                        auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    successMessage = "Password reset email sent. Check your inbox or spam folder."
                                    error = ""
                                    email = ""
                                } else {
                                    error = when (val exception = task.exception) {
                                        is FirebaseAuthInvalidUserException -> "No account found with this email"
                                        is FirebaseAuthInvalidCredentialsException -> "Invalid email format"
                                        else -> exception?.message ?: "Failed to send reset email"
                                    }
                                    successMessage = ""
                                    Log.e("ForgotPassword", "Error: ${task.exception?.message}", task.exception)
                                }
                            }
                    } catch (e: Exception) {
                        isLoading = false
                        error = "Unexpected error: ${e.message}"
                        successMessage = ""
                        Log.e("ForgotPassword", "Unexpected error: ${e.message}", e)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Send Reset Email")
            }
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
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}