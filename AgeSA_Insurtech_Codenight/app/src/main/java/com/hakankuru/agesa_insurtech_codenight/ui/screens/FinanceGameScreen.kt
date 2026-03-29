package com.hakankuru.agesa_insurtech_codenight.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hakankuru.agesa_insurtech_codenight.SwipeableCard
import com.hakankuru.agesa_insurtech_codenight.data.QuestionManager
import com.hakankuru.agesa_insurtech_codenight.ui.screens.AdaptiveQuizScreen
import com.hakankuru.agesa_insurtech_codenight.viewmodel.UserViewModel
import androidx.compose.ui.platform.LocalContext

// Renk sabitleri
private val DarkBg    = Color(0xFF1A1A2E)
private val DarkBg2   = Color(0xFF16213E)
private val GreenBtn  = Color(0xFF00C853)
private val PurpleBtn = Color(0xFF7C4DFF)

@Composable
fun FinanceGameScreen(manager: QuestionManager, viewModel: UserViewModel, onTestFinished: () -> Unit) {
    var currentQuestion    by remember { mutableStateOf(manager.getCurrentQuestion()) }
    var showExplanation    by remember { mutableStateOf<String?>(null) }
    var showSelectionDialog by remember { mutableStateOf(false) }
    val context            = LocalContext.current

    // Adaptif quiz state'leri
    val adaptiveQuestions by viewModel.adaptiveQuestions.collectAsState()
    val isLoadingAdaptive by viewModel.isLoadingAdaptive.collectAsState()

    // Adaptif quiz soruları hazır olunca ekranı göster
    var showAdaptiveQuiz by remember { mutableStateOf(false) }

    LaunchedEffect(adaptiveQuestions) {
        if (adaptiveQuestions.isNotEmpty()) {
            showAdaptiveQuiz = true
        }
    }

    // ── Adaptif Quiz Ekranı ─────────────────────────────────────
    if (showAdaptiveQuiz && adaptiveQuestions.isNotEmpty()) {
        AdaptiveQuizScreen(
            questions  = adaptiveQuestions,
            viewModel  = viewModel,
            onFinished = {
                showAdaptiveQuiz = false
                viewModel.clearAdaptiveQuestions()
                onTestFinished()
            }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        
        // ── Gemini yüklenirken spinner ──────────────────────────
        if (isLoadingAdaptive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = GreenBtn)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text     = "🤖 Gemini AI analiz ediyor...\nSenin için yeni sorular hazırlanıyor",
                        color    = Color.White,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }
            return@Box
        }
        
        // ── Ana quiz içeriği ────────────────────────────────────
        if (currentQuestion != null && showExplanation == null) {
            SwipeableCard(
                category   = currentQuestion!!.category,
                question   = currentQuestion!!.text,
                isJustCash = currentQuestion!!.just_cash,
                onSwiped   = { choice ->
                    when (choice) {
                        "BANK" -> {
                            manager.recordResponse(currentQuestion!!, "BANK")
                            showExplanation = currentQuestion!!.answers.bank.explanation
                        }
                        "CASH" -> {
                            manager.recordResponse(currentQuestion!!, "CASH")
                            viewModel.updateUserFinance(currentQuestion!!.amount_percent, "CASH")
                            showExplanation = currentQuestion!!.answers.cash.explanation
                        }
                        "DOUBLE_LEFT_TRIGGER" -> showSelectionDialog = true
                    }
                }
            )
        } else if (currentQuestion == null) {
            // ── TEST BİTTİ ekranı ──────────────────────────────
            val wrongCategories = manager.getWrongCategories()

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text       = "🎉 Test Bitti!",
                    fontSize   = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    color      = Color(0xFF1A1A2E)
                )

                if (wrongCategories.isNotEmpty()) {
                    Text(
                        text      = "Hata yaptığın ${wrongCategories.size} kategori var.\nAI sana özel sorular hazırlasın!",
                        fontSize  = 15.sp,
                        color     = Color(0xFF546E7A),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    // ── HATALARDAN DERS ÇIKAR butonu ──────────────
                    Button(
                        onClick  = {
                            manager.saveAllResponsesToFile(context)
                            viewModel.learnFromMistakes(wrongCategories)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape  = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleBtn)
                    ) {
                        Text(
                            text       = "🧠  Hatalardan Ders Çıkar",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 17.sp,
                            color      = Color.White
                        )
                    }
                } else {
                    Text(
                        text      = "✅ Tüm soruları doğru cevapladın!\nHarika bir performans!",
                        fontSize  = 16.sp,
                        color     = Color(0xFF388E3C),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Ana menü butonu
                OutlinedButton(
                    onClick  = onTestFinished,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1A1A2E))
                ) {
                    Text(
                        text       = "Ana Menüye Dön",
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 15.sp
                    )
                }
            }
        }

        // ── Açıklama Diyaloğu ────────────────────────────────────
        showExplanation?.let { msg ->
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Finansal Sonuç") },
                text  = { Text(msg) },
                confirmButton = {
                    Button(onClick = {
                        manager.nextQuestion()
                        currentQuestion = manager.getCurrentQuestion()
                        showExplanation = null
                        if (currentQuestion == null) {
                            manager.saveAllResponsesToFile(context)
                        }
                    }) { Text("Devam Et") }
                }
            )
        }

        // ── Kredi/Nakit Seçim Diyaloğu ──────────────────────────
        if (showSelectionDialog) {
            AlertDialog(
                onDismissRequest = { showSelectionDialog = false },
                title            = { Text("Ödeme Yöntemi") },
                confirmButton = {
                    Button(onClick = {
                        manager.recordResponse(currentQuestion!!, "CREDIT")
                        viewModel.updateUserFinance(currentQuestion!!.amount_percent, "CREDIT")
                        showExplanation    = currentQuestion!!.answers.credit.explanation
                        showSelectionDialog = false
                    }) { Text("Kredi Kartı") }
                },
                dismissButton = {
                    Button(onClick = {
                        manager.recordResponse(currentQuestion!!, "CASH")
                        viewModel.updateUserFinance(currentQuestion!!.amount_percent, "CASH")
                        showExplanation    = currentQuestion!!.answers.cash.explanation
                        showSelectionDialog = false
                    }) { Text("Nakit") }
                }
            )
        }
    }
}