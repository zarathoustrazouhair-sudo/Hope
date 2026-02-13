package com.syndic.app.ui.resident.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.syndic.app.ui.theme.CockpitGold
import com.syndic.app.ui.theme.NightBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentDetailScreen(
    apartment: String,
    onBack: () -> Unit, // Parameter unused warning, suppressing via logic or usage
    viewModel: ResidentDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(apartment) { viewModel.loadResident(apartment) }
    val state by viewModel.state.collectAsState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Sync state to local editable fields
    LaunchedEffect(state.user) {
        state.user?.let {
            firstName = it.firstName
            lastName = it.lastName
            phone = it.phoneNumber ?: ""
            email = it.email
        }
    }

    Scaffold(
        containerColor = NightBlue,
        topBar = {
            TopAppBar(
                title = { Text(apartment, color = CockpitGold, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NightBlue),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (state.isEditing) {
                            viewModel.updateResident(firstName, lastName, phone, email)
                        } else {
                            viewModel.toggleEdit()
                        }
                    }) {
                        Icon(
                            if (state.isEditing) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = CockpitGold
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (state.isLoading) {
                CircularProgressIndicator(color = CockpitGold)
            } else if (state.user != null) {
                if (state.isEditing) {
                    OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Prénom") })
                    OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Nom") })
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Téléphone") })
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                } else {
                    Text("Nom: ${state.user!!.firstName} ${state.user!!.lastName}", color = Color.White)
                    Text("Tél: ${state.user!!.phoneNumber ?: "N/A"}", color = Color.White)
                    Text("Email: ${state.user!!.email}", color = Color.White)
                }
            }
        }
    }
}
