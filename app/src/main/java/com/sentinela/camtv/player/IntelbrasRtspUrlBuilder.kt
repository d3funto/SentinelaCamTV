package com.sentinela.camtv.player

import com.sentinela.camtv.config.DvrConnectionConfig
import com.sentinela.camtv.domain.IntelbrasDvrChannel
import com.sentinela.camtv.domain.OnvifCameraSource
import com.sentinela.camtv.domain.RtspCameraSource
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class IntelbrasRtspUrlBuilder(
    private val dvrConfig: DvrConnectionConfig,
) {
    fun build(source: IntelbrasDvrChannel): String = buildIntelbrasRtspUrl(
        dvrConfig = dvrConfig,
        source = source,
    )

    fun build(request: CameraStreamRequest): String {
        return when (val source = request.camera.source) {
            is IntelbrasDvrChannel -> buildIntelbrasRtspUrl(
                dvrConfig = dvrConfig,
                channel = source.channel,
                subtype = request.subtype,
            )

            is OnvifCameraSource -> source.urlForSubtype(request.subtype)
            is RtspCameraSource -> source.urlForSubtype(request.subtype)
        }
    }
}

private fun OnvifCameraSource.urlForSubtype(subtype: Int): String =
    if (subtype == 0 || subRtspUrl.isNullOrBlank()) mainRtspUrl else subRtspUrl

private fun RtspCameraSource.urlForSubtype(subtype: Int): String =
    if (subtype == 0 || subRtspUrl.isNullOrBlank()) mainRtspUrl else subRtspUrl

fun buildIntelbrasRtspUrl(
    dvrConfig: DvrConnectionConfig,
    source: IntelbrasDvrChannel,
): String = buildIntelbrasRtspUrl(
    dvrConfig = dvrConfig,
    channel = source.channel,
    subtype = source.subtype,
)

fun buildIntelbrasRtspUrl(
    dvrConfig: DvrConnectionConfig,
    channel: Int,
    subtype: Int,
): String {
    val userInfo = buildUserInfo(dvrConfig)

    return "rtsp://$userInfo${dvrConfig.host}:${dvrConfig.rtspPort}/cam/realmonitor" +
        "?channel=$channel&subtype=$subtype&unicast=true&proto=Onvif"
}

private fun buildUserInfo(dvrConfig: DvrConnectionConfig): String {
    if (dvrConfig.username.isBlank()) {
        return ""
    }

    val encodedUsername = encodeUserInfo(dvrConfig.username)
    if (dvrConfig.password.isBlank()) {
        return "$encodedUsername@"
    }

    return "$encodedUsername:${encodeUserInfo(dvrConfig.password)}@"
}

private fun encodeUserInfo(value: String): String =
    URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20")
