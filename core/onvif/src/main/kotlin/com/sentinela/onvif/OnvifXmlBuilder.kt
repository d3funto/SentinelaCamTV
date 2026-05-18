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

    fun getCapabilities(): String =
        soapBody(
            body = """
            <tds:GetCapabilities xmlns:tds="http://www.onvif.org/ver10/device/wsdl">
                <tds:Category>All</tds:Category>
            </tds:GetCapabilities>
            """.trimIndent(),
        )

    fun getProfiles(): String =
        soapBody(
            body = """
            <trt:GetProfiles xmlns:trt="http://www.onvif.org/ver10/media/wsdl" />
            """.trimIndent(),
        )

    fun getStreamUri(profileToken: String): String =
        soapBody(
            body = """
            <trt:GetStreamUri xmlns:trt="http://www.onvif.org/ver10/media/wsdl">
                <trt:StreamSetup>
                    <tt:Stream xmlns:tt="http://www.onvif.org/ver10/schema">RTP-Unicast</tt:Stream>
                    <tt:Transport xmlns:tt="http://www.onvif.org/ver10/schema">
                        <tt:Protocol>RTSP</tt:Protocol>
                    </tt:Transport>
                </trt:StreamSetup>
                <trt:ProfileToken>$profileToken</trt:ProfileToken>
            </trt:GetStreamUri>
            """.trimIndent(),
        )

    private fun soapBody(body: String): String =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope">
            <s:Body>
                $body
            </s:Body>
        </s:Envelope>
        """.trimIndent()
}
