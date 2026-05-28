package com.sentinela.camtv.data.update

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DownloadedUpdateCacheTest {
    @Test
    fun cacheWithValidSha256ReturnsDownloadedUpdate() {
        val apkFile = File.createTempFile("sentinela-update", ".apk")
        try {
            apkFile.writeText("abc")
            val update = availableUpdate(assetName = apkFile.name)
            val sumsText = """
                ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad  ${apkFile.name}
            """.trimIndent()

            val downloaded = DownloadedUpdateCache.validDownloadedUpdateOrNull(
                update = update,
                downloadedFile = apkFile,
                sumsText = sumsText,
            )

            assertNotNull(downloaded)
            assertEquals(apkFile.absolutePath, downloaded?.filePath)
            assertEquals(update, downloaded?.update)
        } finally {
            apkFile.delete()
        }
    }

    @Test
    fun cacheWithInvalidSha256IsIgnored() {
        val apkFile = File.createTempFile("sentinela-update", ".apk")
        try {
            apkFile.writeText("abc")
            val update = availableUpdate(assetName = apkFile.name)
            val sumsText = """
                0000000000000000000000000000000000000000000000000000000000000000  ${apkFile.name}
            """.trimIndent()

            assertNull(
                DownloadedUpdateCache.validDownloadedUpdateOrNull(
                    update = update,
                    downloadedFile = apkFile,
                    sumsText = sumsText,
                ),
            )
        } finally {
            apkFile.delete()
        }
    }

    @Test
    fun targetFileSanitizesAssetName() {
        val updatesDir = File("updates")

        assertEquals(
            File(updatesDir, "SentinelaCamTV-v1.1.0-arm64-v8a.apk").path,
            DownloadedUpdateCache.targetFile(
                updatesDir = updatesDir,
                assetName = "SentinelaCamTV-v1.1.0-arm64-v8a.apk",
            ).path,
        )
        assertEquals(
            File(updatesDir, ".._evil.apk").path,
            DownloadedUpdateCache.targetFile(
                updatesDir = updatesDir,
                assetName = "../evil.apk",
            ).path,
        )
    }

    private fun availableUpdate(assetName: String): AvailableUpdate =
        AvailableUpdate(
            versionName = "1.1.0",
            assetName = assetName,
            downloadUrl = "https://example.invalid/$assetName",
            checksumUrl = "https://example.invalid/SHA256SUMS.txt",
            releasePageUrl = "https://example.invalid/release",
            changelog = "Teste",
        )
}
