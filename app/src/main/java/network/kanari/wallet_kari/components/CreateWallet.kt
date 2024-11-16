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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWallet(navController: NavController) {
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var mnemonic by remember { mutableStateOf("") }
    var showMnemonic by remember { mutableStateOf(false) }
    var mnemonicLength by remember { mutableStateOf(12) }
    var expanded by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Enter Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = if (mnemonicLength == 12) "12 words" else "24 words",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mnemonic Length") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor() // Ensure the dropdown menu is anchored to the text field
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("12 words") },
                            onClick = {
                                mnemonicLength = 12
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("24 words") },
                            onClick = {
                                mnemonicLength = 24
                                expanded = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        mnemonic = generateMnemonic(mnemonicLength)
                        showMnemonic = true
                    },
                    enabled = password.isNotEmpty() // Enable button only if password is not empty
                ) {
                    Text("Create Wallet")
                }
            }
        }
        if (showMnemonic) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Mnemonic: $mnemonic", modifier = Modifier.padding(top = 16.dp))
                    Row {
                        Button(onClick = {
                            saveWallet(context, mnemonic, password)
                            navController.navigate("home_screen")
                        }) {
                            Text("Save Wallet")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(onClick = {
                            clipboardManager.setText(AnnotatedString(mnemonic))
                        }) {
                            Text("Copy Mnemonic")
                        }
                    }
                }
            }
        }
    }
}

fun generateMnemonic(wordCount: Int): String {
    val secureRandom = SecureRandom()
    val entropy = ByteArray(wordCount / 3 * 4)
    secureRandom.nextBytes(entropy)
    return MnemonicCode.INSTANCE.toMnemonic(entropy).joinToString(" ")
}

fun saveWallet(context: Context, mnemonic: String, password: String) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putString("mnemonic", mnemonic)
        putString("password", password)
        apply()
    }
}