import android.content.Context

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import network.kanari.wallet_kari.components.CreateWallet
import network.kanari.wallet_kari.components.home.HomeScreen
import network.kanari.wallet_kari.components.ico.IcoScreen
import network.kanari.wallet_kari.components.ImportWallet
import network.kanari.wallet_kari.components.setting.SettingScreen


@Composable
fun AppNavHost(navController: NavHostController, context: Context) {
    val sharedPreferences = context.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE)
    val mnemonic = sharedPreferences.getString("mnemonic", "")

    val startDestination = if (mnemonic.isNullOrEmpty()) "create_wallet" else "home_screen"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("create_wallet") {
            CreateWallet(navController)
        }
        composable("home_screen") {
            HomeScreen(navController)
        }
        composable("import_wallet") {
            ImportWallet(navController)
        }
        composable("settings") {
            SettingScreen(navController)
        }
        composable("ico") {
            IcoScreen(navController)
        }
    }
}


