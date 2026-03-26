package com.hakankuru.agesa_insurtech_codenight.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hakankuru.agesa_insurtech_codenight.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit, // Başarılı girişte ana ekrana gitmek için lambda
    viewModel: LoginViewModel = viewModel()
) {
    var nameInput by remember { mutableStateOf("") }
    val isSaved by viewModel.isSaved.collectAsState()

    // Kayıt başarılı olduğunda otomatik yönlendir
    LaunchedEffect(isSaved) {
        if (isSaved) {
            onLoginSuccess(nameInput)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hoş Geldin!",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Adınızı Giriniz") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.saveUserAndLogin(nameInput) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = nameInput.isNotBlank() // İsim boşsa buton inaktif
        ) {
            Text("Maceraya Başla")
        }
    }
}

@Preview(showBackground = true, name = "Giriş Ekranı Önizleme")
@Composable
fun LoginScreenPreview() {
    // Preview'da ViewModel fonksiyonları çalışmaz ama tasarımı görebiliriz
    LoginScreen(onLoginSuccess = {})
}
