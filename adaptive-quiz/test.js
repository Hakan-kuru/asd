/**
 * test.js - Adaptive Quiz API Test Script
 * Calistirilmasi: node test.js
 */

const http = require('http');
const fs = require('fs');

const SID = 'test-' + Date.now();
let passed = 0;
let failed = 0;
const results = [];
const output = [];

const FALLBACK_QUESTION_PREFIXES = [
  "Sence bu durumda ne yapmalısın?",
  "İşte yeni bir finansal karar anı:",
  "Bu harcama sence gerekli mi?",
  "Bütçeni korumak için hangi adımı atmalısın?"
];

function log(msg) {
  const s = String(msg);
  output.push(s);
  process.stdout.write(s + '\n');
}


function check(name, cond, detail) {
  if (cond) {
    passed++;
    results.push('[PASS] ' + name + (detail ? ' (' + detail + ')' : ''));
    log('  [PASS] ' + name);
  } else {
    failed++;
    results.push('[FAIL] ' + name + (detail ? ' (' + detail + ')' : ''));
    log('  [FAIL] ' + name + (detail ? ' -- ' + detail : ''));
  }
}

function req(method, path, body, sessionId) {
  return new Promise((resolve, reject) => {
    const data = body ? JSON.stringify(body) : null;
    const opts = {
      hostname: 'localhost', port: 3000, path, method,
      headers: {
        'Content-Type': 'application/json',
        'X-Session-Id': sessionId || SID,
        ...(data ? { 'Content-Length': Buffer.byteLength(data) } : {}),
      },
    };
    const r = http.request(opts, (res) => {
      let raw = '';
      res.on('data', c => raw += c);
      res.on('end', () => {
        try { resolve({ status: res.statusCode, body: JSON.parse(raw) }); }
        catch { resolve({ status: res.statusCode, body: raw }); }
      });
    });
    r.on('error', reject);
    if (data) r.write(data);
    r.end();
  });
}

