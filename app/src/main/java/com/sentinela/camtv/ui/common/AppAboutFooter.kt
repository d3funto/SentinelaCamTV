package com.sentinela.camtv.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppAboutFooter(
    versionName: String,
    license: String,
    siteUrl: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BodyText("Versão: $versionName")
        BodyText("Licença: $license")
        BodyText("Site: $siteUrl")
    }
}
