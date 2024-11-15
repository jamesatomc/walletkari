// generateSeedPhrase.kt
package network.kanari.wallet_kari

import org.bitcoinj.crypto.MnemonicCode
import org.bitcoinj.wallet.DeterministicSeed
import org.bitcoinj.core.ECKey
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.core.LegacyAddress
import java.security.SecureRandom

data class SeedPhraseAndAddress(val seedPhrase: String, val bitcoinAddress: String)

fun generateSeedPhraseAndAddress(seedPhrase: String? = null): SeedPhraseAndAddress {
    val mnemonicWords: List<String>
    val finalSeedPhrase: String

    if (seedPhrase == null) {
        val secureRandom = SecureRandom()
        val entropy = ByteArray(16) // 128 bits of entropy
        secureRandom.nextBytes(entropy)
        mnemonicWords = MnemonicCode.INSTANCE.toMnemonic(entropy)
        finalSeedPhrase = mnemonicWords.joinToString(" ")
    } else {
        mnemonicWords = seedPhrase.split(" ")
        finalSeedPhrase = seedPhrase
    }

    val params = MainNetParams.get()
    val key = ECKey()
    val bitcoinAddress = LegacyAddress.fromKey(params, key).toString()

    return SeedPhraseAndAddress(finalSeedPhrase, bitcoinAddress)
}