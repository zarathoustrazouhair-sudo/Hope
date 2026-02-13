package com.syndic.app.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.syndic.app.ui.theme.SyndicAppTheme

// Night Cockpit Colors (Hardcoded for this screen to ensure consistency)
private val CockpitBackground = Color(0xFF0F172A)
private val CockpitGold = Color(0xFFFFD700)
private val CockpitCyan = Color(0xFF00E5FF)
private val CockpitSurface = Color(0xFF1E293B)
private val CockpitError = Color(0xFFEF4444)

@Composable
fun SetupScreen(
    onSetupComplete: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.currentStep == SetupStep.COMPLETE) {
        onSetupComplete()
        return
    }

    Scaffold(
        containerColor = CockpitBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = "LA MONDIALE",
                color = CockpitGold,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "CONFIGURATION MAÎTRE",
                color = CockpitCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Content based on step
            when (state.currentStep) {
                SetupStep.WELCOME -> WelcomeStep(state, viewModel)
                SetupStep.MASTER_PIN -> MasterPinStep(state, viewModel)
                SetupStep.FINANCIAL_CONFIG -> FinancialConfigStep(state, viewModel)
                SetupStep.SECURITY_CHECK -> SecurityCheckStep(state, viewModel)
                else -> {}
            }

            // Error Message
            if (state.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.error!!,
                    color = CockpitError,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeStep(state: SetupState, viewModel: SetupViewModel) {
    Text(
        text = "Bienvenue, Architecte.",
        color = Color.White,
        fontSize = 20.sp,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Initialisons votre résidence.",
        color = Color.Gray,
        fontSize = 16.sp,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(32.dp))

    OutlinedTextField(
        value = state.residenceName,
        onValueChange = viewModel::onResidenceNameChange,
        label = { Text("Nom de la Résidence") },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CockpitCyan,
            unfocusedBorderColor = Color.Gray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = CockpitCyan,
            unfocusedLabelColor = Color.Gray
        ),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = viewModel::onNextStep,
        colors = ButtonDefaults.buttonColors(containerColor = CockpitCyan),
        modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
        Text("COMMENCER", color = CockpitBackground, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterPinStep(state: SetupState, viewModel: SetupViewModel) {
    Text(
        text = "Sécurité Absolue",
        color = Color.White,
        fontSize = 20.sp
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Définissez le Master PIN (4-6 chiffres).",
        color = Color.Gray,
        fontSize = 14.sp
    )

    Spacer(modifier = Modifier.height(32.dp))

    OutlinedTextField(
        value = state.masterPin,
        onValueChange = viewModel::onMasterPinChange,
        label = { Text("Master PIN") },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CockpitGold,
            unfocusedBorderColor = Color.Gray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = CockpitGold
        ),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = state.masterPinConfirm,
        onValueChange = viewModel::onMasterPinConfirmChange,
        label = { Text("Confirmer le PIN") },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
         colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CockpitGold,
            unfocusedBorderColor = Color.Gray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = CockpitGold
        ),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(32.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = viewModel::onBackStep) {
            Text("RETOUR", color = Color.Gray)
        }
        Button(
            onClick = viewModel::onNextStep,
            colors = ButtonDefaults.buttonColors(containerColor = CockpitGold),
            modifier = Modifier.height(50.dp)
        ) {
            Text("SUIVANT", color = CockpitBackground, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialConfigStep(state: SetupState, viewModel: SetupViewModel) {
    Text(
        text = "Moteur Financier",
        color = Color.White,
        fontSize = 20.sp
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Paramètres mensuels fixes (DH).",
        color = Color.Gray,
        fontSize = 14.sp
    )

    Spacer(modifier = Modifier.height(24.dp))

    FinancialInput(
        label = "Cotisation Mensuelle / Appt",
        value = state.monthlyFee,
        onValueChange = viewModel::onMonthlyFeeChange
    )

    FinancialInput(
        label = "Salaire Concierge",
        value = state.conciergeSalary,
        onValueChange = viewModel::onConciergeSalaryChange
    )

    FinancialInput(
        label = "Frais Ménage/Entretien",
        value = state.cleaningCost,
        onValueChange = viewModel::onCleaningCostChange
    )

    FinancialInput(
        label = "Électricité",
        value = state.electricityCost,
        onValueChange = viewModel::onElectricityCostChange
    )

    FinancialInput(
        label = "Eau",
        value = state.waterCost,
        onValueChange = viewModel::onWaterCostChange
    )

    FinancialInput(
        label = "Maintenance Ascenseur",
        value = state.elevatorCost,
        onValueChange = viewModel::onElevatorCostChange
    )

    FinancialInput(
        label = "Assurance",
        value = state.insuranceCost,
        onValueChange = viewModel::onInsuranceCostChange
    )

    FinancialInput(
        label = "Frais Divers",
        value = state.diversCost,
        onValueChange = viewModel::onDiversCostChange
    )

    Spacer(modifier = Modifier.height(32.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = viewModel::onBackStep) {
            Text("RETOUR", color = Color.Gray)
        }
        Button(
            onClick = viewModel::onNextStep,
            colors = ButtonDefaults.buttonColors(containerColor = CockpitCyan),
            modifier = Modifier.height(50.dp)
        ) {
            Text("SUIVANT", color = CockpitBackground, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FinancialInput(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CockpitCyan,
            unfocusedBorderColor = Color.Gray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = CockpitCyan,
            unfocusedLabelColor = Color.Gray
        ),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        singleLine = true
    )
}

@Composable
fun SecurityCheckStep(state: SetupState, viewModel: SetupViewModel) {
     Text(
        text = "Vérification Finale",
        color = Color.White,
        fontSize = 20.sp
    )
    Spacer(modifier = Modifier.height(16.dp))

    Card(
        colors = CardDefaults.cardColors(containerColor = CockpitSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            DetailRow("Résidence", state.residenceName)
            DetailRow("Cotisation", "${state.monthlyFee} DH")
            DetailRow("Total Charges", "${calculateTotalCosts(state)} DH")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Master PIN: Configuré (SHA-256)", color = CockpitGold, fontSize = 12.sp)
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

     Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = viewModel::onBackStep) {
            Text("MODIFIER", color = Color.Gray)
        }
        Button(
            onClick = viewModel::onNextStep,
            colors = ButtonDefaults.buttonColors(containerColor = CockpitGold),
            modifier = Modifier.height(50.dp),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(color = CockpitBackground, modifier = Modifier.size(24.dp))
            } else {
                Text("VERROUILLER LA CONFIGURATION", color = CockpitBackground, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray)
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

private fun calculateTotalCosts(state: SetupState): Double {
    return (state.conciergeSalary.toDoubleOrNull() ?: 0.0) +
           (state.cleaningCost.toDoubleOrNull() ?: 0.0) +
           (state.electricityCost.toDoubleOrNull() ?: 0.0) +
           (state.waterCost.toDoubleOrNull() ?: 0.0) +
           (state.elevatorCost.toDoubleOrNull() ?: 0.0) +
           (state.insuranceCost.toDoubleOrNull() ?: 0.0) +
           (state.diversCost.toDoubleOrNull() ?: 0.0)
}
