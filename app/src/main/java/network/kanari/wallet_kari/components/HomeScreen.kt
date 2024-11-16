package network.kanari.wallet_kari.components

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.common.collect.ImmutableList
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.DeterministicKey

import org.bitcoinj.params.MainNetParams
import org.bitcoinj.script.Script
import org.bitcoinj.core.Address
import org.bitcoinj.wallet.DeterministicKeyChain
import org.bitcoinj.wallet.DeterministicSeed


import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import org.bitcoinj.core.Coin
import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.wallet.SendRequest
import org.bitcoinj.wallet.Wallet
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE)
    val mnemonic = sharedPreferences.getString("mnemonic", "") ?: ""
    val password = sharedPreferences.getString("password", "") ?: ""

    var recipientAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var fee by remember { mutableStateOf("") }
    var transactionResult by remember { mutableStateOf("") }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Generated Bitcoin Addresses", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Native Segwit (P2WPKH): $nativeSegwitAddress")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Native Segwit (P2SH-P2WPKH): $nativeSegwitP2SHAddress")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Legacy (P2PKH): $legacyAddress")
        Spacer(modifier = Modifier.height(16.dp))

        Text("Send Bitcoin", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = recipientAddress,
            onValueChange = { recipientAddress = it },
            label = { Text("Recipient Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount (BTC)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = fee,
            onValueChange = { fee = it },
            label = { Text("Fee per KB (BTC)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val amountCoin = Coin.parseCoin(amount)
            val feeCoin = Coin.parseCoin(fee)
            transactionResult = createAndSendTransaction(mnemonic, recipientAddress, amountCoin, feeCoin, "P2WPKH")
        }) {
            Text("Send Transaction")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Transaction Result: $transactionResult")
    }
}


fun generateAddress(mnemonic: String, addressType: String): String {
    val params = MainNetParams.get()
    val seed = DeterministicSeed(mnemonic, null, "", 0L)
    val keyChain = DeterministicKeyChain.builder().seed(seed).build()
    val key: DeterministicKey = keyChain.getKeyByPath(ImmutableList.of(ChildNumber(44, true), ChildNumber(0, true), ChildNumber(0, true)), true)

    return when (addressType) {
        "P2WPKH" -> {
            val segwitKey = key.dropPrivateBytes().dropParent()
            Address.fromKey(params, segwitKey, Script.ScriptType.P2WPKH).toString()
        }
        "P2SH-P2WPKH" -> {
            val segwitKey = key.dropPrivateBytes().dropParent()
            val segwitAddress = Address.fromKey(params, segwitKey, Script.ScriptType.P2WPKH)
            LegacyAddress.fromScriptHash(params, ScriptBuilder.createP2SHOutputScript(segwitAddress.hash).pubKeyHash).toString()
        }
        "P2PKH" -> {
            Address.fromKey(params, key, Script.ScriptType.P2PKH).toString()
        }
        else -> "Invalid address type"
    }
}


fun createAndSendTransaction(
    mnemonic: String,
    recipientAddress: String,
    amount: Coin,
    feePerKb: Coin,
    addressType: String
): String {
    val params = MainNetParams.get()
    val seed = DeterministicSeed(mnemonic, null, "", 0L)
    val keyChain = DeterministicKeyChain.builder().seed(seed).build()
    val key: DeterministicKey = keyChain.getKeyByPath(ImmutableList.of(ChildNumber(44, true), ChildNumber(0, true), ChildNumber(0, true)), true)
    val wallet = Wallet(params)
    wallet.importKey(key)

    val recipient = Address.fromString(params, recipientAddress)
    val sendRequest = SendRequest.to(recipient, amount)
    sendRequest.feePerKb = feePerKb

    return try {
        val completedTx = wallet.sendCoinsOffline(sendRequest)
        completedTx.hashAsString
    } catch (e: Exception) {
        Log.e("Transaction", "Error creating or sending transaction", e)
        "Error creating or sending transaction"
    }
}
