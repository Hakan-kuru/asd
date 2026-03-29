/**
 * process_csv.js
 * CSV dosyalarını okur, hataları ayıklar ve Gemini ile analiz raporu (JSON) üretir.
 */

const fs = require('fs');
const path = require('path');
const { readJSON } = require('./utils/fileHandler');
const { InterpretMistake, GenerateNewQuestion } = require('./services/geminiService');

// Ayarlar
const INPUT_FILE = path.join(__dirname, 'input_results.csv');
const OUTPUT_FILE = path.join(__dirname, 'output_analysis.json');
const SCENARIOS = readJSON('scenarios_1.json');

/**
 * CSV satırını parse eder (Basit CSV parser)
 */
function parseCSV(content) {
  const lines = content.split('\n').filter(line => line.trim() !== '');
  const headers = lines[0].split(',').map(h => h.trim());
  
  return lines.slice(1).map(line => {
    const values = line.split(',').map(v => v.trim());
    const obj = {};
    headers.forEach((header, i) => {
      obj[header] = values[i];
    });
    return obj;
  });
}

/**
 * Ana işlem döngüsü
 */
async function run() {
  console.log('🚀 CSV İşleme Başlatıldı...');

  if (!fs.existsSync(INPUT_FILE)) {
    console.error(`❌ Hata: ${INPUT_FILE} bulunamadı!`);
    return;
  }

  const csvContent = fs.readFileSync(INPUT_FILE, 'utf-8');
  const rows = parseCSV(csvContent);
  const mistakes = rows.filter(row => String(row.is_correct).toLowerCase() === 'false');

  console.log(`📊 Toplam satır: ${rows.length}, Hata sayısı: ${mistakes.length}`);

  const results = [];

  for (const [index, row] of mistakes.entries()) {
    const scenarioIdx = parseInt(row.scenario_index);
    const scenario = SCENARIOS[scenarioIdx];

    if (!scenario) {
      console.warn(`⚠️ Senaryo bulunamadı (Index: ${scenarioIdx})`);
      continue;
    }

    console.log(`[${index + 1}/${mistakes.length}] Analiz ediliyor: ${scenario.category}...`);

    try {
      // Bot 1: Yorumlama
      const yorum = await InterpretMistake({
        scenario,
        chosenOption: row.chosen_option,
        isCorrect: false
      });

      // Bot 2: Eğitici Soru Üretimi
      const yeniSoru = await GenerateNewQuestion({
        category: scenario.category,
        balance: 100 // Varsayılan/Statik bakiye (CSV'de yoksa)
      });

      results.push({
        hata_no: index + 1,
        kategori: scenario.category,
        yapilan_yanlis_secim: row.chosen_option,
        orijinal_senaryo: scenario.text,
        bot_1_yorum: yorum,
        bot_2_egitici_soru: yeniSoru
      });

      // API Limitlerini korumak için kısa bekleme
      await new Promise(res => setTimeout(res, 500));
    } catch (err) {
      console.error(`❌ Hata oluştu (Satır ${index}):`, err.message);
    }
  }

  // JSON Kaydetme
  fs.writeFileSync(OUTPUT_FILE, JSON.stringify(results, null, 2), 'utf-8');
  console.log(`✅ İşlem Tamamlandı! Rapor: ${OUTPUT_FILE}`);
}

run();
