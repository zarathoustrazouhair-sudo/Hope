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

@Composable
fun MainRouter(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (!uiState.isAuthenticated) {
        // Show Login Screen (Placeholder for now, redirecting to simple text)
        // In real app: LoginScreen(onLoginSuccess = { ... })
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Login Screen Placeholder")
        }
    } else {
        // Role Dispatcher
        when (uiState.userRole) {
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
