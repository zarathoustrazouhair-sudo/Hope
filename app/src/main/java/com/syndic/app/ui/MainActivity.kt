package com.syndic.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.syndic.app.ui.navigation.MainRouter
import com.syndic.app.ui.theme.SyndicAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SyndicAppTheme {
                MainRouter()
            }
        }
    }
}
