package network.kanari.wallet_kari.components.home


import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

import network.kanari.wallet_kari.components.widget.AppDrawer
import network.kanari.wallet_kari.components.widget.BitcoinAddressCard

import org.bitcoinj.core.Coin



@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE)
    val mnemonic = sharedPreferences.getString("mnemonic", "") ?: ""
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var recipientAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var fee by remember { mutableStateOf("") }
    var transactionResult by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var selectedFeeOption by remember { mutableStateOf("Low") }
    var selectedNetwork by remember { mutableStateOf(sharedPreferences.getString("selected_network", "Testnet3") ?: "Testnet3") }
    var expanded by remember { mutableStateOf(false) }
    val addressTypeOptions = listOf("P2WPKH", "P2SH-P2WPKH", "P2PKH")
    var selectedAddressType by remember { mutableStateOf(addressTypeOptions[0]) }
    var maxBalance by remember { mutableStateOf("0.0") }

    // Generate BTC addresses from the mnemonic
    val nativeSegwitAddress = try {
        generateAddress(mnemonic, "P2WPKH", selectedNetwork)
    } catch (e: Exception) {
        Log.e("HomeScreen", "Error generating native Segwit address", e)
        "Error generating address"
    }
    val nativeSegwitP2SHAddress = try {
        generateAddress(mnemonic, "P2SH-P2WPKH", selectedNetwork)
    } catch (e: Exception) {
        Log.e("HomeScreen", "Error generating native Segwit P2SH address", e)
        "Error generating address"
    }
    val legacyAddress = try {
        generateAddress(mnemonic, "P2PKH", selectedNetwork)
    } catch (e: Exception) {
        Log.e("HomeScreen", "Error generating legacy address", e)
        "Error generating address"
    }

    var nativeSegwitBalance by remember { mutableStateOf("Loading...") }
    var nativeSegwitP2SHBalance by remember { mutableStateOf("Loading...") }
    var legacyBalance by remember { mutableStateOf("Loading...") }

    LaunchedEffect(selectedNetwork) {
        nativeSegwitBalance = withContext(Dispatchers.IO) { fetchBalance(nativeSegwitAddress, selectedNetwork) }
        nativeSegwitP2SHBalance = withContext(Dispatchers.IO) { fetchBalance(nativeSegwitP2SHAddress, selectedNetwork) }
        legacyBalance = withContext(Dispatchers.IO) { fetchBalance(legacyAddress, selectedNetwork) }

        // Check if any balance is not available
        val balances = listOf(nativeSegwitBalance, nativeSegwitP2SHBalance, legacyBalance)
        if (balances.all { it == "Error" || it == "Loading..." }) {
            maxBalance = "0.0"
        } else {
            // Calculate max balance
            maxBalance = balances
                .mapNotNull { it.toBigDecimalOrNull() }
                .maxOrNull()
                ?.toPlainString() ?: "0.0"
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                navController = navController,
                onLogoutClick = {
                    showDialog = true
                }
            )
        },
    ) {
        Scaffold(
            Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        Box {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    },
                    title = { Text("Wallet") },
                    actions = {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            TextField(
                                value = selectedNetwork,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Network") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor() // Ensure the dropdown menu is anchored to the text field
                                    .width(180.dp)
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
                                onDismissRequest = { expanded = false }
                            ) {
                                listOf("Mainnet", "Testnet3", "Testnet4").forEach { network ->
                                    DropdownMenuItem(
                                        text = { Text(network) },
                                        onClick = {
                                            selectedNetwork = network
                                            expanded = false
                                            // Save the selected network to SharedPreferences
                                            with(sharedPreferences.edit()) {
                                                putString("selected_network", network)
                                                apply()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,

            ) {
                Spacer(modifier = Modifier.height(70.dp))

                Row(
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val expanded = remember { mutableStateOf(false) }

                    BitcoinAddressCard(
                        title = "Native Segway (P2WPKH)",
                        address = nativeSegwitAddress,
                        balance = nativeSegwitBalance,
                        expanded = expanded.value,
                        onCardClick = { expanded.value = !expanded.value },
                        onAddressClick = { clipboardManager.setText(AnnotatedString(nativeSegwitAddress)) }
                    )

                    BitcoinAddressCard(
                        title = "Native Segwit (P2SH-P2WPKH)",
                        address = nativeSegwitP2SHAddress,
                        balance = nativeSegwitP2SHBalance,
                        expanded = expanded.value,
                        onCardClick = { expanded.value = !expanded.value },
                        onAddressClick = { clipboardManager.setText(AnnotatedString(nativeSegwitP2SHAddress)) }
                    )

                    BitcoinAddressCard(
                        title = "Legacy (P2PKH)",
                        address = legacyAddress,
                        balance = legacyBalance,
                        expanded = expanded.value,
                        onCardClick = { expanded.value = !expanded.value },
                        onAddressClick = { clipboardManager.setText(AnnotatedString(legacyAddress)) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(560.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .shadow(8.dp),
                ) {
                    SendBitcoinSection(
                        context = context,
                        recipientAddress = recipientAddress,
                        onRecipientAddressChange = { recipientAddress = it },
                        amount = amount,
                        onAmountChange = { amount = it },
                        fee = fee,
                        onFeeChange = { fee = it },
                        feeOptions = listOf("Low", "Medium", "High"),
                        selectedFeeOption = selectedFeeOption,
                        onSelectedFeeOptionChange = { selectedFeeOption = it },
                        transactionResult = transactionResult,
                        onSendClick = {
                            val amountCoin = Coin.parseCoin(amount)
                            val feePerKb = when (selectedFeeOption) {
                                "Low" -> Coin.valueOf(1000)
                                "Medium" -> Coin.valueOf(5000)
                                "High" -> Coin.valueOf(10000)
                                else -> Coin.valueOf(1000)
                            }
                            transactionResult = sendBitcoin(
                                mnemonic,
                                recipientAddress,
                                amountCoin,
                                feePerKb,
                                selectedNetwork,
                                selectedAddressType
                            )
                        },
                        addressTypeOptions = addressTypeOptions,
                        selectedAddressType = selectedAddressType,
                        onSelectedAddressTypeChange = { selectedAddressType = it },
                        maxBalance = maxBalance
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }   // Drawer content
}


@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES,showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController())
}