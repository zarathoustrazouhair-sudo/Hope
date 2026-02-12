package com.syndic.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.syndic.app.data.local.entity.UserRole
import com.syndic.app.ui.auth.AuthViewModel
import com.syndic.app.ui.dashboard.CockpitScreen
import com.syndic.app.ui.setup.SetupScreen

@Composable
fun MainRouter(
    authViewModel: AuthViewModel = hiltViewModel(),
    routerViewModel: RouterViewModel = hiltViewModel() // New ViewModel for global routing state
) {
    val authState by authViewModel.uiState.collectAsState()
    val routerState by routerViewModel.state.collectAsState()

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
        // Show Login Screen (Placeholder for now)
        // In real app: LoginScreen(onLoginSuccess = { ... })
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
             // TODO: Integrate Login Screen in next Phase
            Text("Login Screen Placeholder - System Configured")
        }
    } else {
        // Role Dispatcher (Authenticated and Configured)
        when (authState.userRole) {
            UserRole.SYNDIC, UserRole.ADJOINT -> {
                CockpitScreen()
            }
            UserRole.RESIDENT -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Resident Home Screen Placeholder")
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
