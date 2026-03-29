/**
 * sessionStore.js
 * In-memory session yönetimi. Her kullanıcı X-Session-Id header'ı ile ayırt edilir.
 * Sunucu yeniden başlatılınca tüm session'lar sıfırlanır (DB yok, tasarım gereği).
 */

// sessionId → session objesi
const sessions = {};

/**
 * Yeni bir session oluşturur (başlangıç durumu).
 */
function createSession(sessionId) {
  sessions[sessionId] = {
    balance: 100,          // Başlangıç bütçesi (100 birim = %100)
    credit_debt: 0,        // Kredi borcu (amount_percent cinsinden birikir)
    score: 0,
    weakTopics: {},        // { category: yanlışSayısı }
    askedIndices: [],      // Sorulmuş senaryo indeksleri
    pendingAdaptive: null, // Aktif adaptif senaryo { index, scenario }
    pendingGenerated: null, // Yeni botun ürettiği soru burada tutulur
  };
  return sessions[sessionId];
}

/**
 * Mevcut session'ı getirir; yoksa yeni oluşturur.
 * @param {string} sessionId
 * @returns {Object} session
 */
function getSession(sessionId) {
  if (!sessions[sessionId]) {
    return createSession(sessionId);
  }
  return sessions[sessionId];
}

/**
 * Session'ı sıfırlar (yeni oyun başlatmak için).
 * @param {string} sessionId
 */
function resetSession(sessionId) {
  return createSession(sessionId);
}

module.exports = { getSession, resetSession };
