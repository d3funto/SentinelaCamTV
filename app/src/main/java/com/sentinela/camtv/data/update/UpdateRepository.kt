package com.sentinela.camtv.data.update

interface UpdateRepository {
    suspend fun checkForUpdate(
        currentVersionName: String,
        supportedAbis: List<String>,
    ): Result<UpdateCheckResult>

    suspend fun downloadUpdate(update: AvailableUpdate): Result<DownloadedUpdate>

    suspend fun findDownloadedUpdate(update: AvailableUpdate): Result<DownloadedUpdate?>
}
