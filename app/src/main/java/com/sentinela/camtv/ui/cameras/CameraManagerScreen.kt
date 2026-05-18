package com.sentinela.camtv.ui.cameras

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.sentinela.camtv.ui.theme.SentinelaBackground

@Composable
fun CameraManagerScreen(
    state: CameraManagerUiState,
    onDiscoverOnvif: () -> Unit,
    onBack: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    BackHandler(onBack = onBack)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelaBackground)
            .padding(horizontal = 56.dp, vertical = 40.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Gerenciar câmeras",
                style = MaterialTheme.typography.displaySmall,
            )
            Text(
                text = "Câmeras salvas: ${state.cameras.size}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = state.statusMessage ?: "Descoberta ONVIF e RTSP manual ficam neste fluxo.",
                style = MaterialTheme.typography.titleMedium,
            )
            Button(
                onClick = onDiscoverOnvif,
                modifier = Modifier.focusRequester(focusRequester),
                enabled = !state.scanning,
            ) {
                Text(if (state.scanning) "Procurando..." else "Buscar ONVIF na rede")
            }
            if (state.discoveredDevices.isNotEmpty()) {
                Text(
                    text = "Dispositivos encontrados: ${state.discoveredDevices.size}",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Button(onClick = onBack) {
                Text("Voltar")
            }
        }
    }
}
