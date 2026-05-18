package com.sentinela.camtv.data.camera

import java.net.URI

data class SanitizedRtspUrl(
    val urlWithoutUserInfo: String,
    val username: String?,
    val password: String?,
)

object RtspUrlSanitizer {
    fun sanitize(rawUrl: String): SanitizedRtspUrl {
        val uri = URI(rawUrl.trim())
        val userInfo = uri.userInfo
        val username = userInfo?.substringBefore(':')?.takeIf { it.isNotBlank() }
        val password = userInfo
            ?.substringAfter(':', missingDelimiterValue = "")
            ?.takeIf { it.isNotBlank() }
        val sanitized = URI(
            uri.scheme,
            null,
            uri.host,
            uri.port,
            uri.path,
            uri.query,
            uri.fragment,
        ).toString()

        return SanitizedRtspUrl(
            urlWithoutUserInfo = sanitized,
            username = username,
            password = password,
        )
    }

    fun withCredentials(
        sanitizedUrl: String,
        username: String?,
        password: String?,
    ): String {
        if (username.isNullOrBlank()) return sanitizedUrl
        val uri = URI(sanitizedUrl)
        val userInfo = if (password.isNullOrBlank()) {
            username
        } else {
            "$username:$password"
        }
        return URI(
            uri.scheme,
            userInfo,
            uri.host,
            uri.port,
            uri.path,
            uri.query,
            uri.fragment,
        ).toString()
    }
}
