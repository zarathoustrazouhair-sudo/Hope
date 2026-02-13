package com.syndic.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.syndic.app.ui.components.BottomNavBar
import com.syndic.app.ui.components.CashFlowChart
import com.syndic.app.ui.components.CockpitHeader
import com.syndic.app.ui.components.KpiCard
import com.syndic.app.ui.components.RecoveryGauge
import com.syndic.app.ui.components.ResidentMatrixGrid
import com.syndic.app.ui.matrix.MatrixViewModel
import com.syndic.app.ui.theme.CyanNeon
import com.syndic.app.ui.theme.Gold
import com.syndic.app.ui.theme.RoseNeon
import com.syndic.app.ui.theme.Slate

@Composable
fun CockpitScreen(
    onFinanceClick: () -> Unit = {},
    onIncidentsClick: () -> Unit = {},
    onBlogClick: () -> Unit = {},
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    matrixViewModel: MatrixViewModel = hiltViewModel()
) {
    val dashboardState by dashboardViewModel.uiState.collectAsState()
    val matrixState by matrixViewModel.state.collectAsState()

    Scaffold(
        bottomBar = { BottomNavBar() },
        containerColor = com.syndic.app.ui.theme.NightBlue
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CockpitHeader()

            // 1. KPI Cards Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                KpiCard(
                    title = "SOLDE GLOBAL",
                    value = "${dashboardState.globalBalance} DH",
                    borderColor = Gold,
                    modifier = Modifier.weight(1f),
                    onClick = onFinanceClick
                )

                val runway = dashboardState.runwayMonths
                val runwayEmoji = when {
                    runway >= 6 -> "😎"
                    runway >= 3 -> "🙂"
                    runway >= 1 -> "😐"
                    else -> "😱"
                }

                KpiCard(
                    title = "SURVIE",
                    value = String.format("%.1f MOIS", runway),
                    borderColor = CyanNeon,
                    icon = runwayEmoji,
                    modifier = Modifier.weight(1f),
                    onClick = onFinanceClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Operational Cards Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                KpiCard(
                    title = "MAGAZINE",
                    value = "BLOG", // Placeholder value, could be post count
                    borderColor = Slate, // Neutral
                    icon = "📰",
                    modifier = Modifier.weight(1f),
                    onClick = onBlogClick
                )

                val incidentCount = dashboardState.openIncidentsCount
                val incidentBorderColor = if (incidentCount > 0) RoseNeon else Slate

                KpiCard(
                    title = "INCIDENTS",
                    value = "$incidentCount",
                    borderColor = incidentBorderColor,
                    modifier = Modifier.weight(1f),
                    onClick = onIncidentsClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Matrix Grid (Center)
            Text(
                text = "MATRICE RÉSIDENTS",
                color = Gold,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            ResidentMatrixGrid(
                residents = matrixState.residents,
                onResidentClick = { /* Navigate to detail */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(300.dp) // Fixed height for 3x5 grid approx
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Charts (Bottom)
            Text(
                text = "ANALYSE FINANCIÈRE",
                color = Gold,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("RECOUVREMENT", color = CyanNeon)
                    Spacer(modifier = Modifier.height(8.dp))
                    RecoveryGauge(percentage = dashboardState.recoveryRate.toFloat())
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("FLUX TRÉSORERIE", color = CyanNeon)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.height(100.dp).fillMaxWidth()) {
                        CashFlowChart()
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
