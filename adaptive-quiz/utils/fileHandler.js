/**
 * fileHandler.js
 * JSON dosyalarını senkron olarak okuyup parse eden yardımcı fonksiyonlar.
 */

const fs = require('fs');
const path = require('path');

/**
 * Verilen dosya adını /data klasöründen okuyup JSON olarak döner.
 * @param {string} filename - Örn: 'scenarios_1.json'
 * @returns {Array|Object} Parsed JSON içeriği
 */
function readJSON(filename) {
  const filePath = path.join(__dirname, '..', 'data', filename);
  const raw = fs.readFileSync(filePath, 'utf-8');
  return JSON.parse(raw);
}

module.exports = { readJSON };
