package com.example.cakeorderingapp


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") { LoginScreen(navController) }
        composable("dashboard") { DashboardScreen(navController) }
        composable("cake_list") { CakeListScreen(navController, null) }
        composable("cake_selection") { CakeSelectionScreen(navController) }
        composable("delivery_setup/{cakeId}") { backStackEntry ->
            DeliverySetupScreen(navController, backStackEntry.arguments?.getString("cakeId"))
        }
        composable("delivery_tracking") { DeliveryTrackingScreen(navController) }
        composable("flavour_screen") { FlavourScreen(navController) }
        composable("cake_details/{cakeId}") { backStackEntry ->
            CakeDetailsScreen(navController, backStackEntry.arguments?.getString("cakeId"))
        }
        composable("delivery_selection/{cakeId}") { backStackEntry ->
            DeliverySelectionScreen(navController, backStackEntry.arguments?.getString("cakeId"))
        }
        composable("order_form/{cakeId}/{selectedLocation}") { backStackEntry ->
            OrderFormScreen(navController,
                backStackEntry.arguments?.getString("cakeId"),
                backStackEntry.arguments?.getString("selectedLocation"))
        }
        composable("admin_cake_screen") { AdminCakeScreen(navController) }
    }
}
