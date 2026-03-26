package com.hakankuru.agesa_insurtech_codenight

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeableCard(
    question: String,
    leftOption: String,
    rightOption: String,
    onSwiped: (String) -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val screenWidthThreshold = 450f // Kaydırma eşiği

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Arka plan (Hangi yöne çekince ne olacağını gösteren kılavuzlar)
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "❌", fontSize = 24.sp)
            Text(text = "✅", fontSize = 24.sp)
        }

        // Hareketli Kart
        Card(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .graphicsLayer {
                    rotationZ = offsetX.value / 25 // Kart kaydıkça hafif döner
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value > screenWidthThreshold) {
                                    onSwiped(rightOption)
                                    offsetX.snapTo(0f)
                                } else if (offsetX.value < -screenWidthThreshold) {
                                    onSwiped(leftOption)
                                    offsetX.snapTo(0f)
                                } else {
                                    offsetX.animateTo(0f, tween(300))
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
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Soru Metni
                Text(
                    text = question,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Yatık Seçenekler Row'u
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Sola Yatık Sol Seçenek
                    Text(
                        text = leftOption,
                        modifier = Modifier.graphicsLayer { rotationZ = -15f }, // Sola eğim
                        color = Color.Red,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Sağa Yatık Sağ Seçenek
                    Text(
                        text = rightOption,
                        modifier = Modifier.graphicsLayer { rotationZ = 15f }, // Sağa eğim
                        color = Color(0xFF388E3C), // Yeşil tonu
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Swipe Card Önizleme")
@Composable
fun SwipeableCardPreview() {
    // Projendeki genel temayı kullanıyorsan onunla sarmalayabilirsin
    // Örn: YourAppTheme { ... }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF0F0F0) // Arka planı biraz gri yapalım ki beyaz kart belli olsun
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SwipeableCard(
                question = "Acil durum butonu aktif edilsin mi?",
                leftOption = "İptal",
                rightOption = "Onayla",
                onSwiped = { secim ->
                    println("Seçilen: $secim")
                }
            )
        }
    }
}