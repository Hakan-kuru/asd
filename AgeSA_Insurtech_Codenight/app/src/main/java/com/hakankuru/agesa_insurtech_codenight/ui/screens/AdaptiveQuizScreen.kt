package com.hakankuru.agesa_insurtech_codenight.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import com.hakankuru.agesa_insurtech_codenight.data.AdaptiveQuestion
import com.hakankuru.agesa_insurtech_codenight.viewmodel.UserViewModel

// ─── Renk paleti ───────────────────────────────────────────────
private val BgGradientTop    = Color(0xFF1A1A2E)
private val BgGradientBottom = Color(0xFF16213E)
private val AccentGreen      = Color(0xFF00E676)
private val AccentRed        = Color(0xFFFF5252)
private val CardBg           = Color(0xFF0F3460).copy(alpha = 0.85f)
private val TextWhite        = Color(0xFFECEFF1)
private val TextGray         = Color(0xFF90A4AE)
private val CategoryChip     = Color(0xFF533483)

/**
 * AdaptiveQuizScreen
 *
 * "Hatalardan Ders Çıkar" butonuna basılınca açılan adaptif quiz ekranı.
 * Gemini tarafından üretilen soruları Evet/Hayır formatında gösterir.
 *
 * adaptive-quiz'in frontend mantığını (yes/no + explanation) Compose ile uygular.
 */
@Composable
fun AdaptiveQuizScreen(
    questions: List<AdaptiveQuestion>,
    viewModel: UserViewModel,
    onFinished: () -> Unit
) {
    var currentIndex    by remember { mutableIntStateOf(0) }
    var showFeedback    by remember { mutableStateOf<FeedbackState?>(null) }
    var correctCount    by remember { mutableIntStateOf(0) }
    var showSummary     by remember { mutableStateOf(false) }
    var earnedXP        by remember { mutableIntStateOf(0) }

    val currentQuestion = if (currentIndex < questions.size) questions[currentIndex] else null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BgGradientTop, BgGradientBottom)
                )
            )
    ) {
        when {
            // ── Özet ekranı ─────────────────────────────────────
            showSummary -> {
                SummaryCard(
                    correctCount = correctCount,
                    totalCount   = questions.size,
                    earnedXP     = earnedXP,
                    onFinished   = onFinished
                )
            }

            // ── Feedback diyaloğu ────────────────────────────────
            showFeedback != null -> {
                FeedbackCard(
                    state = showFeedback!!,
                    onNext = {
                        showFeedback = null
                        val nextIndex = currentIndex + 1
                        if (nextIndex >= questions.size) {
                            // Quiz bitti — XP hesapla ve Firebase'e yaz
                            earnedXP = correctCount * 15
                            viewModel.updateAdaptiveXP(correctCount, questions.size)
                            showSummary = true
                        } else {
                            currentIndex = nextIndex
                        }
                    }
                )
            }

            // ── Soru kartı ───────────────────────────────────────
            currentQuestion != null -> {
                AnimatedVisibility(
                    visible = true,
                    enter   = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 }
                ) {
                    QuestionCard(
                        question     = currentQuestion,
                        questionNo   = currentIndex + 1,
                        totalQuestions = questions.size,
                        onAnswered = { chosen ->
                            val isCorrect = chosen == currentQuestion.correct_option
                            if (isCorrect) correctCount++
                            showFeedback = FeedbackState(
                                isCorrect   = isCorrect,
                                explanation = currentQuestion.explanation
                            )
                        }
                    )
                }
            }
        }
    }
}

// ─── Soru Bileşeni ──────────────────────────────────────────────
@Composable
private fun QuestionCard(
    question: AdaptiveQuestion,
    questionNo: Int,
    totalQuestions: Int,
    onAnswered: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Üst kısım: ilerleme + kategori
        Column {
            Spacer(Modifier.height(40.dp))

            // İlerleme çubuğu
            LinearProgressIndicator(
                progress = { questionNo.toFloat() / totalQuestions.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color             = AccentGreen,
                trackColor        = Color.White.copy(alpha = 0.15f)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text  = "Soru $questionNo / $totalQuestions",
                color = TextGray,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(8.dp))

            // Kategori chip
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CategoryChip.copy(alpha = 0.6f)
            ) {
                Text(
                    text     = "📚 ${question.category.uppercase()}",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    color    = TextWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Soru metni
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape  = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text  = "🧠 Finansal Bilgi Sorusu",
                    color = AccentGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text       = question.text,
                    color      = TextWhite,
                    fontSize   = 18.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign  = TextAlign.Start
                )
            }
        }

        // Evet / Hayır butonları
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // EVET butonu
            Button(
                onClick  = { onAnswered("yes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
            ) {
                Text(
                    text       = "✅ Evet, Doğru",
                    color      = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
            }

            // HAYIR butonu
            Button(
                onClick  = { onAnswered("no") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
            ) {
                Text(
                    text       = "❌ Hayır, Yanlış",
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

// ─── Feedback Kartı ─────────────────────────────────────────────
private data class FeedbackState(val isCorrect: Boolean, val explanation: String)

@Composable
private fun FeedbackCard(state: FeedbackState, onNext: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape  = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (state.isCorrect) Color(0xFF1B5E20) else Color(0xFF7F0000)
            ),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text     = if (state.isCorrect) "🎉 Harika!" else "💡 Öğrenme Fırsatı",
                    color    = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text     = if (state.isCorrect) "Bilgin doğruymuş!" else "Bu konuyu öğrendik:",
                    color    = Color.White.copy(alpha = 0.75f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                Spacer(Modifier.height(20.dp))

                Text(
                    text      = state.explanation,
                    color     = Color.White,
                    fontSize  = 16.sp,
                    lineHeight = 24.sp,
                    textAlign  = TextAlign.Center
                )

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick  = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.25f)
                    )
                ) {
                    Text(
                        text = "Devam Et →",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// ─── Özet Ekranı ────────────────────────────────────────────────
@Composable
private fun SummaryCard(correctCount: Int, totalCount: Int, earnedXP: Int, onFinished: () -> Unit) {
    val successRate = if (totalCount > 0) (correctCount * 100 / totalCount) else 0
    val emoji = when {
        successRate >= 80 -> "🏆"
        successRate >= 50 -> "📈"
        else              -> "💪"
    }
    val message = when {
        successRate >= 80 -> "Harika iş çıkardın! Finansal bilgin güçlenmiş."
        successRate >= 50 -> "İyi bir başlangıç! Pratik yaptıkça daha iyi olacaksın."
        else              -> "Her sorun bir fırsat. Bu kategorileri tekrar çalış!"
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape  = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F3460)),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = emoji, fontSize = 56.sp)

                Text(
                    text  = "Adaptif Quiz Tamamlandı!",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text  = "$correctCount / $totalCount Doğru (%$successRate)",
                    color = AccentGreen,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                // Kazanılan XP rozeti
                if (earnedXP > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF7C4DFF).copy(alpha = 0.35f)
                    ) {
                        Text(
                            text     = "⭐ +$earnedXP XP kazandın!",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color    = Color(0xFFCE93D8),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                Text(
                    text      = message,
                    color     = TextGray,
                    fontSize  = 15.sp,
                    lineHeight = 22.sp,
                    textAlign  = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick  = onFinished,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape  = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Text(
                        text  = "Ana Menüye Dön",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
