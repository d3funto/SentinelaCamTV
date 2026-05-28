package com.sentinela.onvif

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class OnvifSoapClient(
    private val connectTimeoutMillis: Int = 5_000,
    private val readTimeoutMillis: Int = 5_000,
    private val usernameTokenFactory: OnvifUsernameTokenFactory = OnvifUsernameTokenFactory(),
) {
    fun getCapabilities(
        deviceServiceUrl: String,
        credentials: OnvifCredentials? = null,
    ): Result<OnvifCapabilities> =
        post(deviceServiceUrl, OnvifXmlBuilder.getCapabilities(credentials.usernameToken()))
            .mapCatching { xml -> OnvifXmlParser.parseCapabilities(xml) }

    fun getProfiles(
        mediaServiceUrl: String,
        credentials: OnvifCredentials? = null,
    ): Result<List<OnvifMediaProfile>> =
        post(mediaServiceUrl, OnvifXmlBuilder.getProfiles(credentials.usernameToken()))
            .mapCatching { xml -> OnvifXmlParser.parseProfiles(xml) }

    fun getStreamUri(
        mediaServiceUrl: String,
        profileToken: String,
        credentials: OnvifCredentials? = null,
    ): Result<OnvifStreamUri> =
        post(mediaServiceUrl, OnvifXmlBuilder.getStreamUri(profileToken, credentials.usernameToken()))
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
        val xml = stream.readOnvifXmlText()
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

    private fun OnvifCredentials?.usernameToken(): OnvifUsernameToken? =
        this?.takeIf { it.isConfigured }?.let(usernameTokenFactory::create)

    private fun InputStream.readOnvifXmlText(): String = use { input ->
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var totalBytes = 0
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            totalBytes += read
            if (totalBytes > OnvifXmlParser.MAX_XML_BYTES) {
                throw IOException("Resposta ONVIF excede o limite de tamanho.")
            }
            output.write(buffer, 0, read)
        }
        output.toString(Charsets.UTF_8.name())
    }
}
