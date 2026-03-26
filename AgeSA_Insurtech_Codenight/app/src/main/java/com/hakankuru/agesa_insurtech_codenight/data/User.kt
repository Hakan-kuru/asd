package com.hakankuru.agesa_insurtech_codenight.data

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val id: String,
    val username: String,
    var xp: Int
)