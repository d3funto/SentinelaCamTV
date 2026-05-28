package com.sentinela.camtv.data.update

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class Sha256SumsTest {
    @Test
    fun findsExpectedHashForAssetName() {
        val hash = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
        val sumsText = """
            $hash  SentinelaCamTV-v1.1.0-armeabi-v7a.apk
            ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff  SentinelaCamTV-v1.1.0-universal.apk
        """.trimIndent()

        assertEquals(
            hash,
            Sha256Sums.expectedHashFor(
                sumsText = sumsText,
                fileName = "SentinelaCamTV-v1.1.0-armeabi-v7a.apk",
            ),
        )
    }

    @Test
    fun ignoresInvalidHashesAndUnknownFiles() {
        val sumsText = """
            invalid  SentinelaCamTV-v1.1.0-armeabi-v7a.apk
            0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef  outro.apk
        """.trimIndent()

        assertNull(
            Sha256Sums.expectedHashFor(
                sumsText = sumsText,
                fileName = "SentinelaCamTV-v1.1.0-armeabi-v7a.apk",
            ),
        )
    }

    @Test
    fun calculatesSha256ForDownloadedFile() {
        val file = File.createTempFile("sentinela-sha256", ".apk")
        try {
            file.writeText("abc")

            assertEquals(
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                Sha256Sums.sha256(file),
            )
        } finally {
            file.delete()
        }
    }
}
