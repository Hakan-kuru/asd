/**
 * geminiService.js - Çift Botlu Mimari
 * Bot 1: InterpretMistake (Yorumlama Botu)
 * Bot 2: GenerateNewQuestion (Soru Üretim Botu)
 */

const { GoogleGenAI } = require('@google/genai');

const genAI = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY });
const MODEL = 'gemini-2.0-flash';

/**
 * Gemini'ye bir prompt gönderir.
 */
async function callGemini(prompt) {
  try {
    const response = await genAI.models.generateContent({
      model: MODEL,
      contents: prompt,
    });
    return response.text.trim();
  } catch (err) {
    console.warn('[Gemini] API hatası:', err?.message ?? err);
    return null;
  }
}

/**
 * BOT 1: InterpretMistake
 * Kullanıcının neden yanlış yaptığını günlük dille (2 cümle) açıklar.
 */
async function InterpretMistake({ scenario, chosenOption, isCorrect }) {
  if (isCorrect) return "Harika bir karar! Bütçeni korudun.";

  const prompt = `
Sen bir finansal koçsun. Kullanıcı şu kararı verdi: "${scenario.text}" 
Seçeneği: "${chosenOption}" (BU YANLIŞ BİR KARAR)
Cevabın teknik açıklaması: "${scenario.answers[chosenOption]?.explanation}"

Görevin:
1. Kullanıcıya bu kararın neden yanlış olduğunu günlük dille (sivil dil, "pazar dili", teknik olmayan) anlat.
2. Kesinlikle ve sadece TAM 2 CÜMLE olsun. Ne eksik ne fazla.
3. Samimi bir üslup kullan (Örn: "Bak güzel kardeşim...", "O iş öyle değil...").
`.trim();

  const feedback = await callGemini(prompt);
  return feedback || `Bak şimdi, bu harcama bütçeni sarsabilir. ${scenario.answers[chosenOption]?.explanation || 'Dikkatli olmalısın.'}`;
}

/**
 * BOT 2: GenerateNewQuestion (Eğitici Mod)
 * Kullanıcının hata yaptığı konuya dair Doğru/Yanlış formatında eğitici bir soru üretir.
 */
async function GenerateNewQuestion({ category, balance }) {
  const prompt = `
Sen bir finansal eğitim botusun. 
Kullanıcı "${category}" kategorisinde bir hata yaptı. 
Şu anki bakiye durumu: %${balance.toFixed(1)}

Görevin:
1. Bu kategoriyle ilgili, kullanıcının finansal bilgisini artıracak bir "Doğru mu/Yanlış mı?" veya "Mantıklı mı/Değil mi?" senaryosu üret.
2. Soru, bir şeyi sadece sormak yerine, içinde finansal bir ders/bilgi barındırmalı. (Örn: "Acil durum fonu olmadan yatırım yapmak risklidir, doğru mu?")
3. Seçenekler MUTLAKA ["yes", "no"] olmalı. (yes=Doğru/Evet, no=Yanlış/Hayır)
4. Yanıtını MUTLAKA aşağıdaki JSON formatında ver (başka bir şey yazma):

{
  "text": "Eğitici senaryo/soru metni burada (günlük dilde)",
  "options": ["yes", "no"],
  "correct_option": "yes", 
  "explanation": "Bu durumun neden doğru veya yanlış olduğunun finansal gerekçesi (teknik olmayan, öğretici dil)"
}
`.trim();

  const raw = await callGemini(prompt);
  try {
    const clean = raw.replace(/```json|```/g, '').trim();
    return JSON.parse(clean);
  } catch {
    // Fallback: Gemini yanıt vermezse veya parse edilemezse
    return {
      text: `${category.toUpperCase()} kuralı: Gelirinin %10'unu her ay kenara ayırmak finansal özgürlük için doğru bir adımdır, değil mi?`,
      options: ["yes", "no"],
      correct_option: "yes",
      explanation: "Kendine ödeme yapmak (birikim), finansal sağlığın temelidir."
    };
  }
}

module.exports = { InterpretMistake, GenerateNewQuestion };
