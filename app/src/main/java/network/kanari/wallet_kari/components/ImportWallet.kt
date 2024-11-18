package network.kanari.wallet_kari.components

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import network.kanari.wallet_kari.ui.theme.WalletkariTheme

@Composable
fun ImportWallet(navController: NavController) {
    var wordCount by remember { mutableStateOf(12) }
    var seedPhrase by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Import Wallet", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = wordCount == 12,
                onClick = { wordCount = 12 },
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onSurface,
                    disabledSelectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    disabledUnselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            )
            Text(text = "12 words")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = wordCount == 24,
                onClick = { wordCount = 24 },
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onSurface,
                    disabledSelectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    disabledUnselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            )
            Text(text = "24 words")
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = seedPhrase,
            onValueChange = { seedPhrase = it },
            label = { Text("Seed Phrase") },
            isError = showError
        )
        if (showError) {
            Text(
                text = "Invalid seed phrase",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val words = seedPhrase.trim().split("\\s+".toRegex())
            showError = words.size != wordCount
            if (!showError) {
                val sharedPreferences = context.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putString("mnemonic", seedPhrase)
                    apply()
                }
                navController.navigate("home_screen")
            }
        }) {
            Text(text = "Import Wallet")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
                navController.navigate("create_wallet")
        }) {
            Text(text = "Create Wallet")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES,showBackground = true)
@Composable
fun LoginScreenPreview() {
    WalletkariTheme{
        ImportWallet(
            navController = rememberNavController(),
        )
    }
}