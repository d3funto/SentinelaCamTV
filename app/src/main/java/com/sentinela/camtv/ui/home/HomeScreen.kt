package com.sentinela.camtv.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import com.sentinela.camtv.BuildConfig
import com.sentinela.camtv.ui.common.AppAboutFooter
import com.sentinela.camtv.ui.common.ScreenTitle
import com.sentinela.camtv.ui.common.SentinelaScreen

@Composable
fun HomeScreen(
    onOpenMosaic: () -> Unit,
    onOpenCameras: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    SentinelaScreen(
        contentAlignment = Alignment.CenterStart,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ScreenTitle("Sentinela Cam TV")

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onOpenMosaic,
                modifier = Modifier.focusRequester(focusRequester),
            ) {
                Text("Ver câmeras")
            }

            Button(onClick = onOpenCameras) {
                Text("Cadastrar câmeras")
            }

            Button(onClick = onOpenSettings) {
                Text("Suporte")
            }

            Spacer(modifier = Modifier.height(26.dp))

            AppAboutFooter(
                versionName = BuildConfig.VERSION_NAME,
                license = "GPL-3.0-or-later",
                githubUrl = "https://github.com/d3funto/SentinelaCamTV",
            )
        }
    }
}
