package com.syndic.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syndic.app.ui.theme.Gold
import com.syndic.app.ui.theme.NightBlue
import com.syndic.app.ui.theme.Slate
import com.syndic.app.ui.theme.TextPrimary

@Composable
fun BottomNavBar(
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = NightBlue,
        contentColor = Gold
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { /* Navigate */ },
            icon = { Icon(Icons.Default.Home, contentDescription = "Cockpit") },
            label = { Text("Cockpit") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Gold,
                selectedTextColor = Gold,
                indicatorColor = Slate,
                unselectedIconColor = TextPrimary,
                unselectedTextColor = TextPrimary
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* Navigate */ },
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Residents") },
            label = { Text("Residents") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Gold,
                selectedTextColor = Gold,
                indicatorColor = Slate,
                unselectedIconColor = TextPrimary,
                unselectedTextColor = TextPrimary
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* Navigate */ },
            icon = { Icon(Icons.Default.List, contentDescription = "Finance") },
            label = { Text("Finance") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Gold,
                selectedTextColor = Gold,
                indicatorColor = Slate,
                unselectedIconColor = TextPrimary,
                unselectedTextColor = TextPrimary
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* Navigate */ },
            icon = { Icon(Icons.Default.Build, contentDescription = "Docs") },
            label = { Text("Docs") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Gold,
                selectedTextColor = Gold,
                indicatorColor = Slate,
                unselectedIconColor = TextPrimary,
                unselectedTextColor = TextPrimary
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* Navigate */ },
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Blog") },
            label = { Text("Blog") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Gold,
                selectedTextColor = Gold,
                indicatorColor = Slate,
                unselectedIconColor = TextPrimary,
                unselectedTextColor = TextPrimary
            )
        )
    }
}
