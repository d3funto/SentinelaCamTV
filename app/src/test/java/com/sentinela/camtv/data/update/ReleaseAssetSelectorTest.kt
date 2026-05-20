package com.sentinela.camtv.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReleaseAssetSelectorTest {
    @Test
    fun selectUsesPrimaryAbiFirst() {
        val selected = ReleaseAssetSelector.selectApk(
            assets = listOf(
                asset("SentinelaCamTV-v1.0.1-arm64-v8a.apk"),
                asset("SentinelaCamTV-v1.0.1-armeabi-v7a.apk"),
                asset("SentinelaCamTV-v1.0.1-universal.apk"),
            ),
            supportedAbis = listOf("armeabi-v7a", "arm64-v8a"),
        )

        assertEquals("SentinelaCamTV-v1.0.1-armeabi-v7a.apk", selected?.name)
    }

    @Test
    fun selectFallsBackToSecondarySupportedAbi() {
        val selected = ReleaseAssetSelector.selectApk(
            assets = listOf(
                asset("SentinelaCamTV-v1.0.1-arm64-v8a.apk"),
                asset("SentinelaCamTV-v1.0.1-universal.apk"),
            ),
            supportedAbis = listOf("armeabi-v7a", "arm64-v8a"),
        )

        assertEquals("SentinelaCamTV-v1.0.1-arm64-v8a.apk", selected?.name)
    }

    @Test
    fun selectFallsBackToUniversalAsset() {
        val selected = ReleaseAssetSelector.selectApk(
            assets = listOf(
                asset("SentinelaCamTV-v1.0.1-universal.apk"),
            ),
            supportedAbis = listOf("armeabi-v7a"),
        )

        assertEquals("SentinelaCamTV-v1.0.1-universal.apk", selected?.name)
    }

    @Test
    fun selectDoesNotMistakeX86ForX8664() {
        val selected = ReleaseAssetSelector.selectApk(
            assets = listOf(
                asset("SentinelaCamTV-v1.0.1-x86_64.apk"),
                asset("SentinelaCamTV-v1.0.1-x86.apk"),
            ),
            supportedAbis = listOf("x86"),
        )

        assertEquals("SentinelaCamTV-v1.0.1-x86.apk", selected?.name)
    }

    @Test
    fun selectReturnsNullWhenNoCompatibleApkExists() {
        val selected = ReleaseAssetSelector.selectApk(
            assets = listOf(
                asset("SentinelaCamTV-v1.0.1-arm64-v8a.apk"),
                asset("SentinelaCamTV-v1.0.1-release-notes.txt"),
            ),
            supportedAbis = listOf("x86"),
        )

        assertNull(selected)
    }

    private fun asset(name: String): GitHubReleaseAsset =
        GitHubReleaseAsset(
            name = name,
            downloadUrl = "https://example.invalid/$name",
        )
}
