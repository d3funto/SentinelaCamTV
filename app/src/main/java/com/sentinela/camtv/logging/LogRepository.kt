package com.sentinela.camtv.logging

import java.io.File

interface LogRepository {
    suspend fun exportSupportLogs(): Result<File>
    suspend fun exportCrashReport(): Result<File>
}
