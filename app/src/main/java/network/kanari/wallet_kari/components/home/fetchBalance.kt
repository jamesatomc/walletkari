package network.kanari.wallet_kari.components.home

import android.annotation.SuppressLint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

@SuppressLint("DefaultLocale")
suspend fun fetchBalance(address: String, network: String): String {
    val apiUrl = when (network) {
        "Mainnet" -> "https://blockstream.info/api/address/$address"
        "Testnet3" -> "https://blockstream.info/testnet/api/address/$address"
        "Testnet4" -> "https://blockstream.info/testnet4/api/address/$address"
        else -> throw IllegalArgumentException("Invalid network")
    }
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