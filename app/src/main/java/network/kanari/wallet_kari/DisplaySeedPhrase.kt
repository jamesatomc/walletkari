import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun DisplaySeedPhrase(seedPhrase: String, navController: NavController) {
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Your Seed Phrase", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        ClickableText(
            text = AnnotatedString(seedPhrase),
            onClick = {
                clipboardManager.setText(AnnotatedString(seedPhrase))
            },
            style = LocalTextStyle.current.copy(textDecoration = TextDecoration.Underline)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { clipboardManager.setText(AnnotatedString(seedPhrase)) }) {
            Text(text = "Copy Seed Phrase")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val walletAddress = "your_generated_wallet_address" // Replace with actual wallet address generation logic
            navController.navigate("display_wallet_address/$walletAddress")
        }) {
            Text(text = "Next")
        }
    }
}