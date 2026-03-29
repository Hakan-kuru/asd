//package com.hakankuru.agesa_insurtech_codenight.viewmodel
//
//import android.util.Log
//import androidx.compose.runtime.mutableStateOf
//import com.google.firebase.firestore.FirebaseFirestore
//import com.hakankuru.agesa_insurtech_codenight.data.User
//
//class FinanceViewModel {
//    private val db = FirebaseFirestore.getInstance()
//
//    // UI'da gözükecek canlı kullanıcı durumu
//    var currentUser = mutableStateOf<User?>(null)
//        private set
//
//    /**
//     * Kullanıcının finansal durumunu hem yerelde hem de Firebase'de günceller.
//     * @param cost Harcanacak miktar
//     * @param budgetType "CASH" veya "CREDIT"
//     */
//    fun updateUserFinance(cost: Double, budgetType: String) {
//        val user = currentUser.value ?: return // Kullanıcı yoksa işlem yapma
//
//        // 1. Mantıksal Güncelleme (Local State)
//        val updatedCash = if (budgetType == "CASH") user.cash - cost else user.cash
//        val updatedCredit = if (budgetType == "CREDIT") user.credit - cost else user.credit
//
//        // Yerel objeyi güncelle (UI anında tepki versin diye)
//        val updatedUser = user.copy(cash = updatedCash, credit = updatedCredit)
//        currentUser.value = updatedUser
//
//        // 2. Firebase Güncelleme (Arka Plan)
//        // Dürüst olayım: Eğer internet yoksa bu işlem 'Failure' döner.
//        // Gerçek bir projede 'offline persistence' açık olmalı.
//        db.collection("users").document(user.id)
//            .update(
//                "cash", updatedCash,
//                "credit", updatedCredit
//            )
//            .addOnSuccessListener {
//                Log.d("Finance", "Bütçe başarıyla güncellendi: Cash: $updatedCash, Credit: $updatedCredit")
//            }
//            .addOnFailureListener { e ->
//                Log.e("Finance", "Güncelleme hatası! Eskiye dönülüyor...", e)
//                // Hata durumunda yerel durumu eski haline getirebilirsin (Rollback)
//                currentUser.value = user
//            }
//    }
//}