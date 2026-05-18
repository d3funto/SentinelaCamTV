package com.sentinela.onvif

import java.util.UUID

object OnvifXmlBuilder {
    fun probe(messageId: String = "uuid:${UUID.randomUUID()}"): String =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <e:Envelope xmlns:e="http://www.w3.org/2003/05/soap-envelope"
            xmlns:w="http://schemas.xmlsoap.org/ws/2004/08/addressing"
            xmlns:d="http://schemas.xmlsoap.org/ws/2005/04/discovery"
            xmlns:dn="http://www.onvif.org/ver10/network/wsdl">
            <e:Header>
                <w:MessageID>$messageId</w:MessageID>
                <w:To>urn:schemas-xmlsoap-org:ws:2005:04:discovery</w:To>
                <w:Action>http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</w:Action>
            </e:Header>
            <e:Body>
                <d:Probe>
                    <d:Types>dn:NetworkVideoTransmitter</d:Types>
                </d:Probe>
            </e:Body>
        </e:Envelope>
        """.trimIndent()

    fun getCapabilities(usernameToken: OnvifUsernameToken? = null): String =
        soapBody(
            usernameToken = usernameToken,
            body = """
            <tds:GetCapabilities xmlns:tds="http://www.onvif.org/ver10/device/wsdl">
                <tds:Category>All</tds:Category>
            </tds:GetCapabilities>
            """.trimIndent(),
        )

    fun getProfiles(usernameToken: OnvifUsernameToken? = null): String =
        soapBody(
            usernameToken = usernameToken,
            body = """
            <trt:GetProfiles xmlns:trt="http://www.onvif.org/ver10/media/wsdl" />
            """.trimIndent(),
        )

    fun getStreamUri(
        profileToken: String,
        usernameToken: OnvifUsernameToken? = null,
    ): String =
        soapBody(
            usernameToken = usernameToken,
            body = """
            <trt:GetStreamUri xmlns:trt="http://www.onvif.org/ver10/media/wsdl">
                <trt:StreamSetup>
                    <tt:Stream xmlns:tt="http://www.onvif.org/ver10/schema">RTP-Unicast</tt:Stream>
                    <tt:Transport xmlns:tt="http://www.onvif.org/ver10/schema">
                        <tt:Protocol>RTSP</tt:Protocol>
                    </tt:Transport>
                </trt:StreamSetup>
                <trt:ProfileToken>${profileToken.xmlEscaped()}</trt:ProfileToken>
            </trt:GetStreamUri>
            """.trimIndent(),
        )

    private fun soapBody(
        body: String,
        usernameToken: OnvifUsernameToken?,
    ): String {
        val header = usernameToken?.let(::securityHeader).orEmpty()
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope">
                $header
                <s:Body>
                    $body
                </s:Body>
            </s:Envelope>
            """.trimIndent()
    }

    private fun securityHeader(usernameToken: OnvifUsernameToken): String =
        """
        <s:Header>
            <wsse:Security
                xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"
                xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
                <wsse:UsernameToken>
                    <wsse:Username>${usernameToken.username.xmlEscaped()}</wsse:Username>
                    <wsse:Password
                        Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest">${usernameToken.passwordDigest}</wsse:Password>
                    <wsse:Nonce
                        EncodingType="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary">${usernameToken.nonceBase64}</wsse:Nonce>
                    <wsu:Created>${usernameToken.created}</wsu:Created>
                </wsse:UsernameToken>
            </wsse:Security>
        </s:Header>
        """.trimIndent()

    private fun String.xmlEscaped(): String =
        replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
}
