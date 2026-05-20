package com.sentinela.camtv.data.onvif

import com.sentinela.onvif.OnvifMediaProfile
import com.sentinela.onvif.OnvifStreamUri
import java.net.URI

data class OnvifProfileSelection(
    val main: OnvifMediaProfile,
    val sub: OnvifMediaProfile?,
)

data class ResolvedOnvifProfile(
    val profile: OnvifMediaProfile,
    val streamUri: OnvifStreamUri,
)

data class OnvifCameraProfileSelection(
    val groupKey: String,
    val channelNumber: Int?,
    val main: ResolvedOnvifProfile,
    val sub: ResolvedOnvifProfile?,
)

object OnvifProfileSelector {
    const val MAX_CAMERAS_PER_ONVIF_DEVICE = 16

    fun select(profiles: List<OnvifMediaProfile>): OnvifProfileSelection? {
        if (profiles.isEmpty()) return null
        val main = profiles.firstOrNull { profile ->
            val name = profile.name.lowercase()
            "main" in name || "principal" in name || "hd" in name
        } ?: profiles.first()
        val sub = profiles.firstOrNull { profile ->
            profile.token != main.token && profile.name.lowercase().let { name ->
                "sub" in name || "low" in name || "sd" in name
            }
        } ?: profiles.firstOrNull { profile -> profile.token != main.token }

        return OnvifProfileSelection(main = main, sub = sub)
    }

    fun selectCameras(profiles: List<ResolvedOnvifProfile>): List<OnvifCameraProfileSelection> {
        if (profiles.isEmpty()) return emptyList()
        return profiles
            .groupBy { profile -> profile.channelNumber()?.let { "channel-$it" } ?: "single-camera" }
            .map { (groupKey, groupProfiles) ->
                val sortedProfiles = groupProfiles.sortedWith(
                    compareBy<ResolvedOnvifProfile> { it.channelNumber() ?: Int.MAX_VALUE }
                        .thenBy { it.subtypeNumber() ?: Int.MAX_VALUE }
                        .thenBy { it.profile.token },
                )
                val main = sortedProfiles.firstOrNull { it.isMainProfile() } ?: sortedProfiles.first()
                val sub = sortedProfiles.firstOrNull { profile ->
                    profile.profile.token != main.profile.token && profile.isSubProfile()
                } ?: sortedProfiles.firstOrNull { profile -> profile.profile.token != main.profile.token }
                OnvifCameraProfileSelection(
                    groupKey = groupKey,
                    channelNumber = sortedProfiles.firstNotNullOfOrNull { it.channelNumber() },
                    main = main,
                    sub = sub,
                )
            }
            .sortedWith(compareBy<OnvifCameraProfileSelection> { it.channelNumber ?: Int.MAX_VALUE }.thenBy { it.groupKey })
            .take(MAX_CAMERAS_PER_ONVIF_DEVICE)
    }

    private fun ResolvedOnvifProfile.isMainProfile(): Boolean {
        subtypeNumber()?.let { subtype -> return subtype == 0 }
        val name = profile.name.lowercase()
        return "main" in name || "principal" in name || "hd" in name
    }

    private fun ResolvedOnvifProfile.isSubProfile(): Boolean {
        subtypeNumber()?.let { subtype -> return subtype > 0 }
        val name = profile.name.lowercase()
        return "sub" in name || "low" in name || "sd" in name
    }

    private fun ResolvedOnvifProfile.channelNumber(): Int? =
        streamUri.uri.queryParameter("channel")?.toIntOrNull()

    private fun ResolvedOnvifProfile.subtypeNumber(): Int? =
        streamUri.uri.queryParameter("subtype")?.toIntOrNull()

    private fun String.queryParameter(name: String): String? {
        val query = runCatching { URI(this).rawQuery }.getOrNull() ?: return null
        return query
            .split('&')
            .firstOrNull { parameter -> parameter.substringBefore('=') == name }
            ?.substringAfter('=', missingDelimiterValue = "")
            ?.takeIf { it.isNotBlank() }
    }
}
