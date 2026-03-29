package com.hakankuru.agesa_insurtech_codenight.data

data class FinanceQuestion(
    val day: Int,
    val month: Int,
    val category: String,
    val just_cash: Boolean,
    val text: String,
    val amount_percent: Double, // JSON'da 0.15 olduğu için Double
    val answers: AnswerOptions   // Yukarıda tanımladığımız iç obje
)
data class AnswerDetail(
    val correct: Boolean,
    val explanation: String
)

// "answers" objesinin içindeki seçenekler
data class AnswerOptions(
    val cash: AnswerDetail,
    val credit: AnswerDetail,
    val bank: AnswerDetail
)

// ──────────────────────────────────────────────────────────────
// Kullanıcının verdiği cevabın kaydı (hatalardan ders çıkar için)
// ──────────────────────────────────────────────────────────────
data class UserResponse(
    val question_category: String,
    val question_text: String,
    val selected_method: String,   // "CASH", "BANK", "CREDIT"
    val was_correct: Boolean       // Doğru cevaplayıp cevaplayamadığı
)

// ──────────────────────────────────────────────────────────────
// Gemini'nin ürettiği adaptif soru formatı
// ──────────────────────────────────────────────────────────────
data class AdaptiveQuestion(
    val category: String,
    val text: String,              // Soru metni
    val options: List<String>,     // ["yes", "no"]
    val correct_option: String,    // "yes" veya "no"
    val explanation: String        // Doğru cevabın açıklaması
)