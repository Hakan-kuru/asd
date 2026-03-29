package com.hakankuru.agesa_insurtech_codenight.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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

// ─── Tema renkleri ───────────────────────────────────────────────
private val ScreenBgTop    = Color(0xFF0D1117)
private val ScreenBgBottom = Color(0xFF161B22)
private val CardBg         = Color(0xFF1C2128)
private val AccentPurple   = Color(0xFF7C4DFF)
private val AccentGreen    = Color(0xFF00E676)
private val AccentRed      = Color(0xFFFF5252)
private val AccentBlue     = Color(0xFF42A5F5)
private val TextPrimary    = Color(0xFFECEFF1)
private val TextSecondary  = Color(0xFF8B949E)

@Composable
fun MainScreen(userId: String, viewModel: UserViewModel = viewModel()) {
    val userState by viewModel.userState.collectAsState()
    val allUsers  by viewModel.allUsers.collectAsState()  // Firebase tüm kullanıcılar
    val context = LocalContext.current
    var isTestStarted by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) viewModel.startFetching(userId)
    }

    val questionManager = remember(isTestStarted) {
        if (isTestStarted) {
            try {
                val json = context.resources.openRawResource(R.raw.questions).bufferedReader().use { it.readText() }
                QuestionManager(json)
            } catch (e: Exception) {
                Log.e("JSON_ERROR", "questions.json bulunamadı!", e)
                null
            }
        } else null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ScreenBgTop, ScreenBgBottom)))
    ) {
        when (val state = userState) {

            is UserState.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color    = AccentPurple
            )

            is UserState.Error -> Text(
                text     = "Hata: ${state.message}",
                color    = AccentRed,
                modifier = Modifier.align(Alignment.Center)
            )

            is UserState.Success -> {
                if (isTestStarted) {
                    // ── TEST MODU ─────────────────────────────────
                    if (questionManager != null) {
                        Column {
                            BudgetHeader(cash = state.user.cash, credit = state.user.credit)
                            FinanceGameScreen(
                                manager       = questionManager,
                                viewModel     = viewModel,
                                onTestFinished = { isTestStarted = false }
                            )
                        }
                    } else {
                        Text(
                            text     = "Soru dosyası yüklenemedi!",
                            color    = AccentRed,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else {
                    // ── ANA MENÜ MODU ─────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(Modifier.height(16.dp))

                        // Kullanıcı bilgi kartı
                        UserInfoCard(user = state.user)

                        Spacer(Modifier.height(12.dp))

                        // ── TESTE BAŞLA butonu (her zaman görünür) ─
                        Button(
                            onClick  = { isTestStarted = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape  = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                        ) {
                            Text(
                                text       = "🚀  Teste Başla",
                                fontSize   = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color.White
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        // ── LEADERBOARD (3 tablo, yatay kaydır) ───
                        Surface(
                            shape  = RoundedCornerShape(20.dp),
                            color  = CardBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LeaderboardSection(
                                currentUser = state.user,
                                allUsers    = allUsers
                            )
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

// ─── Kullanıcı bilgi kartı ───────────────────────────────────────
@Composable
fun UserInfoCard(user: User) {
    Surface(
        shape  = RoundedCornerShape(20.dp),
        color  = CardBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Hoş Geldin 👋", fontSize = 13.sp, color = TextSecondary)
            Text(
                text       = user.username,
                fontSize   = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = TextPrimary
            )

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Spacer(Modifier.height(14.dp))

            // Nakit | Kredi | XP — 3 metrik yan yana
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricChip(
                    label = "Nakit",
                    value = "${user.cash}₺",
                    color = AccentGreen
                )
                MetricChip(
                    label = "Kredi Borcu",
                    value = if (user.credit > 0) "-${user.credit}₺" else "Temiz ✓",
                    color = if (user.credit > 0) AccentRed else AccentGreen
                )
                MetricChip(
                    label = "Skor",
                    value = "${user.xp} XP",
                    color = AccentBlue
                )
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text     = label,
            fontSize = 11.sp,
            color    = TextSecondary
        )
        Spacer(Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.15f)
        ) {
            Text(
                text       = value,
                modifier   = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                fontWeight = FontWeight.Bold,
                fontSize   = 14.sp,
                color      = color
            )
        }
    }
}