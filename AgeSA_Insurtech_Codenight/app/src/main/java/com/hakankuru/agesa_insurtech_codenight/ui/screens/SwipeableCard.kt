package com.hakankuru.agesa_insurtech_codenight

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeableCard(
    category: String,
    question: String,
    isJustCash: Boolean, // JSON'dan gelen just_cash alanı
    onSwiped: (String) -> Unit // "BANK", "CASH" veya "DOUBLE_LEFT_TRIGGER" döner
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val screenWidthThreshold = 450f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp) // Kategori geldiği için biraz yükselttik
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // --- ARKA PLAN (Kılavuzlar) ---
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // SOL (Harcama Tarafı)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "💸", fontSize = 30.sp)
                Text(
                    text = if (isJustCash) "Nakit" else "Ödeme Seç",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }

            // SAĞ (Tasarruf Tarafı)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🏦", fontSize = 30.sp)
                Text(text = "Banka", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
            }
        }

        // --- HAREKETLİ KART ---
        Card(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .graphicsLayer {
                    rotationZ = offsetX.value / 20
                    alpha = 1f - (Math.abs(offsetX.value) / 1200f).coerceIn(0f, 0.4f)
                }
                .pointerInput(isJustCash) { // State değiştiğinde inputu sıfırla
                    detectDragGestures(
                        onDragEnd = {
                            scope.launch {
                                when {
                                    // SAĞA KAYDIRMA -> Her zaman Banka (Tasarruf)
                                    offsetX.value > screenWidthThreshold -> {
                                        onSwiped("BANK")
                                        offsetX.snapTo(0f)
                                    }
                                    // SOLA KAYDIRMA
                                    offsetX.value < -screenWidthThreshold -> {
                                        if (isJustCash) {
                                            onSwiped("CASH") // Direkt nakit harcama
                                        } else {
                                            onSwiped("DOUBLE_LEFT_TRIGGER") // Seçim ekranını aç (Kredi/Nakit)
                                        }
                                        offsetX.snapTo(0f)
                                    }
                                    else -> offsetX.animateTo(0f, tween(300))
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch { offsetX.snapTo(offsetX.value + dragAmount.x) }
                        }
                    )
                }
                .fillMaxSize(),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // KATEGORİ (Üstte küçük ve şık)
                Surface(
                    color = Color(0xFFFFEB3B).copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = category.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.DarkGray
                    )
                }

                // SORU METNİ (Ortada büyük)
                Text(
                    text = question,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp,
                    color = Color(0xFF2D2D2D)
                )

                // ALT BİLGİ (Kullanıcıya ne yapacağını hatırlatır)
                Text(
                    text = "Karar vermek için kaydır",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}
