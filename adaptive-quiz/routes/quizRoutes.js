/**
 * quizRoutes.js
 * Tüm quiz endpoint'lerinin route tanımları.
 */

const express = require('express');
const router = express.Router();
const {
  submitAnswers,
  nextQuestion,
  submitFollowup,
  resetQuiz,
  learnFromMistakes,
} = require('../controllers/quizController');

// Toplu cevap gönderimi ve değerlendirme
router.post('/submit-answers', submitAnswers);

// Sıradaki (veya adaptif) soruyu getir
router.get('/next-question', nextQuestion);

// Adaptif soruya verilen cevabı değerlendir
router.post('/submit-followup', submitFollowup);

// Session'ı sıfırla (yeni oyun)
router.post('/reset', resetQuiz);

// Hatalardan ders çıkar (Yeni Eğitici/Adaptif Quiz)
router.post('/learn-from-mistakes', learnFromMistakes);

module.exports = router;
