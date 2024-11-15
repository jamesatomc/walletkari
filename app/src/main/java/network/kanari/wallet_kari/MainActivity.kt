// MainActivity.kt
package network.kanari.wallet_kari

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import network.kanari.wallet_kari.ui.theme.WalletkariTheme
import org.bitcoinj.crypto.MnemonicCode
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WalletkariTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WalletScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun WalletScreen(modifier: Modifier = Modifier) {
    val wordList = MnemonicCode.INSTANCE.wordList
    var seedPhrase by remember { mutableStateOf("") }
    var seedPhraseAndAddress by remember { mutableStateOf(SeedPhraseAndAddress("", "")) }
    var isValidSeedPhrase by remember { mutableStateOf(true) }
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Bitcoin Wallet")
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = seedPhrase,
            onValueChange = {
                val words = it.trim().split("\\s+".toRegex())
                isValidSeedPhrase = (words.size == 12 || words.size == 24) && words.all { word -> word in wordList }
                if (isValidSeedPhrase) {
                    seedPhrase = it
                }
            },
            label = { Text("Enter Seed Phrase (optional)") },
            isError = !isValidSeedPhrase
        )
        if (!isValidSeedPhrase) {
            Text(text = "Seed phrase must be 12 or 24 words from the BIP-39 word list", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))
        ClickableText(
            text = AnnotatedString("Seed Phrase: ${seedPhraseAndAddress.seedPhrase}"),
            onClick = {
                clipboardManager.setText(AnnotatedString(seedPhraseAndAddress.seedPhrase))
            },
            style = LocalTextStyle.current.copy(textDecoration = TextDecoration.Underline)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Bitcoin Address: ${seedPhraseAndAddress.bitcoinAddress}")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { seedPhraseAndAddress = generateSeedPhraseAndAddress(seedPhrase.ifBlank { null }) }) {
            Text(text = "Generate Seed Phrase and Address")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WalletScreenPreview() {
    WalletkariTheme {
        WalletScreen()
    }
}