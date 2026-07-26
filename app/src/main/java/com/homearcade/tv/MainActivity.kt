package com.homearcade.tv

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.homearcade.tv.navigation.NavGraph
import com.homearcade.tv.ui.theme.HomeArcadeTVTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeArcadeTVTheme {
                NavGraph()
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // TVKeyBridge is attached to each WebView instance in HomeScreen
        // We propagate key events from the activity level if needed
        return super.dispatchKeyEvent(event)
    }
}
