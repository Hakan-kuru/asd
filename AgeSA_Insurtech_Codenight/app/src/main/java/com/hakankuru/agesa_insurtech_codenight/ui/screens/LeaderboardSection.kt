package com.hakankuru.agesa_insurtech_codenight.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hakankuru.agesa_insurtech_codenight.data.User

// ─── Renkler ────────────────────────────────────────────────────
private val CardDark     = Color(0xFF161B22)
private val AccentGold   = Color(0xFFFFD700)
private val AccentSilver = Color(0xFFB0BEC5)
private val AccentBronze = Color(0xFFCD7F32)
private val AccentGreen  = Color(0xFF00E676)
private val AccentRed    = Color(0xFFFF5252)
private val AccentBlue   = Color(0xFF42A5F5)
private val AccentPurple = Color(0xFFCE93D8)
private val SelfColor    = Color(0xFF7C4DFF)
private val TabActive    = Color(0xFF7C4DFF)
private val TabInactive  = Color(0xFF30363D)

// Tab tanımları: (etiket, sortKey)
private val TABS = listOf(
    "⭐ XP"    to "xp",
    "💰 Bütçe" to "cash",
    "💳 Kredi" to "credit",
)

/**
 * LeaderboardSection
 *
 * Firebase Firestore 'users' koleksiyonundan gelen tüm kullanıcıları
 * 3 ayrı tabloda (XP / Bütçe / Kredi) sıralar. Yatay kaydırma ile geçiş yapılır.
 *
 * currentUser → oturum açan kullanıcı. Satırı mor renkle vurgulanır.
 * allUsers    → ViewModel'daki _allUsers StateFlow'undan gelen realtime liste.
 */
@Composable
fun LeaderboardSection(currentUser: User, allUsers: List<User>) {
    val pagerState = rememberPagerState(pageCount = { 3 })

    Column(modifier = Modifier.fillMaxWidth()) {

        // ── Başlık + toplam oyuncu sayısı ──────────────────────────
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text       = "🏆 Yarışma Sıralaması",
                color      = Color.White,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold
            )
            if (allUsers.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.08f)
                ) {
                    Text(
                        text     = "${allUsers.size} oyuncu",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color    = Color(0xFF8B949E),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // ── Tab bar ───────────────────────────────────────────────
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TABS.forEachIndexed { index, (label, _) ->
                val isActive = pagerState.currentPage == index
                Surface(
                    shape    = RoundedCornerShape(20.dp),
                    color    = if (isActive) TabActive else TabInactive,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text       = label,
                        modifier   = Modifier.padding(vertical = 8.dp),
                        textAlign  = TextAlign.Center,
                        color      = if (isActive) Color.White else Color(0xFF8B949E),
                        fontSize   = 12.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Yüklenme durumu ───────────────────────────────────────
        if (allUsers.isEmpty()) {
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color    = TabActive,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text     = "Sıralama yükleniyor...",
                        color    = Color(0xFF8B949E),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            // ── Yatay Pager — 3 tablo ─────────────────────────────
            HorizontalPager(
                state    = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) { page ->
                val (_, sortKey) = TABS[page]

                // Firestore'dan gelen User listesini sırala
                val ranked: List<Pair<User, Int>> = when (sortKey) {
                    "xp"     -> allUsers.sortedByDescending { it.xp }
                    "cash"   -> allUsers.sortedByDescending { it.cash }
                    "credit" -> allUsers.sortedBy { it.credit }   // düşük borç = iyi sıra
                    else     -> allUsers.sortedByDescending { it.xp }
                }.mapIndexed { idx, user -> user to (idx + 1) }

                // Oturum açan kullanıcının sırası (ID ile eşleştir)
                val myRank = ranked.firstOrNull { it.first.id == currentUser.id }?.second ?: 0

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                ) {
                    // Kullanıcının sıra etiketi
                    Surface(
                        shape    = RoundedCornerShape(10.dp),
                        color    = SelfColor.copy(alpha = 0.25f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier              = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                text     = "👤 Senin sıran:",
                                color    = AccentPurple,
                                fontSize = 13.sp
                            )
                            Text(
                                text       = if (myRank > 0) "#$myRank / ${ranked.size}" else "—",
                                color      = Color.White,
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            val myValue = when (sortKey) {
                                "xp"     -> "${currentUser.xp} XP"
                                "cash"   -> "${currentUser.cash}₺"
                                "credit" -> "${currentUser.credit}₺ borç"
                                else     -> ""
                            }
                            Text(
                                text       = myValue,
                                color      = AccentGreen,
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // ── Sıralama listesi ──────────────────────────
                    Column(
                        modifier            = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ranked.forEach { (user, rank) ->
                            LeaderboardRow(
                                rank    = rank,
                                user    = user,
                                sortKey = sortKey,
                                isMe    = user.id == currentUser.id
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        // ── Swipe noktaları ───────────────────────────────────────
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            repeat(3) { idx ->
                val isActive = pagerState.currentPage == idx
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isActive) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (isActive) TabActive else Color(0xFF30363D))
                )
            }
        }
    }
}

// ─── Tek satır bileşeni ─────────────────────────────────────────
@Composable
private fun LeaderboardRow(rank: Int, user: User, sortKey: String, isMe: Boolean) {
    val rankColor = when (rank) {
        1    -> AccentGold
        2    -> AccentSilver
        3    -> AccentBronze
        else -> Color(0xFF8B949E)
    }
    val rankLabel = when (rank) {
        1    -> "🥇"; 2 -> "🥈"; 3 -> "🥉"
        else -> "#$rank"
    }
    val bgColor = when {
        isMe      -> SelfColor.copy(alpha = 0.22f)
        rank <= 3 -> rankColor.copy(alpha = 0.08f)
        else      -> CardDark
    }
    val primaryValue = when (sortKey) {
        "xp"     -> "${user.xp} XP"
        "cash"   -> "${user.cash}₺"
        "credit" -> "${user.credit}₺"
        else     -> ""
    }
    val primaryColor = when (sortKey) {
        "xp"     -> AccentBlue
        "cash"   -> AccentGreen
        "credit" -> if (user.credit == 0) AccentGreen else AccentRed
        else     -> Color.White
    }

    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = bgColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text       = rankLabel,
                fontSize   = if (rank <= 3) 18.sp else 13.sp,
                fontWeight = FontWeight.Bold,
                color      = rankColor,
                modifier   = Modifier.width(40.dp),
                textAlign  = TextAlign.Center
            )
            Text(
                text       = if (isMe) "👤 ${user.username}" else user.username,
                color      = if (isMe) AccentPurple else Color.White,
                fontSize   = 14.sp,
                fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal,
                modifier   = Modifier.weight(1f),
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Text(
                text       = primaryValue,
                color      = primaryColor,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.End
            )
        }
    }
}
