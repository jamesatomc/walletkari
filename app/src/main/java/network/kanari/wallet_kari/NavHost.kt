import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import network.kanari.wallet_kari.components.CreateWallet


@Composable
fun AppNavHost(navController: NavHostController, context: Context) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE)
    val walletCreated = remember { sharedPreferences.contains("wallet_address") }

    NavHost(
        navController = navController,
        startDestination = if (walletCreated) "display_wallet_address" else "create_wallet"
    ) {
        composable("create_wallet") {
            CreateWallet(navController)
        }
        composable("display_seed_phrase/{seedPhrase}") { backStackEntry ->
            val seedPhrase = backStackEntry.arguments?.getString("seedPhrase") ?: ""
            DisplaySeedPhrase(seedPhrase, navController)
        }
        composable("display_wallet_address") {
            val walletAddress = sharedPreferences.getString("wallet_address", "") ?: ""
            DisplayWalletAddress(walletAddress, context)
        }
    }
}