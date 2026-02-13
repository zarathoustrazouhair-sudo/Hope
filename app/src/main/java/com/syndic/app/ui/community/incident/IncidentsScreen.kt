package com.syndic.app.ui.community.incident

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.syndic.app.data.local.entity.IncidentEntity
import com.syndic.app.data.local.entity.IncidentStatus
import com.syndic.app.ui.theme.CockpitGold
import com.syndic.app.ui.theme.CockpitGreen
import com.syndic.app.ui.theme.CockpitRed
import com.syndic.app.ui.theme.NightBlue
import com.syndic.app.ui.theme.Slate

// Temporary color for In Progress
val CockpitOrange = Color(0xFFFB923C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentsScreen(
    viewModel: IncidentViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedIncident by remember { mutableStateOf<IncidentEntity?>(null) } // For Status Dialog

    Scaffold(
        containerColor = NightBlue,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = CockpitGold
            ) {
                Icon(Icons.Default.Add, contentDescription = "Signaler", tint = NightBlue)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                text = if (state.isSyndic) "TOUS LES INCIDENTS" else "MES INCIDENTS",
                color = CockpitGold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(24.dp).align(Alignment.CenterHorizontally)
            )

            if (state.isLoading && state.incidents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CockpitGold)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.incidents) { incident ->
                        IncidentCard(
                            incident = incident,
                            canEditStatus = state.isSyndic,
                            onClick = {
                                if (state.isSyndic) selectedIncident = incident
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateIncidentDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, desc ->
                viewModel.createIncident(title, desc)
                showCreateDialog = false
            }
        )
    }

    if (selectedIncident != null) {
        UpdateStatusDialog(
            incident = selectedIncident!!,
            onDismiss = { selectedIncident = null },
            onStatusSelected = { status ->
                viewModel.updateStatus(selectedIncident!!.id, status)
                selectedIncident = null
            }
        )
    }
}

@Composable
fun IncidentCard(
    incident: IncidentEntity,
    canEditStatus: Boolean,
    onClick: () -> Unit
) {
    val statusColor = when (incident.status) {
        IncidentStatus.OPEN -> CockpitRed
        IncidentStatus.IN_PROGRESS -> CockpitOrange
        IncidentStatus.RESOLVED -> CockpitGreen
    }

    val statusText = when (incident.status) {
        IncidentStatus.OPEN -> "OUVERT"
        IncidentStatus.IN_PROGRESS -> "EN COURS"
        IncidentStatus.RESOLVED -> "CLÔTURÉ"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate),
        modifier = Modifier.fillMaxWidth().clickable(enabled = canEditStatus) { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = incident.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = incident.description,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = statusColor.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.small,
                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
            ) {
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CreateIncidentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Signaler un Incident") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre (Ex: Fuite d'eau)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, description) },
                enabled = title.isNotBlank() && description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CockpitGold)
            ) {
                Text("Signaler", color = NightBlue)
            }
        },
        dismissButton = {
             TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
fun UpdateStatusDialog(
    incident: IncidentEntity,
    onDismiss: () -> Unit,
    onStatusSelected: (IncidentStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mettre à jour le statut") },
        text = {
            Column {
                Button(
                    onClick = { onStatusSelected(IncidentStatus.OPEN) },
                    colors = ButtonDefaults.buttonColors(containerColor = CockpitRed.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OUVERT", color = CockpitRed)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onStatusSelected(IncidentStatus.IN_PROGRESS) },
                    colors = ButtonDefaults.buttonColors(containerColor = CockpitOrange.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("EN COURS", color = CockpitOrange)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onStatusSelected(IncidentStatus.RESOLVED) },
                    colors = ButtonDefaults.buttonColors(containerColor = CockpitGreen.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CLÔTURÉ", color = CockpitGreen)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
