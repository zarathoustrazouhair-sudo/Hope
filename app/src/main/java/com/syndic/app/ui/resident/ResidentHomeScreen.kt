package com.syndic.app.ui.resident

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

// Night Cockpit Colors
private val CockpitBackground = Color(0xFF0F172A)
private val CockpitGold = Color(0xFFFFD700)
private val CockpitCyan = Color(0xFF00E5FF)
private val CockpitSurface = Color(0xFF1E293B)
private val CockpitError = Color(0xFFEF4444)
private val CockpitSuccess = Color(0xFF10B981)

@Composable
fun ResidentHomeScreen(
    onChangePinClick: () -> Unit,
    onBlogClick: () -> Unit = {},
    onIncidentsClick: () -> Unit = {},
    viewModel: ResidentHomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

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
            // Header
            Text(
                text = "ESPACE RÉSIDENT",
                color = CockpitGold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = state.apartment,
                color = Color.Gray,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Security Alert
            if (state.isDefaultPin) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CockpitError.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CockpitError)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠ SÉCURITÉ",
                            color = CockpitError,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Vous utilisez le code par défaut (0000).\nVeuillez le changer immédiatement.",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onChangePinClick,
                            colors = ButtonDefaults.buttonColors(containerColor = CockpitError),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("SÉCURISER MON COMPTE", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Financial Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CockpitSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("MON SOLDE", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${state.balance} DH",
                        color = if (state.balance >= 0) CockpitSuccess else CockpitError,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Latest News (Blog)
            if (state.latestBlogTitle != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CockpitSurface),
                    modifier = Modifier.fillMaxWidth().clickable { onBlogClick() },
                    border = androidx.compose.foundation.BorderStroke(1.dp, CockpitGold.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "DERNIÈRE ANNONCE",
                            color = CockpitGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.latestBlogTitle ?: "",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Community Health (Emoji Only)
            Card(
                colors = CardDefaults.cardColors(containerColor = CockpitSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                 Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SANTÉ RÉSIDENCE", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    val emoji = when {
                        state.runwayMonths >= 6 -> "😎"
                        state.runwayMonths >= 3 -> "🙂"
                        state.runwayMonths >= 1 -> "😐"
                        else -> "😱"
                    }

                    Text(
                        text = emoji,
                        fontSize = 48.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Actions
            Button(
                onClick = onIncidentsClick,
                colors = ButtonDefaults.buttonColors(containerColor = CockpitSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("MES INCIDENTS", color = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (!state.isDefaultPin) {
                 Button(
                    onClick = onChangePinClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CockpitSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CHANGER MON PIN", color = Color.White)
                }
            }
        }
    }
}
