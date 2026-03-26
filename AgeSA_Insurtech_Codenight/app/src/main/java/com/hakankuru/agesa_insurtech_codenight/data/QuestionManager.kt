package com.hakankuru.agesa_insurtech_codenight.data

import android.content.Context
import com.google.gson.Gson // JSON parse için (build.gradle'a eklemelisin)
import com.hakankuru.agesa_insurtech_codenight.data.FinanceQuestion
import java.io.OutputStreamWriter

class QuestionManager(jsonString: String) {
    private val questions: List<FinanceQuestion>
    private var currentIndex = 0
    private val userResponses = mutableListOf<Map<String, String>>()

    init {
        // Gelen JSON string'ini listeye çeviriyor, karıştırıyor ve ilk 10'unu alıyoruz
        questions = Gson().fromJson(jsonString, Array<FinanceQuestion>::class.java).toList().shuffled().take(10)
    }

    fun getCurrentQuestion(): FinanceQuestion? {
        return if (currentIndex < questions.size) questions[currentIndex] else null
    }

    fun nextQuestion() {
        currentIndex++
    }

    fun recordResponse(question: FinanceQuestion, selectedAnswer: String) {
        val response = mapOf(
            "question_category" to question.category,
            "question_text" to question.text,
            "selected_method" to selectedAnswer
        )
        userResponses.add(response)
    }

    fun saveAllResponsesToFile(context: Context) {
        try {
            val fileOutputStream = context.openFileOutput("user_responses.json", Context.MODE_PRIVATE)
            val writer = OutputStreamWriter(fileOutputStream)
            writer.write(Gson().toJson(userResponses))
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}