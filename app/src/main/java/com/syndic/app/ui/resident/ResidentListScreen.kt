package com.syndic.app.ui.resident

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.syndic.app.ui.matrix.MatrixColor
import com.syndic.app.ui.matrix.MatrixViewModel
import com.syndic.app.ui.matrix.ResidentStatus
import com.syndic.app.ui.theme.CockpitGold
import com.syndic.app.ui.theme.CyanNeon
import com.syndic.app.ui.theme.NightBlue
import com.syndic.app.ui.theme.RoseNeon
import com.syndic.app.ui.theme.Slate

@Composable
fun ResidentListScreen(
    viewModel: MatrixViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = NightBlue
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Header
            Text(
                text = "LISTE RÉSIDENTS",
                color = CockpitGold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.residents) { resident ->
                    ResidentCard(
                        resident = resident,
                        onWhatsAppClick = {
                            val message = when {
                                resident.balance < 0 -> "Bonjour ${resident.name}, sauf erreur de notre part, vous avez un solde débiteur de ${kotlin.math.abs(resident.balance)} DH. Merci de régulariser."
                                resident.balance > 0 -> "Bonjour ${resident.name}, vous avez une avance de ${resident.balance} DH. Merci pour votre confiance !"
                                else -> "Bonjour ${resident.name}, vous êtes à jour dans vos cotisations. Merci."
                            }

                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://wa.me/?text=${Uri.encode(message)}")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ResidentCard(
    resident: ResidentStatus,
    onWhatsAppClick: () -> Unit
) {
    val statusColor = when (resident.statusColor) {
        MatrixColor.GOLD -> CockpitGold
        MatrixColor.GREEN -> CyanNeon
        MatrixColor.RED -> RoseNeon
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate),
        border = BorderStroke(1.dp, statusColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar / Apartment Circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(statusColor.copy(alpha = 0.2f), CircleShape)
                    .border(2.dp, statusColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = resident.apartment.replace("AP", ""),
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resident.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${resident.balance} DH",
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusColor
                )
            }

            IconButton(onClick = onWhatsAppClick) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "WhatsApp",
                    tint = CyanNeon
                )
            }
        }
    }
}
