package com.sentinela.camtv.logging

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import timber.log.Timber

class FileTimberTree(
    context: Context,
    private val maxBytes: Long = 256 * 1024,
) : Timber.Tree() {
    private val logDir = File(context.filesDir, "logs").apply { mkdirs() }
    private val timestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    val currentLogFile: File
        get() = File(logDir, "sentinela.log")

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val priorityLabel = when (priority) {
            Log.VERBOSE -> "V"
            Log.DEBUG -> "D"
            Log.INFO -> "I"
            Log.WARN -> "W"
            Log.ERROR -> "E"
            else -> priority.toString()
        }
        val line = buildString {
            append(timestampFormat.format(Date()))
            append(' ')
            append(priorityLabel)
            append('/')
            append(tag ?: "Sentinela")
            append(": ")
            append(message)
            if (t != null) {
                append('\n')
                append(Log.getStackTraceString(t))
            }
            append('\n')
        }

        synchronized(this) {
            rotateIfNeeded()
            currentLogFile.appendText(line)
        }
    }

    fun logFiles(): List<File> =
        logDir.listFiles { file -> file.name.endsWith(".log") }?.sortedBy { it.name }.orEmpty()

    private fun rotateIfNeeded() {
        val file = currentLogFile
        if (file.length() <= maxBytes) return
        val previous = File(logDir, "sentinela.previous.log")
        if (previous.exists()) previous.delete()
        file.renameTo(previous)
    }
}
