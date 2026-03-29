/**
 * index.js
 * Uygulamanın giriş noktası. Express sunucusunu başlatır.
 */

require('dotenv').config();
const express = require('express');
const cors = require('cors');
const quizRoutes = require('./routes/quizRoutes');

const app = express();
const PORT = process.env.PORT || 3000;

// ─── Middleware ───────────────────────────────
app.use(cors());
app.use(express.json());

// ─── Routes ──────────────────────────────────
app.use('/api', quizRoutes);

// ─── Sağlık kontrolü ─────────────────────────
app.get('/health', (req, res) => {
  res.json({ status: 'ok', message: 'Adaptive Quiz API çalışıyor.' });
});

app.get('/', (req, res) => {
  res.send('<h1>Adaptive Quiz API Çalışıyor!</h1><p>Android uygulamasından testi tamamlayıp "Hatalarından Ders Çıkar" butonuna basabilirsiniz.</p>');
});

// ─── 404 handler ─────────────────────────────
app.use((req, res) => {
  res.status(404).json({ error: `Endpoint bulunamadı: ${req.method} ${req.url}` });
});

// ─── Global hata handler ─────────────────────
app.use((err, req, res, _next) => {
  console.error('[Unhandled Error]', err.message);
  res.status(500).json({ error: 'Beklenmedik sunucu hatası.' });
});

// ─── Sunucuyu başlat ─────────────────────────
app.listen(PORT, () => {
  console.log(`✅ Adaptive Quiz API http://localhost:${PORT} adresinde çalışıyor`);
  console.log(`📚 Senaryo sayısı: ${require('./utils/fileHandler').readJSON('scenarios_1.json').length}`);
});
