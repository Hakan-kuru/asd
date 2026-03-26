package com.hakankuru.agesa_insurtech_codenight.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
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

    // LoginScreen'den gelen ID ile veri çekmeyi başlatan tetikleyici
    fun startFetching(userId: String) {
        if (userId.isEmpty()) {
            _userState.value = UserState.Error("Kullanıcı ID bulunamadı.")
            return
        }
        getUserData(userId)
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
}