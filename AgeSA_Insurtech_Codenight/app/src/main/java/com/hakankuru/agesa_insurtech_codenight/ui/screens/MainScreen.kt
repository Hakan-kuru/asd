package com.hakankuru.agesa_insurtech_codenight.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hakankuru.agesa_insurtech_codenight.assest.BudgetHeader
import com.hakankuru.agesa_insurtech_codenight.data.QuestionManager
import com.hakankuru.agesa_insurtech_codenight.data.User
import com.hakankuru.agesa_insurtech_codenight.ui.FinanceGameScreen
import com.hakankuru.agesa_insurtech_codenight.viewmodel.UserState
import com.hakankuru.agesa_insurtech_codenight.viewmodel.UserViewModel
import com.hakankuru.agesa_insurtech_codenight.R


import android.util.Log


@Composable
fun MainScreen(userId: String, viewModel: UserViewModel = viewModel()) {
    val userState by viewModel.userState.collectAsState()
    val context = LocalContext.current
    var isTestStarted by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.startFetching(userId)
        }
    }

    // JSON dosyasını yerel assets klasöründen oku
    val questionManager = remember(isTestStarted) {
        if (isTestStarted) {
            try {
                val jsonString = context.resources.openRawResource(R.raw.questions).bufferedReader().use { it.readText() }
                QuestionManager(jsonString)
            } catch (e: Exception) {
                Log.e("JSON_ERROR", "questions.json dosyası bulunamadı!", e)
                null
            }
        } else null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = userState) {
            is UserState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            is UserState.Error -> Text("Hata: ${state.message}", Modifier.align(Alignment.Center))
            is UserState.Success -> {
                if (!isTestStarted) {
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        UserInfoCard(user = state.user)
                        Button(onClick = { isTestStarted = true }) { Text("Teste Başla") }
                    }
                } else {
                    if (questionManager != null) {
                        Column {
                            BudgetHeader(cash = state.user.cash, credit = state.user.credit)
                            FinanceGameScreen(
                                manager = questionManager, 
                                viewModel = viewModel,
                                onTestFinished = { isTestStarted = false }
                            )
                        }
                    } else {
                        Text("Soru dosyası yüklenemedi!", Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}
@Composable
fun UserInfoCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Hoş Geldin,", fontSize = 14.sp, color = Color.Gray)
            Text(user.username, fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray)
            Spacer(Modifier.height(12.dp))

            // Nakit, Kredi ve XP yan yana
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Nakit", fontSize = 12.sp, color = Color.Gray)
                    Text("${user.cash}₺", fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                }
                Column {
                    Text("Kredi", fontSize = 12.sp, color = Color.Gray)
                    Text(if (user.credit > 0) "-${user.credit}₺" else "${user.credit}₺", fontWeight = FontWeight.Bold, color = Color.Red)
                }
                Column {
                    Text("Skor", fontSize = 12.sp, color = Color.Gray)
                    Text("${user.xp} XP", fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                }
            }
        }
    }
}