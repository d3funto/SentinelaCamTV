package com.sentinela.camtv.data.update

data class GitHubRelease(
    val tagName: String,
    val htmlUrl: String,
    val body: String,
    val assets: List<GitHubReleaseAsset>,
)

data class GitHubReleaseAsset(
    val name: String,
    val downloadUrl: String,
)

data class AvailableUpdate(
    val versionName: String,
    val assetName: String,
    val downloadUrl: String,
    val releasePageUrl: String,
    val changelog: String,
)

data class DownloadedUpdate(
    val update: AvailableUpdate,
    val filePath: String,
)

sealed interface UpdateCheckResult {
    data object UpToDate : UpdateCheckResult
    data class Available(val update: AvailableUpdate) : UpdateCheckResult
}
