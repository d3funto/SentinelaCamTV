package com.sentinela.camtv.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File

class AppUpdateInstaller(
    context: Context,
) {
    private val appContext = context.applicationContext

    fun openInstaller(update: DownloadedUpdate): Result<Unit> = runCatching {
        val apkFile = File(update.filePath)
        check(apkFile.isFile) {
            "APK baixado não encontrado."
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !appContext.packageManager.canRequestPackageInstalls()
        ) {
            openUnknownAppsSettings()
            error("Permita instalar apps desconhecidos para o Sentinela Cam TV e tente novamente.")
        }

        val apkUri = FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.fileprovider",
            apkFile,
        )
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, APK_MIME_TYPE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        check(installIntent.resolveActivity(appContext.packageManager) != null) {
            "Nenhum instalador de APK foi encontrado neste aparelho."
        }

        appContext.startActivity(installIntent)
    }

    private fun openUnknownAppsSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val settingsIntent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${appContext.packageName}"),
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (settingsIntent.resolveActivity(appContext.packageManager) != null) {
            appContext.startActivity(settingsIntent)
        }
    }

    private companion object {
        private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    }
}
