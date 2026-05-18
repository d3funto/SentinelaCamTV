package com.sentinela.camtv.logging

import android.content.Context
import android.os.Environment
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalLogRepository(
    private val context: Context,
    private val fileTimberTree: FileTimberTree,
    private val crashReporter: CrashReporter,
) : LogRepository {
    override suspend fun exportSupportLogs(): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val output = File(downloadDir(), "sentinela-logs.txt")
            output.printWriter().use { writer ->
                fileTimberTree.logFiles().forEach { file ->
                    writer.println("===== ${file.name} =====")
                    writer.println(file.readText())
                }
            }
            output
        }
    }

    override suspend fun exportCrashReport(): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val output = File(downloadDir(), "sentinela-crashes.txt")
            output.printWriter().use { writer ->
                crashReporter.crashFiles().forEach { file ->
                    writer.println("===== ${file.name} =====")
                    writer.println(file.readText())
                }
            }
            output
        }
    }

    private fun downloadDir(): File =
        (context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir)
            .apply { mkdirs() }
}
