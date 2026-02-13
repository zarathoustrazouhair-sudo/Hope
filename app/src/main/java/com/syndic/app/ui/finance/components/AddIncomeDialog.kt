package com.syndic.app.ui.finance.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.syndic.app.data.local.entity.PaymentMethod
import com.syndic.app.data.local.entity.UserEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeDialog(
    residents: List<UserEntity>,
    onDismiss: () -> Unit,
    onConfirm: (residentId: String, amount: Double, method: PaymentMethod) -> Unit
) {
    var selectedResident by remember { mutableStateOf<UserEntity?>(null) }
    var amount by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var expandedResident by remember { mutableStateOf(false) }
    var expandedMethod by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvel Encaissement") },
        text = {
            Column {
                // Resident Dropdown
                Box {
                    OutlinedTextField(
                        value = selectedResident?.apartmentNumber ?: "Sélectionner un résident",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Résident") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedResident) },
                        modifier = Modifier.fillMaxWidth().clickable { expandedResident = true }
                    )
                    // Invisible box to capture click if TextField is readOnly but not clickable properly
                    Box(modifier = Modifier.matchParentSize().clickable { expandedResident = true })

                    DropdownMenu(
                        expanded = expandedResident,
                        onDismissRequest = { expandedResident = false }
                    ) {
                        residents.forEach { resident ->
                            DropdownMenuItem(
                                text = { Text("${resident.apartmentNumber} - ${resident.firstName} ${resident.lastName}") },
                                onClick = {
                                    selectedResident = resident
                                    expandedResident = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amount = it },
                    label = { Text("Montant (DH)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Method Dropdown
                Box {
                    OutlinedTextField(
                        value = selectedMethod.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Méthode") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMethod) },
                        modifier = Modifier.fillMaxWidth().clickable { expandedMethod = true }
                    )
                     Box(modifier = Modifier.matchParentSize().clickable { expandedMethod = true })

                    DropdownMenu(
                        expanded = expandedMethod,
                        onDismissRequest = { expandedMethod = false }
                    ) {
                        PaymentMethod.values().forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method.name) },
                                onClick = {
                                    selectedMethod = method
                                    expandedMethod = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull()
                    if (selectedResident != null && amountDouble != null && amountDouble > 0) {
                        onConfirm(selectedResident!!.id, amountDouble, selectedMethod)
                    }
                },
                enabled = selectedResident != null && (amount.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Encaisser")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
