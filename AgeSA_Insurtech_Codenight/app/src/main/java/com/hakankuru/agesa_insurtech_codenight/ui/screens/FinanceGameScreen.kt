package com.hakankuru.agesa_insurtech_codenight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hakankuru.agesa_insurtech_codenight.SwipeableCard
import com.hakankuru.agesa_insurtech_codenight.data.QuestionManager
import com.hakankuru.agesa_insurtech_codenight.viewmodel.UserViewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun FinanceGameScreen(manager: QuestionManager, viewModel: UserViewModel, onTestFinished: () -> Unit) {
    var currentQuestion by remember { mutableStateOf(manager.getCurrentQuestion()) }
    var showExplanation by remember { mutableStateOf<String?>(null) }
    var showSelectionDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        if (currentQuestion != null && showExplanation == null) {
            SwipeableCard(
                category = currentQuestion!!.category,
                question = currentQuestion!!.text,
                isJustCash = currentQuestion!!.just_cash,
                onSwiped = { choice ->
                    when (choice) {
                        "BANK" -> {
                            manager.recordResponse(currentQuestion!!, "BANK")
                            showExplanation = currentQuestion!!.answers.bank.explanation
                        }
                        "CASH" -> {
                            manager.recordResponse(currentQuestion!!, "CASH")
                            viewModel.updateUserFinance(currentQuestion!!.amount_percent, "CASH")
                            showExplanation = currentQuestion!!.answers.cash.explanation
                        }
                        "DOUBLE_LEFT_TRIGGER" -> showSelectionDialog = true
                    }
                }
            )
        } else if (currentQuestion == null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Test Bitti!", textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onTestFinished) {
                    Text("Ana Menüye Dön")
                }
            }
        }

        // Açıklama Diyaloğu
        showExplanation?.let { msg ->
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Finansal Sonuç") },
                text = { Text(msg) },
                confirmButton = {
                    Button(onClick = {
                        manager.nextQuestion()
                        currentQuestion = manager.getCurrentQuestion()
                        showExplanation = null
                        if (currentQuestion == null) {
                            manager.saveAllResponsesToFile(context)
                        }
                    }) { Text("Devam Et") }
                }
            )
        }

        // Kredi/Nakit Seçim Diyaloğu
        if (showSelectionDialog) {
            AlertDialog(
                onDismissRequest = { showSelectionDialog = false },
                title = { Text("Ödeme Yöntemi") },
                confirmButton = {
                    Button(onClick = {
                        manager.recordResponse(currentQuestion!!, "CREDIT")
                        viewModel.updateUserFinance(currentQuestion!!.amount_percent, "CREDIT")
                        showExplanation = currentQuestion!!.answers.credit.explanation
                        showSelectionDialog = false
                    }) { Text("Kredi Kartı") }
                },
                dismissButton = {
                    Button(onClick = {
                        manager.recordResponse(currentQuestion!!, "CASH")
                        viewModel.updateUserFinance(currentQuestion!!.amount_percent, "CASH")
                        showExplanation = currentQuestion!!.answers.cash.explanation
                        showSelectionDialog = false
                    }) { Text("Nakit") }
                }
            )
        }
    }
}