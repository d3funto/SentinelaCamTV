package com.sentinela.camtv.data.update

import java.io.File

object DownloadedUpdateCache {
    fun targetFile(
        updatesDir: File,
        assetName: String,
    ): File =
        File(updatesDir, sanitizedFileName(assetName))

    fun validDownloadedUpdateOrNull(
        update: AvailableUpdate,
        downloadedFile: File,
        sumsText: String,
    ): DownloadedUpdate? {
        if (!downloadedFile.isFile) return null

        val expectedSha256 = Sha256Sums.expectedHashFor(
            sumsText = sumsText,
            fileName = update.assetName,
        ) ?: return null
        val actualSha256 = Sha256Sums.sha256(downloadedFile)
        return if (expectedSha256.equals(actualSha256, ignoreCase = true)) {
            DownloadedUpdate(
                update = update,
                filePath = downloadedFile.absolutePath,
            )
        } else {
            null
        }
    }

    fun sanitizedFileName(value: String): String =
        value.replace(Regex("[^A-Za-z0-9._-]"), "_")
            .ifBlank { "sentinela-update.apk" }
}
