package com.hakankuru.agesa_insurtech_codenight.assest

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BudgetHeader(
    cash: Int,
    credit: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // NAKİT BÖLÜMÜ
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Nakit Bakiye",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${cash}₺",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (cash >= 0) Color(0xFF388E3C) else Color.Red // Nakit biterse kırmızıya döner
                )
            }

            // AYIRICI ÇİZGİ
            VerticalDivider(
                modifier = Modifier.height(40.dp),
                thickness = 1.dp,
                color = Color.LightGray
            )

            // KREDİ BÖLÜMÜ
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Kredi Borcu",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (credit > 0) "-${credit}₺" else "${credit}₺",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (credit > 0) Color(0xFFD32F2F) else Color.DarkGray // Borç varsa kırmızı
                )
            }
        }
    }
}