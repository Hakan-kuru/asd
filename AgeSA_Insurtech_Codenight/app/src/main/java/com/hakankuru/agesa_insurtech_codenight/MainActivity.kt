package com.hakankuru.agesa_insurtech_codenight

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
            AgeSA_Insurtech_CodenightTheme {var currentScreen by remember { mutableStateOf("login") }

                // Kayıt olan kullanıcının ismini saklayıp Main'e aktarmak için
                var userName by remember { mutableStateOf("") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // İçerik alanı (Padding'i yönetmek önemli)
                    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            "login" -> {
                                LoginScreen(
                                    onLoginSuccess = { enteredName: String -> // Tip buraya açıkça eklendi
                                        userName = enteredName
                                        currentScreen = "main"
                                    }
                                )
                            }
                            "main" -> {
                                MainScreen(
                                    onStartTest = {
                                        // Test başlasın
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}