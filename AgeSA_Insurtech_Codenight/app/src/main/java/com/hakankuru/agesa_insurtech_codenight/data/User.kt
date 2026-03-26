package com.hakankuru.agesa_insurtech_codenight.data

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val id: String = "",
    val username: String = "",
    var xp: Int = 0,
    var cash: Int = 1000,   // Başlangıç nakit değeri (Örn: 1000₺)
    var credit: Int = 0     // Kredi borcu 0'dan başlar, harcadıkça eksiye düşer
)