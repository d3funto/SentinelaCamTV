package com.sentinela.camtv.data.update

object GitHubReleaseParser {
    fun parse(json: String): GitHubRelease {
        val assets = json.extractArray("assets")
            ?.extractObjects()
            ?.mapNotNull { assetJson ->
                val name = assetJson.stringField("name")
                val downloadUrl = assetJson.stringField("browser_download_url")
                if (name.isNotBlank() && downloadUrl.isNotBlank()) {
                    GitHubReleaseAsset(
                        name = name,
                        downloadUrl = downloadUrl,
                    )
                } else {
                    null
                }
            }.orEmpty()

        return GitHubRelease(
            tagName = json.stringField("tag_name"),
            htmlUrl = json.stringField("html_url"),
            body = json.stringField("body"),
            assets = assets,
        )
    }

    private fun String.stringField(fieldName: String): String {
        val match = Regex(
            pattern = "\"${Regex.escape(fieldName)}\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"",
        ).find(this)
        return match?.groupValues?.getOrNull(1)?.unescapeJsonString().orEmpty()
    }

    private fun String.extractArray(fieldName: String): String? {
        val fieldMatch = Regex(
            pattern = "\"${Regex.escape(fieldName)}\"\\s*:",
        ).find(this) ?: return null
        val startIndex = indexOf('[', startIndex = fieldMatch.range.last + 1)
        if (startIndex < 0) return null
        val endIndex = endOfBalancedBlock(
            startIndex = startIndex,
            open = '[',
            close = ']',
        )
        if (endIndex < 0) return null
        return substring(startIndex + 1, endIndex)
    }

    private fun String.extractObjects(): List<String> {
        val objects = mutableListOf<String>()
        var index = 0
        while (index < length) {
            val startIndex = indexOf('{', startIndex = index)
            if (startIndex < 0) break
            val endIndex = endOfBalancedBlock(
                startIndex = startIndex,
                open = '{',
                close = '}',
            )
            if (endIndex < 0) break
            objects += substring(startIndex, endIndex + 1)
            index = endIndex + 1
        }
        return objects
    }

    private fun String.endOfBalancedBlock(
        startIndex: Int,
        open: Char,
        close: Char,
    ): Int {
        var depth = 0
        var inString = false
        var escaped = false

        for (index in startIndex until length) {
            val char = this[index]
            if (inString) {
                when {
                    escaped -> escaped = false
                    char == '\\' -> escaped = true
                    char == '"' -> inString = false
                }
            } else {
                when (char) {
                    '"' -> inString = true
                    open -> depth += 1
                    close -> {
                        depth -= 1
                        if (depth == 0) return index
                    }
                }
            }
        }

        return -1
    }

    private fun String.unescapeJsonString(): String {
        val builder = StringBuilder(length)
        var index = 0
        while (index < length) {
            val char = this[index]
            if (char != '\\' || index == lastIndex) {
                builder.append(char)
                index += 1
                continue
            }

            val escaped = this[index + 1]
            when (escaped) {
                '"' -> builder.append('"')
                '\\' -> builder.append('\\')
                '/' -> builder.append('/')
                'b' -> builder.append('\b')
                'f' -> builder.append('\u000C')
                'n' -> builder.append('\n')
                'r' -> builder.append('\r')
                't' -> builder.append('\t')
                'u' -> {
                    val hexStart = index + 2
                    val hexEnd = hexStart + 4
                    if (hexEnd <= length) {
                        this.substring(hexStart, hexEnd)
                            .toIntOrNull(16)
                            ?.toChar()
                            ?.let(builder::append)
                        index += 4
                    }
                }
                else -> builder.append(escaped)
            }
            index += 2
        }
        return builder.toString()
    }
}
