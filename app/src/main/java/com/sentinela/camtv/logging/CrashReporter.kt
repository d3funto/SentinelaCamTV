package com.sentinela.camtv.logging

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashReporter(
    context: Context,
) {
    private val crashDir = File(context.filesDir, "crashes").apply { mkdirs() }
    private val timestampFormat = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
    private var previousHandler: Thread.UncaughtExceptionHandler? = null

    fun install() {
        if (previousHandler != null) return
        previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            writeCrash(thread, throwable)
            previousHandler?.uncaughtException(thread, throwable)
        }
    }

    fun crashFiles(): List<File> =
        crashDir.listFiles { file -> file.name.endsWith(".txt") }?.sortedByDescending { it.name }.orEmpty()

    private fun writeCrash(thread: Thread, throwable: Throwable) {
        runCatching {
            val file = File(crashDir, "crash-${timestampFormat.format(Date())}.txt")
            file.writeText(
                buildString {
                    appendLine("Sentinela Cam TV crash report")
                    appendLine("Thread: ${thread.name}")
                    appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
                    appendLine("SDK: ${Build.VERSION.SDK_INT}")
                    appendLine()
                    appendLine(Log.getStackTraceString(throwable))
                },
            )
        }
    }
}
