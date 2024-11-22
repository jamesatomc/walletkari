package network.kanari.wallet_kari.components.widget

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController


@Composable
fun AppDrawer(
    navController: NavHostController,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE)
    val password = sharedPreferences.getString("password", "") ?: ""
    var inputPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .width(320.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NavigationDrawerItem(
                label = { Text(text = "Wallet") },
                selected = false,
                onClick = { navController.navigate("home_screen") }
            )
            NavigationDrawerItem(
                label = { Text(text = "ICO") },
                selected = false,
                onClick = { navController.navigate("ico") }
            )
            NavigationDrawerItem(
                label = { Text(text = "Settings") },
                selected = false,
                onClick = { navController.navigate("settings") }
            )
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    showDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Text("Logout")
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Enter Password") },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputPassword,
                        onValueChange = { inputPassword = it },
                        label = { Text("Password") },
                        isError = passwordError
                    )
                    if (passwordError) {
                        Text(
                            text = "Incorrect password",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (inputPassword == password) {
                        with(sharedPreferences.edit()) {
                            clear()
                            apply()
                        }
                        navController.navigate("import_wallet") {
                            popUpTo("home_screen") { inclusive = true }
                        }
                    } else {
                        passwordError = true
                    }
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}