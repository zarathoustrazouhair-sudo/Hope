package com.syndic.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.syndic.app.data.local.entity.UserRole
import com.syndic.app.ui.auth.AuthViewModel
import com.syndic.app.ui.dashboard.CockpitScreen
import com.syndic.app.ui.finance.FinanceScreen
import com.syndic.app.ui.login.LoginScreen
import com.syndic.app.ui.resident.ChangePinScreen
import com.syndic.app.ui.resident.ResidentHomeScreen
import com.syndic.app.ui.setup.SetupScreen

enum class RouterDest {
    DASHBOARD,
    CHANGE_PIN,
    FINANCE
}

@Composable
fun MainRouter(
    authViewModel: AuthViewModel = hiltViewModel(),
    routerViewModel: RouterViewModel = hiltViewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val routerState by routerViewModel.state.collectAsState()

    // Simple internal navigation state
    var currentDest by remember { mutableStateOf(RouterDest.DASHBOARD) }

    if (routerState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (!routerState.isSetupComplete) {
        // Show Setup Wizard if not configured
        SetupScreen(
            onSetupComplete = { routerViewModel.refreshSetupState() }
        )
    } else if (authState.isLoading) {
         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (!authState.isAuthenticated) {
        // Show Login Screen
        LoginScreen(
            onLoginSuccess = { role ->
                authViewModel.onLocalLoginSuccess(role)
                currentDest = RouterDest.DASHBOARD // Reset nav on login
            }
        )
    } else {
        // Role Dispatcher (Authenticated and Configured)
        when (authState.userRole) {
            UserRole.SYNDIC, UserRole.ADJOINT -> {
                if (currentDest == RouterDest.FINANCE) {
                    BackHandler { currentDest = RouterDest.DASHBOARD }
                    FinanceScreen()
                } else {
                    CockpitScreen(onFinanceClick = { currentDest = RouterDest.FINANCE })
                }
            }
            UserRole.RESIDENT -> {
                if (currentDest == RouterDest.CHANGE_PIN) {
                    BackHandler { currentDest = RouterDest.DASHBOARD }
                    ChangePinScreen(onBack = { currentDest = RouterDest.DASHBOARD })
                } else {
                    ResidentHomeScreen(onChangePinClick = { currentDest = RouterDest.CHANGE_PIN })
                }
            }
            UserRole.CONCIERGE -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Concierge Dashboard Placeholder")
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Unknown Role")
                }
            }
        }
    }
}
