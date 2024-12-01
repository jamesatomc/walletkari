package network.kanari.wallet_kari.components.home


import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import network.kanari.wallet_kari.components.widget.AppDrawer
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.params.TestNet3Params

import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.wallet.DeterministicKeyChain
import org.bitcoinj.wallet.DeterministicSeed
import org.bitcoinj.wallet.SendRequest
import org.bitcoinj.wallet.Wallet
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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

    // Generate BTC addresses from the mnemonic
    val nativeSegwitAddress = try {
        generateAddress(mnemonic, "P2WPKH")
    } catch (e: Exception) {
        Log.e("HomeScreen", "Error generating native Segwit address", e)
        "Error generating address"
    }
    val nativeSegwitP2SHAddress = try {
        generateAddress(mnemonic, "P2SH-P2WPKH")
    } catch (e: Exception) {
        Log.e("HomeScreen", "Error generating native Segwit P2SH address", e)
        "Error generating address"
    }
    val legacyAddress = try {
        generateAddress(mnemonic, "P2PKH")
    } catch (e: Exception) {
        Log.e("HomeScreen", "Error generating legacy address", e)
        "Error generating address"
    }

    var nativeSegwitBalance by remember { mutableStateOf("Loading...") }
    var nativeSegwitP2SHBalance by remember { mutableStateOf("Loading...") }
    var taprootBalance by remember { mutableStateOf("Loading...") }
    var legacyBalance by remember { mutableStateOf("Loading...") }


    LaunchedEffect(Unit) {
        nativeSegwitBalance = withContext(Dispatchers.IO) { fetchBalance(nativeSegwitAddress) }
        nativeSegwitP2SHBalance = withContext(Dispatchers.IO) { fetchBalance(nativeSegwitP2SHAddress) }
        legacyBalance = withContext(Dispatchers.IO) { fetchBalance(legacyAddress) }
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
                    title = { Text("Wallet Kari") },
                    actions = {

                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Generated Bitcoin Addresses", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {

                    Card(
                        modifier = Modifier.padding(8.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Native Segway (P2WPKH)", style = MaterialTheme.typography.bodyMedium)
                            ClickableText(
                                text = AnnotatedString("Address: $nativeSegwitAddress"),
                                style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline),
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(nativeSegwitAddress))
                                }
                            )
                            Text(
                                "Balance: $nativeSegwitBalance BTC",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Card(
                        modifier = Modifier.padding(8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Native Segwit (P2SH-P2WPKH)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            ClickableText(
                                text = AnnotatedString("Address: $nativeSegwitP2SHAddress"),
                                style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline),
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(nativeSegwitP2SHAddress))
                                }
                            )
                            Text(
                                "Balance: $nativeSegwitP2SHBalance BTC",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Card(
                        modifier = Modifier.padding(8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Legacy (P2PKH)", style = MaterialTheme.typography.bodyMedium)
                            ClickableText(
                                text = AnnotatedString("Address: $legacyAddress"),
                                style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline),
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(legacyAddress))
                                }
                            )
                            Text(
                                "Balance: $legacyBalance BTC",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }



                Spacer(modifier = Modifier.height(8.dp))
                SendBitcoinSection(
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
                        transactionResult = sendBitcoin(mnemonic, recipientAddress, amountCoin, feePerKb)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }   // Drawer content
}


@SuppressLint("DefaultLocale")
suspend fun fetchBalance(address: String): String {
    val apiUrl = "https://blockstream.info/testnet/api/address/$address"
    return try {
        val url = URL(apiUrl)
        val connection = withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpURLConnection
        connection.requestMethod = "GET"
        withContext(Dispatchers.IO) {
            connection.connect()
        }

        val responseCode = connection.responseCode
        if (responseCode != 200) {
            throw RuntimeException("HttpResponseCode: $responseCode")
        } else {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()

            val jsonResponse = JSONObject(response.toString())
            val chainStats = jsonResponse.getJSONObject("chain_stats")
            val balance = chainStats.getLong("funded_txo_sum") - chainStats.getLong("spent_txo_sum")
            String.format("%.8f", balance / 1e8.toDouble()) // Convert satoshis to BTC and format to 8 decimal places
        }
    } catch (e: java.net.UnknownHostException) {
        "Error: Unable to resolve host. Please check your internet connection."
    } catch (e: java.net.SocketTimeoutException) {
        "Error: Connection timed out. Please try again later."
    } catch (e: java.io.IOException) {
        "Error: Network error occurred. Please try again."
    } catch (e: org.json.JSONException) {
        "Error: Failed to parse response. Please try again."
    } catch (e: Exception) {
        e.printStackTrace()
        "Error fetching balance: ${e.message}"
    }
}



