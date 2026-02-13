package com.syndic.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.syndic.app.ui.theme.CockpitCyan
import com.syndic.app.ui.theme.CockpitGold
import com.syndic.app.ui.theme.NightBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = NightBlue,
        topBar = {
            TopAppBar(
                title = { Text("PARAMÈTRES", color = CockpitGold, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NightBlue),
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("RETOUR", color = Color.Gray)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Configuration Résidence", color = CockpitCyan, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.residenceName,
                onValueChange = viewModel::onResidenceNameChange,
                label = { Text("Nom Résidence") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Identité Syndic", color = CockpitCyan, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.syndicPhone,
                onValueChange = viewModel::onPhoneChange,
                label = { Text("Téléphone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.syndicEmail,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Charges Mensuelles (DH)", color = CockpitCyan, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))

            SettingsInput("Concierge", state.conciergeSalary, viewModel::onConciergeChange)
            SettingsInput("Ménage", state.cleaningCost, viewModel::onCleaningChange)
            SettingsInput("Électricité", state.electricityCost, viewModel::onElectricityChange)
            SettingsInput("Eau", state.waterCost, viewModel::onWaterChange)
            SettingsInput("Ascenseur", state.elevatorCost, viewModel::onElevatorChange)
            SettingsInput("Assurance", state.insuranceCost, viewModel::onInsuranceChange)
            SettingsInput("Divers", state.diversCost, viewModel::onDiversChange)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = viewModel::saveSettings,
                colors = ButtonDefaults.buttonColors(containerColor = CockpitGold),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = NightBlue, modifier = Modifier.size(24.dp))
                } else {
                    Text("ENREGISTRER", color = NightBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SettingsInput(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CockpitGold,
            unfocusedBorderColor = Color.Gray
        )
    )
}
