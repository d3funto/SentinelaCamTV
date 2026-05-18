package com.sentinela.camtv.data.onvif

object OnvifEndpointNormalizer {
    fun normalize(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return null
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            return trimmed
        }
        return "http://$trimmed/onvif/device_service"
    }
}