fun generateAddress(mnemonic: String, addressType: String): String {
    val params = TestNet3Params.get()
    val seed = DeterministicSeed(mnemonic, null, "", 0L)
    val keyChain = DeterministicKeyChain.builder().seed(seed).build()
    val key: DeterministicKey = when (addressType) {
        "P2WPKH" -> keyChain.getKeyByPath(ImmutableList.of(ChildNumber(84, true), ChildNumber(1, true), ChildNumber(0, true), ChildNumber(0, false), ChildNumber(0, false)), true)
        "P2SH-P2WPKH" -> keyChain.getKeyByPath(ImmutableList.of(ChildNumber(49, true), ChildNumber(1, true), ChildNumber(0, true), ChildNumber(0, false), ChildNumber(0, false)), true)
        "P2PKH" -> keyChain.getKeyByPath(ImmutableList.of(ChildNumber(44, true), ChildNumber(1, true), ChildNumber(0, true), ChildNumber(0, false), ChildNumber(0, false)), true)
        else -> return "Invalid address type"
    }

    return when (addressType) {
        "P2WPKH" -> {
            val segwitKey = key.dropPrivateBytes().dropParent()
            Address.fromKey(params, segwitKey, Script.ScriptType.P2WPKH).toString()
        }
        "P2SH-P2WPKH" -> {
            val segwitKey = key.dropPrivateBytes().dropParent()
            val script = ScriptBuilder.createP2SHOutputScript(ScriptBuilder.createP2WPKHOutputScript(segwitKey))
            LegacyAddress.fromScriptHash(params, script.pubKeyHash).toString()
        }
        "P2PKH" -> {
            Address.fromKey(params, key, Script.ScriptType.P2PKH).toString()
        }
        else -> "Invalid address type"
    }
}


fun sendBitcoin(
    mnemonic: String,
    recipientAddress: String,
    amount: Coin,
    feePerKb: Coin
): String {
    return try {
        val params = TestNet3Params.get()
        val context = org.bitcoinj.core.Context(params)
        org.bitcoinj.core.Context.propagate(context)

        val seed = DeterministicSeed(mnemonic, null, "", 0L)
        val keyChain = DeterministicKeyChain.builder().seed(seed).build()
        val wallet = Wallet.createDeterministic(params, Script.ScriptType.P2WPKH)
        wallet.addAndActivateHDChain(keyChain)

        // Check wallet balance
        val balance = wallet.balance
        if (balance.isLessThan(amount.add(feePerKb))) {
            return "Error sending Bitcoin: Insufficient funds. Available balance: ${balance.toFriendlyString()}"
        }

        val sendRequest = SendRequest.to(Address.fromString(params, recipientAddress), amount)
        sendRequest.feePerKb = feePerKb
        wallet.completeTx(sendRequest)
        wallet.commitTx(sendRequest.tx)

        sendRequest.tx.txId.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        "Error sending Bitcoin: ${e.message ?: "Unknown error"}"
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendBitcoinSection(
    recipientAddress: String,
    onRecipientAddressChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    fee: String,
    onFeeChange: (String) -> Unit,
    feeOptions: List<String>,
    selectedFeeOption: String,
    onSelectedFeeOptionChange: (String) -> Unit,
    transactionResult: String,
    onSendClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Text("Send Bitcoin", style = MaterialTheme.typography.bodySmall)
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = recipientAddress,
        onValueChange = onRecipientAddressChange,
        label = { Text("Recipient Address") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = amount,
        onValueChange = onAmountChange,
        label = { Text("Amount (BTC)") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = !expanded }
) {
    OutlinedTextField(
        value = selectedFeeOption,
        onValueChange = { onSelectedFeeOptionChange(it) },
        label = { Text("Fee") },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        trailingIcon = {
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }
    )
    ExposedDropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        feeOptions.forEach { feeOption ->
            DropdownMenuItem(
                text = { Text(feeOption) },
                onClick = {
                    onSelectedFeeOptionChange(feeOption)
                    expanded = false
                }
            )
        }
    }
}
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onSendClick) {
        Text("Send")
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text("Transaction Result: $transactionResult", style = MaterialTheme.typography.bodySmall)
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES,showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController())
}