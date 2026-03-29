const fs = require('fs');
const path = require('path');

const jsonPath = path.join(__dirname, 'app', 'src', 'main', 'res', 'raw', 'questions.json');
const data = JSON.parse(fs.readFileSync(jsonPath, 'utf8'));

const transformed = data.map(q => {
    // Determine just_cash loosely based on small amounts or food/transport
    const just_cash = q.amount_percent <= 0.08 || q.category === 'transport' || q.category === 'food';
    const originalExplain = q.explanation;
    const correctMethod = q.correct_method;
    
    let bankObj, cashObj, creditObj;
    
    if (correctMethod === 'bank') {
        bankObj = { correct: true, explanation: originalExplain };
        cashObj = { correct: false, explanation: "Nakit harcamak bütçeni sarsabilir. " + originalExplain };
        creditObj = { correct: false, explanation: "Krediyle harcama yapmak borcunu anlamsızca artırır. " + originalExplain };
    } else if (correctMethod === 'cash') {
        cashObj = { correct: true, explanation: originalExplain };
        bankObj = { correct: false, explanation: "Bu harcamayı ertelemek daha büyük/pahalı sorunlara yol açabilir. Nakit ödemelisin." };
        creditObj = { correct: false, explanation: "Nakit ödeyebileceğin tutarları krediyle alıp borç yükünü artırmamalısın." };
    } else if (correctMethod === 'credit') {
        creditObj = { correct: true, explanation: originalExplain };
        bankObj = { correct: false, explanation: "Bu mecburi ve yüksek bir gider, ertelemek mantıklı değil." };
        cashObj = { correct: false, explanation: "Bu yatırım/yüksek meblağ için tüm nakitini sıfırlamak risklidir, krediye yaymalısın." };
    }

    return {
        day: q.day,
        month: q.month,
        category: q.category,
        just_cash: just_cash,
        text: q.text,
        amount_percent: q.amount_percent,
        answers: {
            cash: cashObj,
            credit: creditObj,
            bank: bankObj
        }
    };
});

fs.writeFileSync(jsonPath, JSON.stringify(transformed, null, 2), 'utf8');
console.log('Successfully transformed questions.json');
