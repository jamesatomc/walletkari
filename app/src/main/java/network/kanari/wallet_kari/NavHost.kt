import android.content.Context

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import network.kanari.wallet_kari.components.CreateWallet
import network.kanari.wallet_kari.components.HomeScreen


@Composable
fun AppNavHost(navController: NavHostController, context: Context) {
    NavHost(
        navController = navController,
        startDestination = "create_wallet"
    ) {
        composable("create_wallet") {
            CreateWallet(navController)
        }
        composable("home_screen") {
            HomeScreen(navController)
        }
    }
}