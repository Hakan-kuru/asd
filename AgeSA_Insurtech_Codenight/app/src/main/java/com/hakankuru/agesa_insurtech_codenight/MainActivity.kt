package com.hakankuru.agesa_insurtech_codenight

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hakankuru.agesa_insurtech_codenight.ui.screens.LoginScreen
import com.hakankuru.agesa_insurtech_codenight.ui.screens.MainScreen
import com.hakankuru.agesa_insurtech_codenight.ui.theme.AgeSA_Insurtech_CodenightTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgeSA_Insurtech_CodenightTheme {
                var currentScreen by remember { mutableStateOf("login") }
                var userId by remember { mutableStateOf("") } // Kaydedilen kullanıcı ID'si

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            "login" -> {
                                LoginScreen(
                                    onLoginSuccess = { savedUserId ->
                                        // Login başarılı olunca Main'e geç
                                        userId = savedUserId
                                        currentScreen = "main"
                                    }
                                )
                            }
                            "main" -> {
                                // Artık onStartTest parametresini içerde yönetiyoruz
                                MainScreen(userId = userId)
                            }
                        }
                    }
                }
            }
        }
    }
}