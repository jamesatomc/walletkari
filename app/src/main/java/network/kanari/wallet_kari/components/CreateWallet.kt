package network.kanari.wallet_kari.components

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.bitcoinj.crypto.MnemonicCode
import java.security.SecureRandom

@Composable
fun CreateWallet(navController: NavController) {
    var walletName by remember { mutableStateOf("") }
    var walletPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPasswordMismatchError by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Create New Wallet", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = walletName,
            onValueChange = { walletName = it },
            label = { Text("Wallet Name") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = walletPassword,
            onValueChange = { walletPassword = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                showPasswordMismatchError = walletPassword != confirmPassword
            },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = showPasswordMismatchError
        )
        if (showPasswordMismatchError) {
            Text(
                text = "Passwords do not match",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (!showPasswordMismatchError) {
                val seedPhrase = generateSeedPhrase()
                saveSeedPhrase(context, seedPhrase)
                val walletAddress = "your_generated_wallet_address" // Replace with actual wallet address generation logic
                saveWalletAddress(context, walletAddress)
                navController.navigate("display_wallet_address")
            }
        }) {
            Text(text = "Create Wallet")
        }
    }
}

fun generateSeedPhrase(): String {
    val entropy = ByteArray(32) // 32 bytes for 24 words
    SecureRandom().nextBytes(entropy)
    val mnemonicCode = MnemonicCode.INSTANCE
    val seedPhrase = mnemonicCode.toMnemonic(entropy)
    return seedPhrase.joinToString(" ")
}

fun saveSeedPhrase(context: Context, seedPhrase: String) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString("seed_phrase", seedPhrase)
    editor.apply()
}

fun saveWalletAddress(context: Context, walletAddress: String) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString("wallet_address", walletAddress)
    editor.apply()
}