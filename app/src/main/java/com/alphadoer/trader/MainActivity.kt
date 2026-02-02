package com.alphadoer.trader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import com.alphadoer.trader.presentation.navigation.AlphaDoerNavHost
import com.alphadoer.trader.presentation.theme.AlphaDoerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlphaDoerTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AlphaDoerNavHost()
                }
            }
        }
    }
}

