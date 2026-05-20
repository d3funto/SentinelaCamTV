package com.sentinela.camtv.data.update

object AppVersion {
    fun isNewer(
        candidate: String,
        current: String,
    ): Boolean {
        val candidateParts = candidate.toVersionParts()
        val currentParts = current.toVersionParts()
        val maxSize = maxOf(candidateParts.size, currentParts.size)

        for (index in 0 until maxSize) {
            val candidateValue = candidateParts.getOrElse(index) { 0 }
            val currentValue = currentParts.getOrElse(index) { 0 }
            if (candidateValue != currentValue) {
                return candidateValue > currentValue
            }
        }

        return false
    }

    fun normalize(version: String): String =
        version.trim()
            .removePrefix("v")
            .removePrefix("V")
            .substringBefore("-")

    private fun String.toVersionParts(): List<Int> =
        normalize(this)
            .split(".")
            .mapNotNull { part -> part.toIntOrNull() }
}
