package com.syndic.app.ui.finance.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, provider: String, category: String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle Dépense") },
        text = {
            Column {
                // Provider
                OutlinedTextField(
                    value = provider,
                    onValueChange = { provider = it },
                    label = { Text("Prestataire (Nom/Tél)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Catégorie (Ex: Plomberie)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amount = it },
                    label = { Text("Montant (DH)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull()
                    if (provider.isNotBlank() && category.isNotBlank() && amountDouble != null && amountDouble > 0) {
                        onConfirm(amountDouble, provider, category)
                    }
                },
                enabled = provider.isNotBlank() && category.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Dépenser")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
