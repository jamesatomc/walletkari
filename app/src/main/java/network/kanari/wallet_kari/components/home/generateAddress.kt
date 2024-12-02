package network.kanari.wallet_kari.components.home

import com.google.common.collect.ImmutableList
import org.bitcoinj.core.Address
import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.wallet.DeterministicKeyChain
import org.bitcoinj.wallet.DeterministicSeed

fun generateAddress(mnemonic: String, addressType: String, network: String): String {
    val params = when (network) {
        "Mainnet" -> MainNetParams.get()
        "Testnet3" -> TestNet3Params.get()
//        "Testnet4" -> TestNet4.get()
        else -> throw IllegalArgumentException("Invalid network")
    }
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