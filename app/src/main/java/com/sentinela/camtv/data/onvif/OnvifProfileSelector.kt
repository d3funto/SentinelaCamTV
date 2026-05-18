package com.sentinela.camtv.data.onvif

import com.sentinela.onvif.OnvifMediaProfile

data class OnvifProfileSelection(
    val main: OnvifMediaProfile,
    val sub: OnvifMediaProfile?,
)

object OnvifProfileSelector {
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
}
