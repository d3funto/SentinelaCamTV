package com.sentinela.camtv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sentinela.camtv.ui.mosaic.SentinelaCamTvScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SentinelaCamTvScreen()
        }
    }
}
