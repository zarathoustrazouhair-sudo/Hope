package com.syndic.app.ui.finance

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.syndic.app.data.local.entity.TransactionEntity
import com.syndic.app.data.local.entity.TransactionType
import com.syndic.app.ui.finance.components.AddExpenseDialog
import com.syndic.app.ui.finance.components.AddIncomeDialog
import com.syndic.app.ui.theme.CockpitGold
import com.syndic.app.ui.theme.CockpitGreen
import com.syndic.app.ui.theme.CockpitRed
import com.syndic.app.ui.theme.NightBlue
import com.syndic.app.ui.theme.Slate
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    viewModel: FinanceViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showTypeSelection by remember { mutableStateOf(false) }
    var showIncomeDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }

    // Handle PDF opening
    LaunchedEffect(state.pdfFile) {
        state.pdfFile?.let { file ->
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
            viewModel.clearPdfState()
        }
    }

    Scaffold(
        containerColor = NightBlue,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showTypeSelection = true },
                containerColor = CockpitGold
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = NightBlue)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Header
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SOLDE GLOBAL", color = Color.Gray, fontSize = 12.sp)
                    Text(
                        "${String.format("%.2f", state.globalBalance)} DH",
                        color = if (state.globalBalance >= 0) CockpitGreen else CockpitRed,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Transactions List
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(state.transactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onPdfClick = { viewModel.generatePdf(transaction) }
                    )
                }
            }
        }
    }

    // Type Selection Dialog
    if (showTypeSelection) {
        AlertDialog(
            onDismissRequest = { showTypeSelection = false },
            title = { Text("Nouvelle Transaction") },
            text = {
                Column {
                    Button(
                        onClick = {
                            showTypeSelection = false
                            showIncomeDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CockpitGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ArrowUpward, null)
                        Spacer(Modifier.width(8.dp))
                        Text("ENCAISSEMENT (Recette)")
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            showTypeSelection = false
                            showExpenseDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CockpitRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ArrowDownward, null)
                        Spacer(Modifier.width(8.dp))
                        Text("DÉPENSE (Sortie)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTypeSelection = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showIncomeDialog) {
        AddIncomeDialog(
            residents = state.residents,
            onDismiss = { showIncomeDialog = false },
            onConfirm = { residentId, amount, method ->
                viewModel.createIncome(residentId, amount, method)
                showIncomeDialog = false
            }
        )
    }

    if (showExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showExpenseDialog = false },
            onConfirm = { amount, provider, category ->
                viewModel.createExpense(amount, provider, category)
                showExpenseDialog = false
            }
        )
    }
}

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    onPdfClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    val isIncome = transaction.type == TransactionType.PAIEMENT

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = if (isIncome) CockpitGreen else CockpitRed,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.label,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = dateFormat.format(transaction.date),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                if (transaction.type == TransactionType.DEPENSE) {
                    Text(
                        text = "Prest: ${transaction.provider} | Cat: ${transaction.category}",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                } else if (transaction.type == TransactionType.PAIEMENT) {
                    Text(
                        text = "Via: ${transaction.paymentMethod?.name}",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIncome) "+" else "-"}${String.format("%.0f", transaction.amount)}",
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) CockpitGreen else CockpitRed
                )
                if (transaction.type == TransactionType.PAIEMENT || transaction.type == TransactionType.DEPENSE) {
                    IconButton(onClick = onPdfClick) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "PDF",
                            tint = CockpitGold
                        )
                    }
                }
            }
        }
    }
}
