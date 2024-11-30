package network.kanari.wallet_kari.components

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.bitcoinj.crypto.MnemonicCode
import java.security.SecureRandom
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import network.kanari.wallet_kari.components.widget.CustomTextField

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
    var passwordMismatchError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.inverseOnSurface,),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CustomTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordMismatchError = false
                    },
                    label = "Enter Password",
                    placeholder = "Password",
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = passwordMismatchError
                )

                Spacer(modifier = Modifier.height(16.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
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
                            .padding(horizontal = 16.dp)
                            .border(
                                BorderStroke(
                                    color = Color.Transparent,
                                    width = 1.dp
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .shadow(elevation = 8.dp, shape = RoundedCornerShape(10.dp)),
                        colors = ExposedDropdownMenuDefaults.textFieldColors(
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                        ),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },

                    ) {
                        DropdownMenuItem(
                            text = { Text("12 words") },
                            onClick = {
                                mnemonicLength = 12
                                expanded = false
                            },
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
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    navController.navigate("import_wallet")
                }) {
                    Text(text = "import_wallet")
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