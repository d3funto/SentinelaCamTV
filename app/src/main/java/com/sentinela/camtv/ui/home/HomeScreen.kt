package com.sentinela.camtv.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
fun HomeScreen(
    canOpenMosaic: Boolean,
    onOpenMosaic: () -> Unit,
    onOpenCameras: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Sentinela Cam TV",
                style = MaterialTheme.typography.displayMedium,
            )

            Text(
                text = "Monitoramento local para Android TV e Google TV",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (canOpenMosaic) {
                Button(
                    onClick = onOpenMosaic,
                    modifier = Modifier.focusRequester(focusRequester),
                ) {
                    Text("Visualizar mosaico")
                }
            } else {
                Button(
                    onClick = onOpenCameras,
                    modifier = Modifier.focusRequester(focusRequester),
                ) {
                    Text("Gerenciar câmeras")
                }
            }

            if (canOpenMosaic) {
                Button(onClick = onOpenCameras) {
                    Text("Gerenciar câmeras")
                }
            }

            Button(onClick = onOpenSettings) {
                Text("Ajustes")
            }
        }
    }
}

@Composable
fun CamerasPlaceholderScreen(
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelaBackground)
            .padding(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Câmeras",
                style = MaterialTheme.typography.displaySmall,
            )
            Text(
                text = "Cadastro de câmeras será implementado em uma etapa futura.",
                style = MaterialTheme.typography.titleMedium,
            )
            Button(onClick = onBack) {
                Text("Voltar")
            }
        }
    }
}
