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