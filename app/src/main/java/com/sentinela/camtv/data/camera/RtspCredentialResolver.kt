package com.sentinela.camtv.data.camera

data class RtspCredentials(
    val username: String?,
    val password: String?,
)

object RtspCredentialResolver {
    fun resolve(
        main: SanitizedRtspUrl,
        sub: SanitizedRtspUrl?,
        username: String?,
        password: String?,
    ): RtspCredentials =
        RtspCredentials(
            username = username?.takeIf { it.isNotBlank() } ?: main.username ?: sub?.username,
            password = password?.takeIf { it.isNotBlank() } ?: main.password ?: sub?.password,
        )
}
