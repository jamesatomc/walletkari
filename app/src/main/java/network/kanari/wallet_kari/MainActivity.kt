package network.kanari.wallet_kari

import AppNavHost
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import network.kanari.wallet_kari.ui.theme.WalletkariTheme
import java.util.concurrent.Executor

class MainActivity : FragmentActivity() {
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var isAuthenticated by mutableStateOf(false)

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prevent screenshots
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Handle error
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isAuthenticated = true
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Handle failure
                }
            })

            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authenticate")
                .setDeviceCredentialAllowed(true) // Allow password as an alternative
                .build()

        setContent {

                val navController = rememberNavController()
                if (isAuthenticated) {
                    Scaffold(modifier = Modifier.fillMaxSize()) {
                        AppNavHost(navController = navController, context = this@MainActivity)
                    }
                } else {
                    // Show a loading or authentication screen with a black background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFFFA500)) // Orange color
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock, // Use a suitable icon
                                contentDescription = "Lock Icon",
                                modifier = Modifier.size(48.dp) // Adjust the size as needed
                            )
                            Spacer(modifier = Modifier.height(320.dp))
                            Button(
                                onClick = { biometricPrompt.authenticate(promptInfo) },
                                Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Unlock Wallet")
                            }
                        }
                    }
                }
            }
        }


    override fun onResume() {
        super.onResume()
        isAuthenticated = false
        biometricPrompt.authenticate(promptInfo)
    }
}