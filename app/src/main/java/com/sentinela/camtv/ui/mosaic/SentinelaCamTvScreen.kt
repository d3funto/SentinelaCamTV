package com.sentinela.camtv.ui.mosaic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sentinela.camtv.config.APP_PADDING_DP
import com.sentinela.camtv.config.AppDvrConfig
import com.sentinela.camtv.config.DvrConnectionConfig
import com.sentinela.camtv.config.SHOW_APP_HEADER
import com.sentinela.camtv.config.TILE_GAP_DP
import com.sentinela.camtv.config.defaultMosaicCameras
import com.sentinela.camtv.config.isConfigured
import com.sentinela.camtv.domain.Camera
import com.sentinela.camtv.domain.IntelbrasDvrChannel
import com.sentinela.camtv.player.IntelbrasRtspUrlBuilder

@Composable
fun SentinelaCamTvScreen(
    cameras: List<Camera> = defaultMosaicCameras(),
    dvrConfig: DvrConnectionConfig = AppDvrConfig.intelbrasMhdx1004,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF071821))
            .padding(APP_PADDING_DP.dp),
    ) {
        if (!dvrConfig.isConfigured()) {
            MissingDvrConfigMessage()
            return@Box
        }

        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            if (SHOW_APP_HEADER) {
                BasicText(
                    text = "Sentinela Cam TV",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(TILE_GAP_DP.dp),
            ) {
                MosaicCameraRow(
                    cameras = cameras.take(3),
                    dvrConfig = dvrConfig,
                    rowWeight = 1f,
                )

                MosaicCameraRow(
                    cameras = cameras.drop(3).take(2),
                    dvrConfig = dvrConfig,
                    rowWeight = 1.15f,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.MosaicCameraRow(
    cameras: List<Camera>,
    dvrConfig: DvrConnectionConfig,
    rowWeight: Float,
) {
    val rtspUrlBuilder = remember(dvrConfig) {
        IntelbrasRtspUrlBuilder(dvrConfig)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .weight(rowWeight),
        horizontalArrangement = Arrangement.spacedBy(TILE_GAP_DP.dp),
    ) {
        cameras.forEach { camera ->
            val rtspUrl = remember(camera.source, rtspUrlBuilder) {
                when (val source = camera.source) {
                    is IntelbrasDvrChannel -> rtspUrlBuilder.build(source)
                }
            }

            RtspCameraTile(
                name = camera.name,
                rtspUrl = rtspUrl,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
            )
        }
    }
}

@Composable
private fun MissingDvrConfigMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = "Configure sentinela.dvr.host no local.properties para testar o mosaico.",
            style = TextStyle(
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}
