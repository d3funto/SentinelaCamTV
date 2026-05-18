package com.sentinela.camtv

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sentinela.camtv.ui.app.SentinelaAppScreen
import com.sentinela.camtv.ui.theme.SentinelaCamTVTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            SentinelaCamTVTheme {
                SentinelaAppScreen()
            }
        }
    }
}
