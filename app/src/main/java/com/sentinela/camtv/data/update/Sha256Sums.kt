package com.sentinela.camtv.data.update

import java.io.File
import java.security.MessageDigest

object Sha256Sums {
    private val hashPattern = Regex("^[A-Fa-f0-9]{64}$")

    fun expectedHashFor(
        sumsText: String,
        fileName: String,
    ): String? =
        sumsText.lineSequence()
            .map { line -> line.trim() }
            .filter { line -> line.isNotBlank() && !line.startsWith("#") }
            .firstNotNullOfOrNull { line ->
                val parts = line.split(Regex("\\s+"), limit = 2)
                val hash = parts.getOrNull(0)?.takeIf { value -> hashPattern.matches(value) }
                val listedFileName = parts.getOrNull(1)
                    ?.trim()
                    ?.removePrefix("*")
                    ?.takeIf { value -> value.isNotBlank() }
                if (hash != null && listedFileName == fileName) {
                    hash.lowercase()
                } else {
                    null
                }
            }

    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString(separator = "") { byte ->
            "%02x".format(byte)
        }
    }
}
