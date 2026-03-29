package com.hakankuru.agesa_insurtech_codenight.data

/**
 * MockLeaderboardData
 *
 * Yarışma ekranı için hardcoded 17 rakip kullanıcı.
 * Hiçbir yerden çekilmez — kod içinde tanımlıdır.
 */
data class LeaderboardUser(
    val username: String,
    val xp: Int,
    val cash: Int,    // Bütçe (₺)
    val credit: Int   // Kredi borcu (₺, 0 = borçsuz, yüksek = daha kötü)
)

object MockLeaderboardData {

    val competitors: List<LeaderboardUser> = listOf(
        LeaderboardUser("Ahmet Yılmaz",   xp = 420, cash = 14_200, credit = 800),
        LeaderboardUser("Zeynep Kaya",    xp = 385, cash = 12_500, credit = 0),
        LeaderboardUser("Mehmet Öz",      xp = 370, cash = 11_800, credit = 1_200),
        LeaderboardUser("Ayşe Demir",     xp = 355, cash = 13_000, credit = 300),
        LeaderboardUser("Emre Çelik",     xp = 340, cash = 9_750,  credit = 2_100),
        LeaderboardUser("Selin Arslan",   xp = 310, cash = 10_400, credit = 500),
        LeaderboardUser("Burak Şahin",    xp = 290, cash = 8_900,  credit = 3_500),
        LeaderboardUser("Nil Koç",        xp = 275, cash = 11_100, credit = 600),
        LeaderboardUser("Can Doğan",      xp = 260, cash = 7_600,  credit = 4_000),
        LeaderboardUser("Dilan Aydın",    xp = 245, cash = 9_200,  credit = 1_800),
        LeaderboardUser("Oğuz Polat",     xp = 230, cash = 6_800,  credit = 5_200),
        LeaderboardUser("Pınar Güler",    xp = 215, cash = 10_700, credit = 200),
        LeaderboardUser("Arda Kılıç",     xp = 200, cash = 5_500,  credit = 6_800),
        LeaderboardUser("Fatma Erdoğan",  xp = 185, cash = 8_300,  credit = 2_400),
        LeaderboardUser("Kaan Yıldız",    xp = 170, cash = 4_200,  credit = 7_500),
        LeaderboardUser("İrem Çakır",     xp = 155, cash = 7_900,  credit = 1_100),
        LeaderboardUser("Serkan Avcı",    xp = 130, cash = 3_800,  credit = 9_000),
    )
}
