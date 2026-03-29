/**
 * quizController.js - Dual-Bot Controller
 */

const { getSession, resetSession } = require('../services/sessionStore');
const { evaluateAnswers, getNextScenario, evaluateFollowup } = require('../services/quizService');
const { readJSON } = require('../utils/fileHandler');
const { GenerateNewQuestion } = require('../services/geminiService');

const scenarios = readJSON('scenarios_1.json');

/**
 * [POST] /api/submit-answers
 * Bot 1: Yorumlama
 */
exports.submitAnswers = async (req, res) => {
  try {
    const sessionId = req.header('X-Session-Id');
    if (!sessionId) return res.status(400).json({ error: "X-Session-Id headeri zorunludur." });

    const session = getSession(sessionId);
    const userAnswers = req.body;

    if (!Array.isArray(userAnswers) || userAnswers.length === 0) {
      return res.status(400).json({ error: "Body bir dizi olmalı: [{ scenario_index, chosen_option }]" });
    }

    // Bot 1 analizini yap
    const feedbacks = await evaluateAnswers(userAnswers, session, scenarios);

    return res.json({
      score: session.score,
      balance: session.balance,
      credit_debt: session.credit_debt,
      weakTopics: Object.keys(session.weakTopics).filter(k => session.weakTopics[k] >= 1),
      feedbacks // Burada 'interpretation' alanı var (2 cümlelik bot yorumu)
    });
  } catch (err) {
    console.error('[submitAnswers]', err.message);
    res.status(500).json({ error: 'Sunucu hatası' });
  }
};

/**
 * [GET] /api/next-question
 * Bot 2: Soru Üretim (veya JSON seed)
 */
exports.nextQuestion = async (req, res) => {
  try {
    const sessionId = req.header('X-Session-Id');
    if (!sessionId) return res.status(400).json({ error: "X-Session-Id headeri zorunludur." });

    const session = getSession(sessionId);
    const result = await getNextScenario(session, scenarios);

    if (!result) {
      return res.json({ message: 'Tüm sorular tamamlandı!', done: true });
    }

    // Ortak response objesi
    return res.json({
      type: result.type, // 'seed' veya 'generated'
      category: result.category,
      text: result.text,
      options: result.options,
      is_adaptive: result.is_adaptive,
      scenario_index: result.scenario_index // seed ise var
    });
  } catch (err) {
    console.error('[nextQuestion]', err.message);
    res.status(500).json({ error: 'Sunucu hatası' });
  }
};

/**
 * [POST] /api/submit-followup
 * Adaptif soruyu doğrula
 */
exports.submitFollowup = async (req, res) => {
  try {
    const sessionId = req.header('X-Session-Id');
    if (!sessionId) return res.status(400).json({ error: "X-Session-Id headeri zorunludur." });

    const session = getSession(sessionId);
    const { chosen_option } = req.body;

    if (!chosen_option) {
      return res.status(400).json({ error: "chosen_option zorunludur." });
    }

    const result = await evaluateFollowup(chosen_option, session);
    if (!result) {
      return res.status(400).json({ error: "Aktif bir adaptif/generated soru bulunamadı." });
    }

    return res.json(result);
  } catch (err) {
    console.error('[submitFollowup]', err.message);
    res.status(500).json({ error: 'Sunucu hatası' });
  }
};

/**
 * [POST] /api/reset
 */
exports.resetQuiz = (req, res) => {
  const sessionId = req.header('X-Session-Id');
  if (sessionId) resetSession(sessionId);
  res.json({ message: "Session sıfırlandı. Yeni botlarla başlayabilirsiniz." });
};

/**
 * [POST] /api/learn-from-mistakes
 * Android uygulamasından gelen JSON verisindeki hata kategorilerine göre yeni sorular üretir.
 */
exports.learnFromMistakes = async (req, res) => {
  try {
    const userAnswers = req.body; // Gelen JSON Array

    if (!Array.isArray(userAnswers) || userAnswers.length === 0) {
      return res.status(400).json({ error: "Body bir dizi olmalı, örneğin: [{ \"question_category\": \"...\", \"question_text\": \"...\", \"selected_method\": \"...\" }]" });
    }

    const uniqueCategories = new Set();
    userAnswers.forEach(ans => {
        if (ans.question_category) {
            uniqueCategories.add(ans.question_category);
        }
    });

    const newQuestions = [];
    let balance = 100;

    for (const category of uniqueCategories) {
        // İlgili kategorideki eğitici soruyu Gemini'den iste
        const geminiResult = await GenerateNewQuestion({ category, balance });
        
        // Gemini'den dönen cevabı AgeSA_Insurtech_Codenight app'in beklediği formata çevir
        const questionObj = {
            category: category,
            text: geminiResult.text || "Yapay zeka analiz hatası.",
            amount_percent: 0.1, // Sabit bir etki
            just_cash: true, // true olursa Banka veya Nakit seçenekleri çıkar, Yes/No yerine bunu kullanacağız çünkü UI mevcut olarak Banka/Nakit destekliyor
            answers: {
                cash: { explanation: "Nakit / Hayır" },
                bank: { explanation: geminiResult.explanation || "Doğru tercih tasarruf etmektir." },
                credit: { explanation: "Kredi kartı" }
            }
        };

        newQuestions.push(questionObj);
        
        // Rate limit için hafif gecikme
        await new Promise(r => setTimeout(r, 800));
    }

    return res.json(newQuestions);

  } catch (err) {
    console.error('[learnFromMistakes]', err.message);
    res.status(500).json({ error: 'Sunucu hatası: ' + err.message });
  }
};
