import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun DisplayWalletAddress(walletAddress: String, context: Context) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE)
    val seedPhrase = sharedPreferences.getString("seed_phrase", "") ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Your Bitcoin Wallet Address", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = walletAddress, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* Handle wallet address actions */ }) {
            Text(text = "Transfer Funds")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* Export seed phrase logic */ }) {
            Text(text = "Export Seed Phrase")
        }
    }
}