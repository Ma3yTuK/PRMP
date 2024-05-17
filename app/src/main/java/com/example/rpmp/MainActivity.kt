package com.example.rpmp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Path.Op
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState.Companion.Saver
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CreatePasswordResponse
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rpmp.ui.theme.RPMPTheme
import kotlinx.coroutines.launch


enum class Numbers(val value: String) {
    ZERO("0"),
    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    NINE("9")
}

const val POINT = "."
const val RESET = "C"
const val BACK = "<-"
const val CALC = "="

const val CODE = "PiPi"


class MainActivity : ComponentActivity() {
    private lateinit var credentialManager: CredentialManager

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val viewModel: MyViewModel by viewModels()
            viewModel.setValue(result.data?.getStringExtra("result")!!)
            viewModel.setRecompose()
            viewModel.calculate(viewModel.uiState.calcObjects)
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this,
                "Permissions not granted by the user.",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun rememberPassword(userId: String, password: String) {
        val request = CreatePasswordRequest(userId, password)

        lifecycleScope.launch {
            try {
                credentialManager.createCredential(this@MainActivity, request) as CreatePasswordResponse
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity,
                    e.message,
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        credentialManager = CredentialManager.create(this)
        setContent {
            Navigator()
        }
    }

    private fun checkPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED)
            return true
        requestPermissionLauncher.launch(Manifest.permission.INTERNET)
        return false
    }

    @Composable
    fun Navigator(viewModel: LoginViewModel = viewModel()) {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "home") {
            // add all destinations here
            composable(route = "home") {
                CalculatorScreen (
                    onScan = {
                        startForResult.launch(Intent(this@MainActivity, ScannerActivity::class.java))
                    },
                    onHist = {
                        if (checkPermissions())
                            navController.navigate("history")
                    }
                )
            } // home destination
            composable(route = "history") {
                HistoryScreen()
                /*if (viewModel.loginState.isLoggedIn) {
                    HistoryScreen()
                } else {
                    LoginScreen(
                        onLogin = { username: String, password: String ->
                            try {
                                viewModel.logIn(username, password)
                                //rememberPassword(username, password)
                            } catch (e: Exception) {
                                Toast.makeText(this@MainActivity,
                                    "Invalid credentials",
                                    Toast.LENGTH_SHORT).show()
                            }
                        },
                        onRegister = {
                            navController.navigate("registration")
                        }
                    )
                }*/
            } // profile destination
            /*composable(route = "registration") {
                RegistrationScreen(
                    onSignUp = { username: String, password: String ->
                        try {
                            viewModel.signUp(username, password)
                            //rememberPassword(username, password)
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity,
                                "Cannot register user",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }*/
        }
    }
}


