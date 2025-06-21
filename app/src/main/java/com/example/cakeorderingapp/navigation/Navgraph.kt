package com.example.cakeorderingapp.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cakeorderingapp.screens.DashboardScreen
import com.example.cakeorderingapp.screens.DeliveryTrackingScreen
import com.example.cakeorderingapp.screens.ForgotPasswordScreen
import com.example.cakeorderingapp.screens.LoginScreen
import com.example.cakeorderingapp.screens.SignUpScreen
import com.example.cakeorderingapp.screens.OrderFormScreen
import com.example.cakeorderingapp.screens.CakeSelectionScreen
import com.example.cakeorderingapp.screens.FlavoursScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("sign_up") {
            SignUpScreen(navController = navController)
        }
        composable("forgot_password") {
            ForgotPasswordScreen(navController = navController)
        }
        composable("dashboard") {
            DashboardScreen(navController = navController)
        }
        composable("cake_selection") {
            CakeSelectionScreen(navController = navController)
        }
        composable("cake_details/{cakeId}") { backStackEntry ->
            // Placeholder: Implement CakeDetailsScreen if needed
            Text("Cake Details: ${backStackEntry.arguments?.getString("cakeId")}")
        }
        composable("order_form/{cakeId}") { backStackEntry ->
            val cakeId = backStackEntry.arguments?.getString("cakeId") ?: ""
            OrderFormScreen(navController = navController, cakeId = cakeId, selectedLocation = null)
        }
        composable("delivery_tracking") {
            DeliveryTrackingScreen(navController = navController)
        }
        composable("flavour_screen") {
            FlavoursScreen(navController = navController)
        }
    }
}

