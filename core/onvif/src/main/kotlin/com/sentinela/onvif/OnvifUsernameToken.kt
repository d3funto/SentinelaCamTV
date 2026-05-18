package com.sentinela.onvif

import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class OnvifCredentials(
    val username: String,
    val password: String,
) {
    val isConfigured: Boolean
        get() = username.isNotBlank()
}

data class OnvifUsernameToken(
    val username: String,
    val passwordDigest: String,
    val nonceBase64: String,
    val created: String,
)

class OnvifUsernameTokenFactory(
    private val nowMillis: () -> Long = System::currentTimeMillis,
    private val nonceBytes: () -> ByteArray = ::secureNonce,
) {
    fun create(credentials: OnvifCredentials): OnvifUsernameToken {
        val nonce = nonceBytes()
        val created = onvifUtcTimestamp(nowMillis())
        val digestInput = nonce + created.toByteArray(Charsets.UTF_8) +
            credentials.password.toByteArray(Charsets.UTF_8)
        val passwordDigest = MessageDigest.getInstance("SHA-1").digest(digestInput).toBase64()

        return OnvifUsernameToken(
            username = credentials.username,
            passwordDigest = passwordDigest,
            nonceBase64 = nonce.toBase64(),
            created = created,
        )
    }
}

private fun secureNonce(): ByteArray =
    ByteArray(16).also { bytes -> SecureRandom().nextBytes(bytes) }

private fun onvifUtcTimestamp(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(millis))

internal fun ByteArray.toBase64(): String {
    if (isEmpty()) return ""
    val output = StringBuilder(((size + 2) / 3) * 4)
    var index = 0
    while (index < size) {
        val first = this[index++].toInt() and 0xFF
        val hasSecond = index < size
        val second = if (hasSecond) this[index++].toInt() and 0xFF else 0
        val hasThird = index < size
        val third = if (hasThird) this[index++].toInt() and 0xFF else 0
        val triple = (first shl 16) or (second shl 8) or third

        output.append(BASE64_ALPHABET[(triple ushr 18) and 0x3F])
        output.append(BASE64_ALPHABET[(triple ushr 12) and 0x3F])
        output.append(if (hasSecond) BASE64_ALPHABET[(triple ushr 6) and 0x3F] else '=')
        output.append(if (hasThird) BASE64_ALPHABET[triple and 0x3F] else '=')
    }
    return output.toString()
}

private const val BASE64_ALPHABET =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
