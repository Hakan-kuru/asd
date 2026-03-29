/**
 * quizService.js - Çift Botlu İş Mantığı
 */

const { InterpretMistake, GenerateNewQuestion } = require('./geminiService');

// Zayıf konu eşiği: 1 yanlış bile olsa o konudan yeni soru üretilebilir (Hassas mod)
const WEAK_THRESHOLD = 1;

/**
 * evaluateAnswers
 * Bot 1 (InterpretMistake) kullanarak cevapları analiz eder.
 */
async function evaluateAnswers(answers, session, scenarios) {
  const feedbacks = [];
  let scoreDelta = 0;

  for (const ans of answers) {
    const scenario = scenarios[ans.scenario_index];
    if (!scenario) continue;

    const answerData = scenario.answers[ans.chosen_option];
    const isCorrect = answerData?.correct ?? false;

    // Session update
    session.askedIndices.push(ans.scenario_index);

    if (isCorrect) {
      scoreDelta += 10;
      session.balance -= (scenario.amount_percent * 100);
    } else {
      // Yanlış! Zayıf konu sayacı artar
      session.weakTopics[scenario.category] = (session.weakTopics[scenario.category] || 0) + 1;
      if (ans.chosen_option === 'credit') {
        session.credit_debt += (scenario.amount_percent * 100);
      } else {
        session.balance -= (scenario.amount_percent * 100);
      }
    }

    // Bot 1: Yorumlama
    const interpretation = await InterpretMistake({ scenario, chosenOption: ans.chosen_option, isCorrect });

    feedbacks.push({
      scenario_index: ans.scenario_index,
      is_correct: isCorrect,
      interpretation, // 2 cümlelik günlük dilli açıklama
      original_explanation: answerData?.explanation
    });
  }

  session.score += scoreDelta;
  return feedbacks;
}

/**
 * getNextScenario
 * Bot 2 (GenerateNewQuestion) veya JSON'dan başlangıç sorusu seçer.
 */
async function getNextScenario(session, scenarios) {
  // En çok hata yapılan kategoriyi bul (zayıf konu varsa)
  const weakCategories = Object.entries(session.weakTopics)
    .filter(([, count]) => count >= WEAK_THRESHOLD)
    .sort((a, b) => b[1] - a[1]);

  if (weakCategories.length > 0) {
    const [targetCategory] = weakCategories[0];
    
    // BOT 2: Soru Üreter
    const generated = await GenerateNewQuestion({ category: targetCategory, balance: session.balance });
    
    // Üretilen soruyu session'a kaydet (doğrulama için)
    session.pendingGenerated = {
      category: targetCategory,
      ...generated
    };

    return {
      type: 'generated',
      category: targetCategory,
      text: generated.text,
      options: generated.options,
      is_adaptive: true
    };
  }

  // Zayıf konu yoksa JSON'dan sıradaki soruyu al (Seed)
  const nextIndex = scenarios.findIndex((_, i) => !session.askedIndices.includes(i));
  if (nextIndex === -1) return null;

  const scenario = scenarios[nextIndex];
  return {
    type: 'seed',
    scenario_index: nextIndex,
    category: scenario.category,
    text: scenario.text,
    options: scenario.just_cash ? ['yes', 'no'] : ['cash', 'credit', 'bank'],
    is_adaptive: false
  };
}

/**
 * evaluateFollowup
 * Bot 2'nin ürettiği soruyu kontrol eder.
 */
async function evaluateFollowup(chosenOption, session) {
  const pending = session.pendingGenerated;
  if (!pending) return null;

  const isCorrect = chosenOption === pending.correct_option;
  const delta = isCorrect ? 15 : 0; // Adaptif soru bonusu

  session.score += delta;
  session.pendingGenerated = null; // Soru kullanıldı

  return {
    correct: isCorrect,
    delta,
    score: session.score,
    feedback: isCorrect ? "Süper, konuyu kavradın!" : `Dostum, hala biraz çalışman lazım. ${pending.explanation}`
  };
}

module.exports = { evaluateAnswers, getNextScenario, evaluateFollowup };
