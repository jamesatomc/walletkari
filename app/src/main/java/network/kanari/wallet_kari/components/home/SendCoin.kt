package network.kanari.wallet_kari.components.home

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import network.kanari.wallet_kari.startQrCodeScanner
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.Script
import org.bitcoinj.wallet.DeterministicKeyChain
import org.bitcoinj.wallet.DeterministicSeed
import org.bitcoinj.wallet.SendRequest
import org.bitcoinj.wallet.Wallet
import java.io.File

fun sendBitcoin(
    mnemonic: String,
    recipientAddress: String,
    amount: Coin,
    feePerKb: Coin,
    network: String,
    addressType: String
): String {
    return try {
        val params = when (network) {
            "Mainnet" -> MainNetParams.get()
            "Testnet3" -> TestNet3Params.get()
            else -> throw IllegalArgumentException("Invalid network")
        }
        val context = org.bitcoinj.core.Context(params)
        org.bitcoinj.core.Context.propagate(context)

        val seed = DeterministicSeed(mnemonic, null, "", 0L)
        val keyChain = DeterministicKeyChain.builder().seed(seed).build()
        val scriptType = when (addressType) {
            "P2WPKH" -> Script.ScriptType.P2WPKH
            "P2SH-P2WPKH" -> Script.ScriptType.P2SH
            "P2PKH" -> Script.ScriptType.P2PKH
            else -> throw IllegalArgumentException("Invalid address type")
        }
        val wallet = Wallet.createDeterministic(params, scriptType)
        wallet.addAndActivateHDChain(keyChain)

        // Use WalletAppKit to manage the wallet and synchronize with the blockchain
        val kit = WalletAppKit(params, File("."), "walletappkit")
        kit.startAsync()
        kit.awaitRunning()

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
    } catch (e: java.net.UnknownHostException) {
        "Error: Unable to resolve host. Please check your internet connection."
    } catch (e: java.net.SocketTimeoutException) {
        "Error: Connection timed out. Please try again later."
    } catch (e: java.io.IOException) {
        "Error: Network error occurred. Please try again."
    } catch (e: Exception) {
        e.printStackTrace()
        "Error sending Bitcoin: ${e.message ?: "Unknown error"}"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendBitcoinSection(
    context: Context,
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
    onSendClick: () -> Unit,
    addressTypeOptions: List<String>,
    selectedAddressType: String,
    onSelectedAddressTypeChange: (String) -> Unit,
    maxBalance: String
) {
    var expandedFee by remember { mutableStateOf(false) }
    var expandedAddressType by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Send Bitcoin",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = recipientAddress,
            onValueChange = onRecipientAddressChange,
            label = { Text("Recipient Address") },
            modifier = Modifier
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
            trailingIcon = {
                IconButton(onClick = { startQrCodeScanner(context) }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Scan QR Code")
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = amount,
            onValueChange = onAmountChange,
            label = { Text("Amount (BTC)") },
            modifier = Modifier
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
            trailingIcon = {
                IconButton(onClick = { onAmountChange(maxBalance) }) {
                    Text("Max")
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = expandedFee,
            onExpandedChange = { expandedFee = !expandedFee }
        ) {
            TextField(
                value = selectedFeeOption,
                onValueChange = { onSelectedFeeOptionChange(it) },
                label = { Text("Fee") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor() // Ensure the dropdown menu is anchored to the text field
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
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFee)
                }
            )
            ExposedDropdownMenu(
                expanded = expandedFee,
                onDismissRequest = { expandedFee = false }
            ) {
                feeOptions.forEach { feeOption ->
                    DropdownMenuItem(
                        text = { Text(feeOption) },
                        onClick = {
                            onSelectedFeeOptionChange(feeOption)
                            expandedFee = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = expandedAddressType,
            onExpandedChange = { expandedAddressType = !expandedAddressType }
        ) {
            TextField(
                value = selectedAddressType,
                onValueChange = { onSelectedAddressTypeChange(it) },
                label = { Text("Address Type") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor() // Ensure the dropdown menu is anchored to the text field
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
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAddressType)
                }
            )
            ExposedDropdownMenu(
                expanded = expandedAddressType,
                onDismissRequest = { expandedAddressType = false }
            ) {
                addressTypeOptions.forEach { addressType ->
                    DropdownMenuItem(
                        text = { Text(addressType) },
                        onClick = {
                            onSelectedAddressTypeChange(addressType)
                            expandedAddressType = false
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
}