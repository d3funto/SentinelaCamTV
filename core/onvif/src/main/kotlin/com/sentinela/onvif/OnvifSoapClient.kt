package com.sentinela.onvif

import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class OnvifSoapClient(
    private val connectTimeoutMillis: Int = 5_000,
    private val readTimeoutMillis: Int = 5_000,
) {
    fun getCapabilities(deviceServiceUrl: String): Result<OnvifCapabilities> =
        post(deviceServiceUrl, OnvifXmlBuilder.getCapabilities())
            .mapCatching { xml -> OnvifXmlParser.parseCapabilities(xml) }

    fun getProfiles(mediaServiceUrl: String): Result<List<OnvifMediaProfile>> =
        post(mediaServiceUrl, OnvifXmlBuilder.getProfiles())
            .mapCatching { xml -> OnvifXmlParser.parseProfiles(xml) }

    fun getStreamUri(
        mediaServiceUrl: String,
        profileToken: String,
    ): Result<OnvifStreamUri> =
        post(mediaServiceUrl, OnvifXmlBuilder.getStreamUri(profileToken))
            .mapCatching { xml -> OnvifXmlParser.parseStreamUri(xml) }

    private fun post(
        endpoint: String,
        body: String,
    ): Result<String> = runCatching {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = connectTimeoutMillis
            readTimeout = readTimeoutMillis
            doOutput = true
            setRequestProperty("Content-Type", "application/soap+xml; charset=utf-8")
        }

        connection.outputStream.use { output ->
            output.write(body.toByteArray())
        }

        val responseCode = connection.responseCode
        val stream = if (responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: throw IOException("ONVIF HTTP $responseCode")
        }
        val xml = stream.bufferedReader().use { it.readText() }
        OnvifXmlParser.parseSoapFault(xml)?.let { fault ->
            throw IOException("ONVIF SOAP fault ${fault.code}: ${fault.reason}")
        }
        xml
    }.recoverCatching { error ->
        when (error) {
            is SocketTimeoutException -> throw IOException("ONVIF timeout", error)
            else -> throw error
        }
    }
}
