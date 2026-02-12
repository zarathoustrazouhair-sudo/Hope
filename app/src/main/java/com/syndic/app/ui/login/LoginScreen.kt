package com.syndic.app.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.syndic.app.data.local.entity.UserRole

// Night Cockpit Colors
private val CockpitBackground = Color(0xFF0F172A)
private val CockpitGold = Color(0xFFFFD700)
private val CockpitCyan = Color(0xFF00E5FF)
private val CockpitError = Color(0xFFEF4444)

@Composable
fun LoginScreen(
    onLoginSuccess: (UserRole) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Trigger navigation on success
    LaunchedEffect(state.isAuthenticated, state.authenticatedRole) {
        if (state.isAuthenticated && state.authenticatedRole != null) {
            onLoginSuccess(state.authenticatedRole!!)
        }
    }

    Scaffold(
        containerColor = CockpitBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ACCÈS RÉSIDENCE",
                color = CockpitGold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Mode Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ModeButton("RÉSIDENT", state.mode == LoginMode.RESIDENT) {
                    viewModel.switchMode(LoginMode.RESIDENT)
                }
                Spacer(modifier = Modifier.width(16.dp))
                ModeButton("SYNDIC", state.mode == LoginMode.SYNDIC) {
                    viewModel.switchMode(LoginMode.SYNDIC)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (state.mode == LoginMode.SYNDIC) {
                SyndicLogin(state, viewModel)
            } else {
                ResidentLogin(state, viewModel)
            }

            if (state.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.error!!,
                    color = CockpitError,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ModeButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) CockpitCyan else Color.Gray.copy(alpha = 0.3f),
            contentColor = if (isSelected) CockpitBackground else Color.White
        ),
        modifier = Modifier.width(140.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyndicLogin(state: LoginState, viewModel: LoginViewModel) {
    OutlinedTextField(
        value = state.syndicPin,
        onValueChange = viewModel::onSyndicPinChange,
        label = { Text("Master PIN") },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CockpitGold,
            unfocusedBorderColor = Color.Gray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = CockpitGold,
            unfocusedLabelColor = Color.Gray
        ),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = viewModel::onLogin,
        colors = ButtonDefaults.buttonColors(containerColor = CockpitGold),
        modifier = Modifier.fillMaxWidth().height(50.dp),
        enabled = !state.isLoading && state.syndicPin.length >= 4
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(color = CockpitBackground, modifier = Modifier.size(24.dp))
        } else {
            Text("OUVRIR COCKPIT", color = CockpitBackground, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentLogin(state: LoginState, viewModel: LoginViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val apartments = (1..15).map { "AP$it" }

    // Apartment Selector
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = state.selectedApartment,
            onValueChange = {},
            readOnly = true,
            label = { Text("Appartement") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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
        // Invisible box to capture click for dropdown
         Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CockpitBackground)
        ) {
            apartments.forEach { apt ->
                DropdownMenuItem(
                    text = { Text(apt, color = Color.White) },
                    onClick = {
                        viewModel.onApartmentSelected(apt)
                        expanded = false
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = state.residentPin,
        onValueChange = viewModel::onResidentPinChange,
        label = { Text("Code PIN (4 chiffres)") },
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

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = viewModel::onLogin,
        colors = ButtonDefaults.buttonColors(containerColor = CockpitCyan),
        modifier = Modifier.fillMaxWidth().height(50.dp),
        enabled = !state.isLoading && state.residentPin.length == 4
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(color = CockpitBackground, modifier = Modifier.size(24.dp))
        } else {
            Text("ACCÉDER", color = CockpitBackground, fontWeight = FontWeight.Bold)
        }
    }
}
