package com.hakankuru.agesa_insurtech_codenight.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // Gerekli bağımlılık aşağıda
import com.hakankuru.agesa_insurtech_codenight.data.User
import com.hakankuru.agesa_insurtech_codenight.viewmodel.UserState
import com.hakankuru.agesa_insurtech_codenight.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onStartTest: () -> Unit, // Teste başla butonu için lambda
    viewModel: UserViewModel = viewModel() // ViewModel injection
) {
    // ViewModel'deki state'i Compose state'ine çeviriyoruz
    val userState by viewModel.userState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Warning Profil") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // State'e göre farklı UI göster
            when (userState) {
                is UserState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 50.dp))
                }
                is UserState.Success -> {
                    val user = (userState as UserState.Success).user
                    UserInfoCard(user = user)
                }
                is UserState.Error -> {
                    val message = (userState as UserState.Error).message
                    Text("Hata: $message", color = Color.Red, modifier = Modifier.padding(top = 50.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Butonu aşağıya itmek için

            // Teste Başla Butonu (Lambda)
            Button(
                onClick = onStartTest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Teste Başla", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun UserInfoCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(text = "Hoş Geldin,", fontSize = 16.sp, color = Color.Gray)
            Text(text = user.username, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Mevcut Skorun:", fontSize = 16.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "${user.xp} XP", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF388E3C))
            }
        }
    }
}