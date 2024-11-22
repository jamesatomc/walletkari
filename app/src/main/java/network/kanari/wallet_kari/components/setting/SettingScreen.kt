package network.kanari.wallet_kari.components.setting

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController



@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingScreen(navController: NavController) {
   Scaffold {
       val context = LocalContext.current
       val sharedPreferences: SharedPreferences = context.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE)
       val mnemonic = sharedPreferences.getString("mnemonic", "") ?: ""
       val password = sharedPreferences.getString("password", "") ?: ""
       var showSeedPhrase by remember { mutableStateOf(false) }
       var showPasswordDialog by remember { mutableStateOf(false) }
       var passwordError by remember { mutableStateOf(false) }
       var inputPassword by remember { mutableStateOf("") }

       Column(
           modifier = Modifier
               .fillMaxSize()
               .padding(16.dp),
           verticalArrangement = Arrangement.Center,
           horizontalAlignment = Alignment.CenterHorizontally
       ) {
           Text(
               text = "Seed Phrase",
               style = MaterialTheme.typography.titleLarge,
               modifier = Modifier.padding(bottom = 16.dp)
           )
           ClickableText(
               text = AnnotatedString(if (showSeedPhrase) mnemonic else "Click to view"),
               style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline),
               onClick = {
                   showPasswordDialog = true
               }
           )

           if (showPasswordDialog) {
               AlertDialog(
                   onDismissRequest = { showPasswordDialog = false },
                   title = { Text("Enter Password") },
                   text = {
                       Column {
                           OutlinedTextField(
                               value = inputPassword,
                               onValueChange = { inputPassword = it },
                               label = { Text("Password") },
                               isError = passwordError,
                               visualTransformation = PasswordVisualTransformation(),
                               modifier = Modifier.fillMaxWidth()
                           )
                           if (passwordError) {
                               Text(
                                   text = "Incorrect password",
                                   color = MaterialTheme.colorScheme.error,
                                   style = MaterialTheme.typography.bodySmall,
                                   modifier = Modifier.padding(top = 8.dp)
                               )
                           }
                       }
                   },
                   confirmButton = {
                       TextButton(onClick = {
                           if (inputPassword == password) {
                               showSeedPhrase = true
                               showPasswordDialog = false
                           } else {
                               passwordError = true
                           }
                       }) {
                           Text("Confirm")
                       }
                   },
                   dismissButton = {
                       TextButton(onClick = { showPasswordDialog = false }) {
                           Text("Cancel")
                       }
                   }
               )
           }
       }
   }
}

@Preview(showBackground = true)
@Composable
fun SettingScreenPreview() {
    SettingScreen(
        navController = rememberNavController(),
    )
}