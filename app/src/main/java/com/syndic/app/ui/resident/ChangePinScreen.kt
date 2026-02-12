package com.syndic.app.ui.resident

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

// Night Cockpit Colors
private val CockpitBackground = Color(0xFF0F172A)
private val CockpitGold = Color(0xFFFFD700)
private val CockpitCyan = Color(0xFF00E5FF)
private val CockpitError = Color(0xFFEF4444)

@Composable
fun ChangePinScreen(
    onBack: () -> Unit,
    viewModel: ChangePinViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.isSuccess) {
        onBack()
    }

    Scaffold(
        containerColor = CockpitBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SÉCURITÉ DU COMPTE",
                color = CockpitGold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = state.oldPin,
                onValueChange = viewModel::onOldPinChange,
                label = { Text("Ancien Code PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CockpitCyan,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = CockpitCyan,
                    unfocusedLabelColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.newPin,
                onValueChange = viewModel::onNewPinChange,
                label = { Text("Nouveau Code PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CockpitCyan,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = CockpitCyan,
                    unfocusedLabelColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.confirmPin,
                onValueChange = viewModel::onConfirmPinChange,
                label = { Text("Confirmer Nouveau Code") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CockpitCyan,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = CockpitCyan,
                    unfocusedLabelColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = CockpitError,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onBack) {
                    Text("ANNULER", color = Color.Gray)
                }
                Button(
                    onClick = viewModel::onChangePin,
                    colors = ButtonDefaults.buttonColors(containerColor = CockpitGold),
                    modifier = Modifier.height(50.dp),
                    enabled = !state.isLoading && state.newPin.length == 4 && state.confirmPin.length == 4 && state.oldPin.length == 4
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = CockpitBackground, modifier = Modifier.size(24.dp))
                    } else {
                        Text("VALIDER", color = CockpitBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
