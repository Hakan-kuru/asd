# Adaptive Financial Quiz — Backend

Node.js + Express backend. Kullanıcı kararlarını analiz eden, bakiye simüle eden ve Gemini API ile kişiselleştirilmiş finansal koçluk üretir.

---

## Kurulum

### 1. Bağımlılıkları yükle
```bash
npm install
```

### 2. `.env` dosyasını düzenle
```
PORT=3000
GEMINI_API_KEY=buraya_api_anahtarınızı_yazın
```

Gemini API anahtarı için: [https://aistudio.google.com/app/apikey](https://aistudio.google.com/app/apikey)

### 3. Sunucuyu başlat
```bash
npm start
```

---

## API Kullanımı

> Her istekte `X-Session-Id` header'ı zorunludur (örn: `user-123`).

---

### POST `/api/submit-answers` — Toplu Cevap Gönder

```bash
curl -X POST http://localhost:3000/api/submit-answers \
  -H "Content-Type: application/json" \
  -H "X-Session-Id: user-123" \
  -d '[
    { "scenario_index": 0, "chosen_option": "bank" },
    { "scenario_index": 1, "chosen_option": "cash" },
    { "scenario_index": 2, "chosen_option": "credit" },
    { "scenario_index": 4, "chosen_option": "cash" }
  ]'
```

**Örnek Yanıt:**
```json
{
  "score": 20,
  "balance": 80.0,
  "credit_debt": 0.0,
  "weakTopics": ["shopping", "entertainment"],
  "feedbacks": [
    {
      "scenario_index": 0,
      "category": "food",
      "chosen_option": "bank",
      "is_correct": true,
      "explanation": "Evde yemek yaparak 800 TL tasarruf ettin...",
      "feedback": "Harika karar! Ay başında bütçeni korudun...",
      "hint": null
    }
  ]
}
```

---

### GET `/api/next-question` — Sıradaki Soruyu Al

```bash
curl http://localhost:3000/api/next-question \
  -H "X-Session-Id: user-123"
```

**Örnek Yanıt (normal):**
```json
{
  "scenario_index": 5,
  "category": "bes",
  "day": 6,
  "month": 1,
  "text": "BES danışmanın aradı: 600 TL katkı payı...",
  "amount_percent": 0.05,
  "options": ["yes", "no"],
  "is_adaptive": false,
  "rationale": null
}
```

**Örnek Yanıt (adaptif — zayıf konu tespit edildi):**
```json
{
  "scenario_index": 12,
  "category": "food",
  "text": "Doğum günü partisi organize ediyorsun...",
  "amount_percent": 0.22,
  "options": ["cash", "credit", "bank"],
  "is_adaptive": true,
  "rationale": "\"food\" kategorisinde zayıf performans tespit edildi. Adaptif soru devreye alındı."
}
```

---

### POST `/api/submit-followup` — Adaptif Soruyu Yanıtla

```bash
curl -X POST http://localhost:3000/api/submit-followup \
  -H "Content-Type: application/json" \
  -H "X-Session-Id: user-123" \
  -d '{ "chosen_option": "bank" }'
```

**Örnek Yanıt:**
```json
{
  "correct": true,
  "delta": 15,
  "score": 35,
  "balance": 78.0,
  "credit_debt": 0.0,
  "feedback": "Harika! Food konusunda doğru karar verdin. Artı 5 bonus puan kazandın...",
  "hint": "💡 FOOD ipucu: Evde sade kutlama yaptın..."
}
```

---

### POST `/api/reset` — Yeni Oyun Başlat

```bash
curl -X POST http://localhost:3000/api/reset \
  -H "X-Session-Id: user-123"
```

---

## Seçenek Türleri

| `just_cash` | Geçerli Seçenekler |
|---|---|
| `false` | `cash`, `credit`, `bank` |
| `true` | `yes`, `no` |

## Puanlama

| Olay | Puan |
|---|---|
| Doğru cevap (normal) | +10 |
| Yanlış cevap | 0 |
| Doğru cevap (adaptif, zayıf konu) | **+15** |

---

## Proje Yapısı

```
adaptive-quiz/
├── index.js                  ← Giriş noktası
├── .env                      ← Kimlik bilgileri
├── data/
│   └── scenarios_1.json      ← Senaryo veritabanı
├── utils/
│   └── fileHandler.js
├── services/
│   ├── geminiService.js      ← EvaluateResponse + PickAdaptiveScenario
│   ├── quizService.js        ← Çekirdek quiz mantığı
│   └── sessionStore.js       ← In-memory session
├── controllers/
│   └── quizController.js
└── routes/
    └── quizRoutes.js
```
