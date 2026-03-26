package com.hakankuru.agesa_insurtech_codenight.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.jvm.java
import com.hakankuru.agesa_insurtech_codenight.data.User

// UI'ın farklı durumlarını temsil eden Sealed Class
sealed class UserState {
    object Loading : UserState()
    data class Success(val user: User) : UserState()
    data class Error(val message: String) : UserState()
}

class UserViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // UI'ın dinleyeceği state
    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    init {
        // ViewModel oluştuğunda veriyi çekmeye başla
        getUserData()
    }

    private fun getUserData() {
        viewModelScope.launch {
            _userState.value = UserState.Loading

            // ÖRNEK: "users" koleksiyonundaki belirli bir dökümanı çekiyoruz.
            // Gerçek projede buraya giriş yapmış kullanıcının ID'si gelir (Firebase Auth).
            // Şimdilik test için Firestore'da manuel oluşturduğun bir döküman ID'sini yaz.
            val testDocId = "KULLANICI_DOKUMAN_ID_BURAYA_YAZ"

            db.collection("users").document(testDocId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _userState.value = UserState.Error(error.message ?: "Bilinmeyen hata")
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        // Firebase veriyi otomatik olarak User nesnesine çevirir
                        val user = snapshot.toObject(User::class.java)
                        if (user != null) {
                            _userState.value = UserState.Success(user)
                        } else {
                            _userState.value = UserState.Error("Veri dönüştürülemedi")
                        }
                    } else {
                        _userState.value = UserState.Error("Kullanıcı bulunamadı")
                    }
                }
        }
    }
}