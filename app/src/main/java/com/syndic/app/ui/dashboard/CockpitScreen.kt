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
import androidx.compose.ui.graphics.Color
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
    onResidentsClick: () -> Unit = {},
    onDocsClick: () -> Unit = {},
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    matrixViewModel: MatrixViewModel = hiltViewModel()
) {
    val dashboardState by dashboardViewModel.uiState.collectAsState()
    val matrixState by matrixViewModel.state.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = "cockpit",
                onHomeClick = { /* Stay here */ },
                onResidentsClick = onResidentsClick,
                onFinanceClick = onFinanceClick,
                onDocsClick = onDocsClick,
                onBlogClick = onBlogClick
            )
        },
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
                    runway > 6 -> "🤩" // > 6 months
                    runway > 3 -> "🙂" // > 3 months
                    runway > 1 -> "😐" // > 1 month
                    runway >= 0 -> "😨" // < 1 month (but positive)
                    else -> "💀"       // Negative (Debt)
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
                // Task Widget Integration (Phase 10)
                // We ensure title is visible (CyanNeon matches SURVIE style for info/action)

                KpiCard(
                    title = "ACTUALITÉS", // Changed from MAGAZINE to ensure clarity
                    value = "BLOG",
                    borderColor = CyanNeon, // Use CyanNeon so text is visible (Slate was invisible)
                    icon = "📰",
                    modifier = Modifier.weight(1f),
                    onClick = onBlogClick
                )

                val incidentCount = dashboardState.openIncidentsCount
                // If 0, use LightGray so title is visible. If > 0, use RoseNeon (Red)
                val incidentBorderColor = if (incidentCount > 0) RoseNeon else Color.LightGray

                KpiCard(
                    title = "INCIDENTS",
                    value = "$incidentCount",
                    borderColor = incidentBorderColor,
                    modifier = Modifier.weight(1f),
                    onClick = onIncidentsClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tasks Widget (Phase 10)
            com.syndic.app.ui.cockpit.tasks.TaskWidget()

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
