package com.sentinela.camtv.data.update

object ReleaseAssetSelector {
    fun selectApk(
        assets: List<GitHubReleaseAsset>,
        supportedAbis: List<String>,
    ): GitHubReleaseAsset? {
        val apkAssets = assets.filter { asset ->
            asset.name.endsWith(".apk", ignoreCase = true)
        }
        if (apkAssets.isEmpty()) return null

        supportedAbis.forEach { abi ->
            apkAssets.firstOrNull { asset ->
                asset.name.containsAbiMarker(abi)
            }?.let { return it }
        }

        apkAssets.firstOrNull { asset ->
            asset.name.containsAbiMarker("universal")
        }?.let { return it }

        return null
    }

    private fun String.containsAbiMarker(value: String): Boolean =
        Regex(
            pattern = "(^|[^A-Za-z0-9_])${Regex.escape(value)}([^A-Za-z0-9_]|$)",
            option = RegexOption.IGNORE_CASE,
        ).containsMatchIn(this)
}
