package com.hakankuru.agesa_insurtech_codenight.data

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * GeminiAdaptiveService
 *
 * adaptive-quiz/services/geminiService.js → GenerateNewQuestion() fonksiyonunun
 * Android/Kotlin karşılığı. Backend yok — doğrudan Gemini REST API'si çağrılır.
 */
object GeminiAdaptiveService {

    private const val TAG = "GeminiAdaptive"
    private const val API_KEY = "AIzaSyBEVafbpyoWqQBoxejqh0pqC8wKdAO6yAs"
    private const val MODEL   = "gemini-2.0-flash"
    private val BASE_URL get() =
        "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent?key=$API_KEY"

    // ──────────────────────────────────────────────────────────────────────────
    // PUBLIC: Verilen kategoriler listesi için adaptif sorular üretir
    // adaptive-quiz → learnFromMistakes endpoint'inin Android karşılığı
    // ──────────────────────────────────────────────────────────────────────────
    suspend fun generateQuestionsForCategories(
        wrongCategories: List<String>
    ): List<AdaptiveQuestion> = withContext(Dispatchers.IO) {

        val questions = mutableListOf<AdaptiveQuestion>()

        for (category in wrongCategories) {
            try {
                val question = generateSingleQuestion(category)
                questions.add(question)
                // Rate-limit için gecikme (adaptive-quiz'in 800ms'i)
                delay(800)
            } catch (e: Exception) {
                Log.w(TAG, "Kategori için soru üretilemedi: $category — ${e.message}")
                // Fallback soru (geminiService.js ile aynı mantık)
                questions.add(fallbackQuestion(category))
            }
        }

        questions
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PRIVATE: Tek bir kategori için Gemini'ye sor
    // adaptive-quiz → GenerateNewQuestion() prompt'ının birebir çevirisi
    // ──────────────────────────────────────────────────────────────────────────
    private suspend fun generateSingleQuestion(category: String): AdaptiveQuestion =
        withContext(Dispatchers.IO) {

            val prompt = """
Sen bir finansal eğitim botusun. 
Kullanıcı "$category" kategorisinde bir hata yaptı.

Görevin:
1. Bu kategoriyle ilgili, kullanıcının finansal bilgisini artıracak bir "Doğru mu/Yanlış mı?" veya "Mantıklı mı/Değil mi?" senaryosu üret.
2. Soru, bir şeyi sadece sormak yerine, içinde finansal bir ders/bilgi barındırmalı. (Örn: "Acil durum fonu olmadan yatırım yapmak risklidir, doğru mu?")
3. Seçenekler MUTLAKA ["yes", "no"] olmalı. (yes=Doğru/Evet, no=Yanlış/Hayır)
4. Yanıtını MUTLAKA aşağıdaki JSON formatında ver (başka bir şey yazma, markdown blok bile ekleme):

{"text": "Eğitici senaryo/soru metni burada (günlük dilde)","options": ["yes", "no"],"correct_option": "yes","explanation": "Bu durumun neden doğru veya yanlış olduğunun finansal gerekçesi (teknik olmayan, öğretici dil)"}
""".trimIndent()

            val rawResponse = callGeminiApi(prompt)
            parseAdaptiveQuestion(category, rawResponse)
        }

    // ──────────────────────────────────────────────────────────────────────────
    // HTTP isteği — HttpURLConnection (OkHttp gerektirmez)
    // ──────────────────────────────────────────────────────────────────────────
    private fun callGeminiApi(prompt: String): String {
        val requestBody = buildGeminiRequestBody(prompt)
        val url = URL(BASE_URL)
        val connection = url.openConnection() as HttpURLConnection

        return try {
            connection.apply {
                requestMethod    = "POST"
                connectTimeout   = 15_000
                readTimeout      = 30_000
                doOutput         = true
                setRequestProperty("Content-Type", "application/json")
            }

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use {
                it.write(requestBody)
                it.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val error = connection.errorStream?.bufferedReader()?.readText() ?: "HTTP $responseCode"
                Log.e(TAG, "API Hatası: $error")
                throw RuntimeException("Gemini API HTTP $responseCode")
            }

            BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8))
                .use { it.readText() }

        } finally {
            connection.disconnect()
        }
    }

    // Gemini REST API istek gövdesi
    private fun buildGeminiRequestBody(prompt: String): String {
        val escaped = prompt
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")

        return """
        {
          "contents": [
            {
              "parts": [
                { "text": "$escaped" }
              ]
            }
          ],
          "generationConfig": {
            "temperature": 0.7,
            "maxOutputTokens": 512
          }
        }
        """.trimIndent()
    }

    // Gemini'nin döndürdüğü ham JSON'dan AdaptiveQuestion çıkar
    private fun parseAdaptiveQuestion(category: String, rawApiResponse: String): AdaptiveQuestion {
        return try {
            // Önce Gemini API wrapper'ını aç
            val root  = JsonParser.parseString(rawApiResponse).asJsonObject
            val text  = root
                .getAsJsonArray("candidates")
                .get(0).asJsonObject
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).asJsonObject
                .get("text").asString

            // İçinden ```json ... ``` veya doğrudan JSON'u temizle
            val clean = text
                .replace(Regex("```json\\s*"), "")
                .replace(Regex("```\\s*"), "")
                .trim()

            // AdaptiveQuestion'a dönüştür
            val obj = JsonParser.parseString(clean).asJsonObject
            AdaptiveQuestion(
                category       = category,
                text           = obj.get("text").asString,
                options        = listOf("yes", "no"),
                correct_option = obj.get("correct_option").asString,
                explanation    = obj.get("explanation").asString
            )
        } catch (e: Exception) {
            Log.w(TAG, "Parse hatası: ${e.message}")
            fallbackQuestion(category)
        }
    }

    // Gemini yanıt veremezse veya parse başarısız olursa fallback
    private fun fallbackQuestion(category: String): AdaptiveQuestion {
        return AdaptiveQuestion(
            category       = category,
            text           = "${category.uppercase()} bilgisi: Gelirinin en az %10'unu her ay kenara ayırmak finansal özgürlük için doğru bir adım mıdır?",
            options        = listOf("yes", "no"),
            correct_option = "yes",
            explanation    = "Kendine ödeme yapmak (birikim), finansal sağlığın temelidir. Her ay düzenli biriktirmek sizi uzun vadede krizlere karşı korur."
        )
    }
}
