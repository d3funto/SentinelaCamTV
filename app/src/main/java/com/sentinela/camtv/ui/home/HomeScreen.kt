package com.sentinela.camtv.ui.home

import androidx.activity.compose.BackHandler
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
import com.sentinela.camtv.ui.common.BodyText
import com.sentinela.camtv.ui.common.ScreenTitle
import com.sentinela.camtv.ui.common.SentinelaScreen

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

    SentinelaScreen(
        contentAlignment = Alignment.CenterStart,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ScreenTitle("Sentinela Cam TV")
            BodyText("Monitoramento local para Android TV e Google TV")

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

    SentinelaScreen(
        contentAlignment = Alignment.Center,
        horizontalPadding = 48.dp,
        verticalPadding = 48.dp,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ScreenTitle("Câmeras")
            BodyText("Cadastro de câmeras será implementado em uma etapa futura.")
            Button(onClick = onBack) {
                Text("Voltar")
            }
        }
    }
}
