package com.example.cakeorderingapp.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cakeorderingapp.ui.screens.CakeSelectionScreen
import com.example.cakeorderingapp.ui.screens.DashboardScreen
import com.example.cakeorderingapp.ui.screens.DeliveryTrackingScreen
import com.example.cakeorderingapp.ui.screens.FlavourScreen
import com.example.cakeorderingapp.ui.screens.ForgotPasswordScreen
import com.example.cakeorderingapp.ui.screens.LoginScreen
import com.example.cakeorderingapp.ui.screens.OrderFormScreen
import com.example.cakeorderingapp.ui.screens.PurchaseScreen
import com.example.cakeorderingapp.ui.screens.SignUpScreen
import com.example.cakeorderingapp.ui.screens.SplashScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController = navController)
        }
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
        composable("flavour") {
            FlavourScreen(
                navController = navController,
                flavourId = "",
            )
        }
        composable("purchase/{flavourId}") { backStackEntry ->
            val flavourId = backStackEntry.arguments?.getString("flavourId") ?: ""
            PurchaseScreen(flavourId = flavourId, navController = navController)
        }
        composable("delivery_tracking") {
            DeliveryTrackingScreen(navController = navController)
        }
        composable("cake_details/{cakeId}") { backStackEntry ->
            val cakeId = backStackEntry.arguments?.getString("cakeId") ?: ""
            Text(
                text = "Cake Details: $cakeId",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
        composable("order_form/{cakeId}") { backStackEntry ->
            val cakeId = backStackEntry.arguments?.getString("cakeId") ?: ""
            OrderFormScreen(navController = navController, cakeId = cakeId, selectedLocation = null)
        }
    }
}