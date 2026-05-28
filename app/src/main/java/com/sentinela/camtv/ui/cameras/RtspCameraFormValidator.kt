package com.sentinela.camtv.ui.cameras

import java.net.URI

data class ValidRtspCameraForm(
    val name: String,
    val mainRtspUrl: String,
    val subRtspUrl: String?,
    val username: String?,
    val password: String?,
)

sealed interface RtspCameraFormValidation {
    data class Valid(val form: ValidRtspCameraForm) : RtspCameraFormValidation
    data class Invalid(val message: String) : RtspCameraFormValidation
}

object RtspCameraFormValidator {
    const val CREDENTIALS_IN_URL_MESSAGE =
        "Informe usuário e senha nos campos próprios, não dentro da URL RTSP."

    fun validate(
        name: String,
        mainRtspUrl: String,
        subRtspUrl: String,
        username: String,
        password: String,
    ): RtspCameraFormValidation {
        val mainUrl = mainRtspUrl.trim()
        val subUrl = subRtspUrl.trim().takeIf { it.isNotBlank() }
        if (!mainUrl.isValidRtspUrl()) {
            return RtspCameraFormValidation.Invalid("Informe uma URL RTSP principal válida.")
        }
        if (subUrl != null && !subUrl.isValidRtspUrl()) {
            return RtspCameraFormValidation.Invalid("Informe uma URL RTSP secundária válida ou deixe o campo vazio.")
        }
        if (mainUrl.hasUserInfo() || subUrl?.hasUserInfo() == true) {
            return RtspCameraFormValidation.Invalid(CREDENTIALS_IN_URL_MESSAGE)
        }

        return RtspCameraFormValidation.Valid(
            ValidRtspCameraForm(
                name = name.trim().ifBlank { "RTSP" },
                mainRtspUrl = mainUrl,
                subRtspUrl = subUrl,
                username = username.trim().takeIf { it.isNotBlank() },
                password = password.takeIf { it.isNotBlank() },
            ),
        )
    }

    private fun String.isValidRtspUrl(): Boolean =
        runCatching {
            val uri = URI(trim())
            uri.scheme.equals("rtsp", ignoreCase = true) &&
                uri.host?.isValidRtspHost() == true
        }.getOrDefault(false)

    private fun String.isValidRtspHost(): Boolean {
        if (isBlank()) return false
        if (!all { char -> char.isDigit() || char == '.' }) return true

        val parts = split('.')
        return parts.size == 4 &&
            parts.all { part ->
                part.isNotBlank() &&
                    part.length <= 3 &&
                    part.toIntOrNull()?.let { value -> value in 0..255 } == true
            }
    }

    private fun String.hasUserInfo(): Boolean =
        runCatching { URI(trim()).userInfo != null }.getOrDefault(false)
}