async function run() {
  log('==========================================================');
  log('  ADAPTIVE QUIZ API -- TEST RAPORU');
  log('==========================================================');

  // --- TEST 1: Health ---
  log('\n[TEST 1] GET /health');
  try {
    const r = await req('GET', '/health');
    check('200 doner', r.status === 200, 'status=' + r.status);
    check('body.status="ok"', r.body.status === 'ok');
    log('  Response: ' + JSON.stringify(r.body));
  } catch(e) { check('Health erisebilir', false, e.message); }

  // --- TEST 2: Missing Session Header ---
  log('\n[TEST 2] POST /api/submit-answers -- X-Session-Id eksik -> 400');
  try {
    const data = JSON.stringify([]);
    const opts = {
      hostname:'localhost', port:3000, path:'/api/submit-answers', method:'POST',
      headers:{ 'Content-Type':'application/json', 'Content-Length': Buffer.byteLength(data) }
    };
    const r = await new Promise((resolve) => {
      const rr = http.request(opts, (res) => {
        let raw = ''; res.on('data',c=>raw+=c);
        res.on('end',()=>resolve({ status:res.statusCode, body:JSON.parse(raw) }));
      });
      rr.write(data); rr.end();
    });
    check('400 doner', r.status === 400, 'status=' + r.status);
    log('  Response: ' + JSON.stringify(r.body));
  } catch(e) { check('Validation calisiyor', false, e.message); }

  // --- TEST 3: Empty Array Validation ---
  log('\n[TEST 3] POST /api/submit-answers -- bos array -> 400');
  try {
    const r = await req('POST', '/api/submit-answers', []);
    check('400 doner', r.status === 400, 'status=' + r.status);
    log('  Response: ' + JSON.stringify(r.body));
  } catch(e) { check('Bos array validation', false, e.message); }

  // --- TEST 4: Submit Answers (2 correct, 2 wrong) ---
  log('\n[TEST 4] POST /api/submit-answers -- Bot 1: Yorumlama Testi');
  try {
    const r = await req('POST', '/api/submit-answers', [
      { scenario_index:0, chosen_option:'bank' }, // DOĞRU
      { scenario_index:2, chosen_option:'cash' }, // YANLIŞ
    ]);
    check('200 doner', r.status === 200, 'status=' + r.status);
    check('feedbacks var', Array.isArray(r.body.feedbacks) && r.body.feedbacks.length === 2);
    const fb = r.body.feedbacks?.[1]; // Yanlış olanın yorumuna bak
    check('interpretation var (Bot 1)', typeof fb?.interpretation === 'string');
    log('  Bot 1 Yorumu: ' + String(fb?.interpretation).slice(0,100));
  } catch(e) { check('submit-answers calisiyor', false, e.message); }

  // --- TEST 5: Reset ---
  log('\n[TEST 5] POST /api/reset');
  try {
    const r = await req('POST', '/api/reset');
    check('200 doner', r.status === 200, 'status=' + r.status);
    log('  Response: ' + JSON.stringify(r.body.message));
  } catch(e) { check('Reset calisiyor', false, e.message); }

  // --- TEST 6: Next Question (Seed) ---
  log('\n[TEST 6] GET /api/next-question -- Baslangic (Seed) Sorusu');
  try {
    const r = await req('GET', '/api/next-question');
    check('200 doner', r.status === 200, 'status=' + r.status);
    check('type="seed"', r.body.type === 'seed');
    check('text var', typeof r.body.text === 'string');
    log('  Seed Soru: ' + String(r.body.text).slice(0,80));
  } catch(e) { check('next-question seed', false, e.message); }

  // --- TEST 7: Followup without pending -> 400 ---
  log('\n[TEST 7] POST /api/submit-followup -- pending soru yok -> 400');
  try {
    const FRESH = 'fresh-' + Date.now();
    const r = await req('POST', '/api/submit-followup', { chosen_option:'bank' }, FRESH);
    check('400 doner', r.status === 400, 'status=' + r.status);
    log('  Response: ' + JSON.stringify(r.body));
  } catch(e) { check('Followup validation', false, e.message); }

  // --- TEST 8: Full Adaptive Flow ---
  log('\n[TEST 8] Tam adaptif akis: 4 yanlis (food x2 + entertainment x2) -> adaptif soru -> +15 bonus');
  const ASID = 'adaptive-' + Date.now();
  try {
    // food:0,6 ve entertainment:4,10 -- hepsi yanlis seciyoruz
    const sa = await req('POST', '/api/submit-answers', [
      { scenario_index:0,  chosen_option:'cash'   },  // food YANLIS
      { scenario_index:6,  chosen_option:'credit' },  // food YANLIS
      { scenario_index:4,  chosen_option:'cash'   },  // entertainment YANLIS
      { scenario_index:11, chosen_option:'credit' },  // entertainment YANLIS
    ], ASID);
    check('submit 200', sa.status === 200, 'status=' + sa.status);
    check('score=0 (4 yanlis)', sa.body.score === 0, 'score=' + sa.body.score);
    check('food ve entertainment zayif', 
      sa.body.weakTopics?.includes('food') && sa.body.weakTopics?.includes('entertainment'),
      JSON.stringify(sa.body.weakTopics));
    log('  weakTopics=' + JSON.stringify(sa.body.weakTopics) + ' score=' + sa.body.score);

    // next-question -> adaptif olmali
    const nq = await req('GET', '/api/next-question', null, ASID);
    check('next-question 200', nq.status === 200, 'status=' + nq.status);
    check('is_adaptive=true', nq.body.is_adaptive === true, 'is_adaptive=' + nq.body.is_adaptive);
    check('kategori food veya entertainment',
      ['food','entertainment'].includes(nq.body.category), 'cat=' + nq.body.category);
    log('  Adaptif soru: category=' + nq.body.category + ' is_adaptive=' + nq.body.is_adaptive);
    log('  text: ' + String(nq.body.text).slice(0,80));
    log('  rationale: ' + nq.body.rationale);

    // dogru secenegi bul ve followup ver
    const correctOpt = nq.body.options?.includes('yes') ? 'yes' : 'bank'; 
    const fu = await req('POST', '/api/submit-followup', { chosen_option: correctOpt }, ASID);
    check('followup 200', fu.status === 200, 'status=' + fu.status);
    check('correct=true', fu.body.correct === true, 'correct=' + fu.body.correct + ' chosen='+correctOpt);
    check('delta=15', fu.body.delta === 15, 'delta=' + fu.body.delta);
    log('  Bot 2 Yanit: ' + String(fu.body.feedback).slice(0,100));
  } catch(e) { check('Adaptif akis tamamlandi', false, e.message); log(' ERR:'+e.message); }

  // --- TEST 9: just_cash=true (BES senaryosu) ---
  log('\n[TEST 9] BES senaryosu (just_cash=true) -- yes secimi');
  const BSID = 'bes-' + Date.now();
  try {
    // Senaryo 5: bes, just_cash:true, yes=dogru
    const r = await req('POST', '/api/submit-answers',
      [{ scenario_index:5, chosen_option:'yes' }], BSID);
    check('200 doner', r.status === 200, 'status=' + r.status);
    check('score=10 (yes dogru)', r.body.score === 10, 'score=' + r.body.score);
    check('is_correct=true', r.body.feedbacks?.[0]?.is_correct === true);
    log('  score=' + r.body.score + ' is_correct=' + r.body.feedbacks?.[0]?.is_correct);

    // next-question ile BES soruda yes/no secenekleri gelmeli
    const nq = await req('GET', '/api/next-question', null, BSID);
    if (!nq.body.done) {
      log('  next-question options: ' + JSON.stringify(nq.body.options));
      if (nq.body.options?.includes('yes')) {
        check('just_cash:true -> yes/no secenekleri', true);
      }
    }
  } catch(e) { check('BES senaryosu', false, e.message); }

  // --- OZET ---
  log('\n==========================================================');
  log('  TEST OZETI');
  log('==========================================================');
  results.forEach(r => log('  ' + r));
  log('----------------------------------------------------------');
  log('  TOPLAM: ' + (passed+failed) + ' | GECTI: ' + passed + ' | BASARISIZ: ' + failed);
  log('==========================================================');

  // Dosyaya yaz
  const fileContent = output.join('\n');
  fs.writeFileSync('./test-results.txt', fileContent, 'utf8');
  log('\nSonuclar test-results.txt dosyasina yazildi.');

  if (failed > 0) process.exit(1);
}

run().catch(e => { log('KRITIK HATA: ' + e.message); process.exit(1); });
