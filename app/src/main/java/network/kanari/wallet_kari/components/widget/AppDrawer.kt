package network.kanari.wallet_kari.components.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController


@Composable
fun AppDrawer(
    navController: NavHostController,
    showDialog: Boolean,
    onLogoutClick: () -> Unit
) {
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
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Text("Logout")
            }
        }
    }
}