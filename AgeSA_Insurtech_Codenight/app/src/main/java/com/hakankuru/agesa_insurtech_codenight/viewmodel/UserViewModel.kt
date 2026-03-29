package com.hakankuru.agesa_insurtech_codenight.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.hakankuru.agesa_insurtech_codenight.data.AdaptiveQuestion
import com.hakankuru.agesa_insurtech_codenight.data.GeminiAdaptiveService
import com.hakankuru.agesa_insurtech_codenight.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UserState {
    object Loading : UserState()
    data class Success(val user: User) : UserState()
    data class Error(val message: String) : UserState()
}

class UserViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    // ──────────────────────────────────────────────────────────────
    // Leaderboard — tüm kullanıcılar (Firestore realtime)
    // ──────────────────────────────────────────────────────────────
    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    // ──────────────────────────────────────────────────────────────
    // "Hatalardan Ders Çıkar" state'leri
    // ──────────────────────────────────────────────────────────────
    private val _adaptiveQuestions = MutableStateFlow<List<AdaptiveQuestion>>(emptyList())
    val adaptiveQuestions: StateFlow<List<AdaptiveQuestion>> = _adaptiveQuestions.asStateFlow()

    private val _isLoadingAdaptive = MutableStateFlow(false)
    val isLoadingAdaptive: StateFlow<Boolean> = _isLoadingAdaptive.asStateFlow()

    // LoginScreen'den gelen ID ile veri çekmeyi başlatan tetikleyici
    fun startFetching(userId: String) {
        if (userId.isEmpty()) {
            _userState.value = UserState.Error("Kullanıcı ID bulunamadı.")
            return
        }
        getUserData(userId)
        fetchAllUsers()   // Leaderboard için tüm kullanıcıları da çek
    }

    // ──────────────────────────────────────────────────────────────
    // Firestore 'users' koleksiyonunu realtime dinler
    // XP/cash/credit değişince leaderboard otomatik güncellenir
    // ──────────────────────────────────────────────────────────────
    private fun fetchAllUsers() {
        db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Leaderboard", "Kullanıcılar çekilemedi: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
                    _allUsers.value = users
                    Log.d("Leaderboard", "${users.size} kullanıcı güncellendi.")
                }
            }
    }

    private fun getUserData(userId: String) {
        viewModelScope.launch {
            db.collection("users").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _userState.value = UserState.Error(error.message ?: "Firebase hatası")
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val user = snapshot.toObject(User::class.java)
                        if (user != null) {
                            _userState.value = UserState.Success(user)
                        } else {
                            _userState.value = UserState.Error("Veri dönüştürülemedi")
                        }
                    } else {
                        _userState.value = UserState.Error("Kullanıcı kaydı bulunamadı")
                    }
                }
        }
    }

    fun updateUserFinance(percent: Double, budgetType: String) {
        val currentState = _userState.value
        if (currentState is UserState.Success) {
            val user = currentState.user
            val cost = (user.cash * percent).toInt()

            var newCash = user.cash
            var newCredit = user.credit
            var newXp = user.xp

            when (budgetType) {
                "CASH" -> {
                    newCash -= cost
                    newXp = (newXp - 5).coerceAtLeast(0)
                }
                "CREDIT" -> {
                    newCredit += cost
                    newXp = (newXp - 10).coerceAtLeast(0)
                }
                "BANK" -> newXp += 15
            }

            db.collection("users").document(user.id)
                .update("cash", newCash, "credit", newCredit, "xp", newXp)
                .addOnFailureListener { Log.e("Finance", "Güncelleme hatası!", it) }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Adaptif Quiz XP Güncellemesi
    // Doğru cevap: +15 XP (adaptive-quiz'deki evaluateFollowup delta ile aynı)
    // Yanlış cevap: 0 XP (ceza yok — öğrenme modu)
    // Test bittikten sonra tek seferde Firebase'e yazılır.
    // ──────────────────────────────────────────────────────────────
    fun updateAdaptiveXP(correctCount: Int, totalCount: Int) {
        val currentState = _userState.value
        if (currentState is UserState.Success) {
            val user   = currentState.user
            val xpGain = correctCount * 15          // Her doğru = +15 XP
            val newXp  = user.xp + xpGain

            Log.d("AdaptiveQuiz", "XP güncelleniyor: +$xpGain → toplam $newXp")

            db.collection("users").document(user.id)
                .update("xp", newXp)
                .addOnSuccessListener {
                    Log.d("AdaptiveQuiz", "XP Firebase'e yazıldı: $newXp")
                }
                .addOnFailureListener { Log.e("AdaptiveQuiz", "XP güncelleme hatası!", it) }
        }
    }


    // ──────────────────────────────────────────────────────────────
    // Hatalardan Ders Çıkar
    // adaptive-quiz → learnFromMistakes endpoint'inin ViewModel karşılığı
    // ──────────────────────────────────────────────────────────────
    fun learnFromMistakes(wrongCategories: List<String>) {
        if (wrongCategories.isEmpty()) return

        viewModelScope.launch {
            _isLoadingAdaptive.value = true
            _adaptiveQuestions.value = emptyList()
            try {
                val questions = GeminiAdaptiveService.generateQuestionsForCategories(wrongCategories)
                _adaptiveQuestions.value = questions
                Log.d("AdaptiveQuiz", "${questions.size} adaptif soru üretildi.")
            } catch (e: Exception) {
                Log.e("AdaptiveQuiz", "Hata: ${e.message}")
            } finally {
                _isLoadingAdaptive.value = false
            }
        }
    }

    /** Adaptif quiz ekranından çıkınca state'i temizle */
    fun clearAdaptiveQuestions() {
        _adaptiveQuestions.value = emptyList()
    }
}