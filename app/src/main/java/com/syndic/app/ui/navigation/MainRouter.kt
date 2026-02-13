package com.syndic.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
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
import com.syndic.app.ui.community.blog.BlogScreen
import com.syndic.app.ui.community.incident.IncidentsScreen
import com.syndic.app.ui.components.BottomNavBar
import com.syndic.app.ui.dashboard.CockpitScreen
import com.syndic.app.ui.finance.FinanceScreen
import com.syndic.app.ui.login.LoginScreen
import com.syndic.app.ui.resident.ChangePinScreen
import com.syndic.app.ui.resident.ResidentHomeScreen
import com.syndic.app.ui.setup.SetupScreen
import com.syndic.app.ui.theme.Gold

enum class RouterDest {
    DASHBOARD,
    CHANGE_PIN,
    FINANCE,
    INCIDENTS,
    BLOG,
    RESIDENTS,
    DOCS
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

                // Helper for Navigation Bar consistency
                val navBar = @Composable { route: String ->
                    BottomNavBar(
                        currentRoute = route,
                        onHomeClick = { currentDest = RouterDest.DASHBOARD },
                        onResidentsClick = { currentDest = RouterDest.RESIDENTS },
                        onFinanceClick = { currentDest = RouterDest.FINANCE },
                        onDocsClick = { currentDest = RouterDest.DOCS },
                        onBlogClick = { currentDest = RouterDest.BLOG }
                    )
                }

                when (currentDest) {
                    RouterDest.FINANCE -> {
                        BackHandler { currentDest = RouterDest.DASHBOARD }
                        Scaffold(bottomBar = { navBar("finance") }) { p ->
                            Box(modifier = Modifier.padding(p)) { FinanceScreen() }
                        }
                    }
                    RouterDest.INCIDENTS -> {
                        BackHandler { currentDest = RouterDest.DASHBOARD }
                        Scaffold(bottomBar = { navBar("cockpit") }) { p ->
                             Box(modifier = Modifier.padding(p)) { IncidentsScreen() }
                        }
                    }
                    RouterDest.BLOG -> {
                        BackHandler { currentDest = RouterDest.DASHBOARD }
                        Scaffold(bottomBar = { navBar("blog") }) { p ->
                            Box(modifier = Modifier.padding(p)) { BlogScreen() }
                        }
                    }
                    RouterDest.RESIDENTS -> {
                        BackHandler { currentDest = RouterDest.DASHBOARD }
                        Scaffold(bottomBar = { navBar("residents") }) { p ->
                             Box(modifier = Modifier.padding(p).fillMaxSize(), contentAlignment = Alignment.Center) {
                                 Text("Module Résidents (Bientôt)", color = Gold)
                             }
                        }
                    }
                    RouterDest.DOCS -> {
                        BackHandler { currentDest = RouterDest.DASHBOARD }
                        Scaffold(bottomBar = { navBar("docs") }) { p ->
                             Box(modifier = Modifier.padding(p).fillMaxSize(), contentAlignment = Alignment.Center) {
                                 Text("Documents & Prestataires (Bientôt)", color = Gold)
                             }
                        }
                    }
                    else -> {
                        CockpitScreen(
                            onFinanceClick = { currentDest = RouterDest.FINANCE },
                            onIncidentsClick = { currentDest = RouterDest.INCIDENTS },
                            onBlogClick = { currentDest = RouterDest.BLOG },
                            onResidentsClick = { currentDest = RouterDest.RESIDENTS },
                            onDocsClick = { currentDest = RouterDest.DOCS }
                        )
                    }
                }
            }
            UserRole.RESIDENT -> {
                when (currentDest) {
                    RouterDest.CHANGE_PIN -> {
                        BackHandler { currentDest = RouterDest.DASHBOARD }
                        ChangePinScreen(onBack = { currentDest = RouterDest.DASHBOARD })
                    }
                    RouterDest.BLOG -> {
                        BackHandler { currentDest = RouterDest.DASHBOARD }
                        BlogScreen()
                    }
                    RouterDest.INCIDENTS -> {
                        BackHandler { currentDest = RouterDest.DASHBOARD }
                        IncidentsScreen()
                    }
                    else -> {
                        ResidentHomeScreen(
                            onChangePinClick = { currentDest = RouterDest.CHANGE_PIN },
                            onBlogClick = { currentDest = RouterDest.BLOG },
                            onIncidentsClick = { currentDest = RouterDest.INCIDENTS }
                        )
                    }
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
