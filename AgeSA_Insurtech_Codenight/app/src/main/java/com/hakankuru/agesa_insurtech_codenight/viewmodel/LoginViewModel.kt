package com.hakankuru.agesa_insurtech_codenight.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _isSaved = MutableStateFlow(false)
    val isSaved = _isSaved.asStateFlow()

    // Kaydedilen kullanıcının ID'sini saklamak istersen:
    var savedUserId: String = ""

    fun saveUserAndLogin(username: String) {
        if (username.isBlank()) return

        // 1. "users" koleksiyonu için boş bir doküman referansı oluştur (ID burada üretilir)
        val newUserRef = db.collection("users").document()
        val generatedId = newUserRef.id // Üretilen ID'yi aldık

        // 2. Veri paketine bu ID'yi de ekliyoruz
        val newUser = com.hakankuru.agesa_insurtech_codenight.data.User(
            id = generatedId,
            username = username
        )

        // 3. .set() kullanarak veriyi bu ID ile kaydediyoruz
        newUserRef.set(newUser)
            .addOnSuccessListener {
                savedUserId = generatedId // İleride lazım olursa diye sakla
                _isSaved.value = true
            }
            .addOnFailureListener { e ->
                println("Hata oluştu: ${e.message}")
            }
    }
}