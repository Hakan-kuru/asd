package com.hakankuru.agesa_insurtech_codenight.data

import android.content.Context
import com.google.gson.Gson
import java.io.OutputStreamWriter

class QuestionManager(jsonString: String) {
    private val questions: List<FinanceQuestion>
    private var currentIndex = 0
    private val userResponses = mutableListOf<UserResponse>()

    init {
        questions = Gson().fromJson(jsonString, Array<FinanceQuestion>::class.java).toList().shuffled().take(10)
    }

    fun getCurrentQuestion(): FinanceQuestion? {
        return if (currentIndex < questions.size) questions[currentIndex] else null
    }

    fun nextQuestion() {
        currentIndex++
    }

    /**
     * Kullanıcının cevabını kaydet.
     * selectedAnswer: "CASH", "BANK" veya "CREDIT"
     */
    fun recordResponse(question: FinanceQuestion, selectedAnswer: String) {
        val answerKey = selectedAnswer.lowercase()
        val wasCorrect = when (answerKey) {
            "cash"   -> question.answers.cash.correct
            "bank"   -> question.answers.bank.correct
            "credit" -> question.answers.credit.correct
            else     -> false
        }
        userResponses.add(
            UserResponse(
                question_category = question.category,
                question_text     = question.text,
                selected_method   = selectedAnswer,
                was_correct       = wasCorrect
            )
        )
    }

    /**
     * Yanlış cevaplanmış benzersiz kategorileri döner.
     * Adaptive quiz'in learnFromMistakes mantığının Android karşılığı.
     */
    fun getWrongCategories(): List<String> {
        return userResponses
            .filter { !it.was_correct }
            .map { it.question_category }
            .distinct()
    }

    /**
     * Tüm cevapları JSON string olarak döner (debug veya sunucuya gönderim için).
     */
    fun getUserResponsesAsJson(): String {
        return Gson().toJson(userResponses)
    }

    fun saveAllResponsesToFile(context: Context) {
        try {
            val fileOutputStream = context.openFileOutput("user_responses.json", Context.MODE_PRIVATE)
            val writer = OutputStreamWriter(fileOutputStream)
            writer.write(getUserResponsesAsJson())
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}