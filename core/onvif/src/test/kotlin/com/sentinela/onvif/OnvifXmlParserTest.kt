package com.sentinela.onvif

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class OnvifXmlParserTest {
    @Test
    fun parseProbeMatchesReadsDeviceAddressAndXAddrs() {
        val devices = OnvifXmlParser.parseProbeMatches(
            """
            <s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope">
                <s:Body>
                    <d:ProbeMatches xmlns:d="http://schemas.xmlsoap.org/ws/2005/04/discovery">
                        <d:ProbeMatch>
                            <a:EndpointReference xmlns:a="http://schemas.xmlsoap.org/ws/2004/08/addressing">
                                <a:Address>urn:uuid:camera-1</a:Address>
                            </a:EndpointReference>
                            <d:Types>dn:NetworkVideoTransmitter</d:Types>
                            <d:XAddrs>http://198.51.100.50/onvif/device_service</d:XAddrs>
                            <d:Scopes>onvif://www.onvif.org/name/CAM1</d:Scopes>
                        </d:ProbeMatch>
                    </d:ProbeMatches>
                </s:Body>
            </s:Envelope>
            """.trimIndent(),
        )

        assertEquals("urn:uuid:camera-1", devices.single().endpointReference)
        assertEquals("http://198.51.100.50/onvif/device_service", devices.single().xAddrs.single())
    }

    @Test
    fun parseCapabilitiesReadsMediaAndDeviceServices() {
        val capabilities = OnvifXmlParser.parseCapabilities(
            """
            <s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope">
                <s:Body>
                    <tds:GetCapabilitiesResponse xmlns:tds="http://www.onvif.org/ver10/device/wsdl">
                        <tds:Capabilities>
                            <tt:Device xmlns:tt="http://www.onvif.org/ver10/schema">
                                <tt:XAddr>http://198.51.100.50/onvif/device_service</tt:XAddr>
                            </tt:Device>
                            <tt:Media xmlns:tt="http://www.onvif.org/ver10/schema">
                                <tt:XAddr>http://198.51.100.50/onvif/media_service</tt:XAddr>
                            </tt:Media>
                        </tds:Capabilities>
                    </tds:GetCapabilitiesResponse>
                </s:Body>
            </s:Envelope>
            """.trimIndent(),
        )

        assertEquals("http://198.51.100.50/onvif/media_service", capabilities.mediaXAddr)
        assertEquals("http://198.51.100.50/onvif/device_service", capabilities.deviceXAddr)
    }

    @Test
    fun parseProfilesReadsTokenNameAndFixedFlag() {
        val profiles = OnvifXmlParser.parseProfiles(
            """
            <trt:GetProfilesResponse xmlns:trt="http://www.onvif.org/ver10/media/wsdl">
                <trt:Profiles token="Profile_1" fixed="true">
                    <tt:Name xmlns:tt="http://www.onvif.org/ver10/schema">mainStream</tt:Name>
                </trt:Profiles>
            </trt:GetProfilesResponse>
            """.trimIndent(),
        )

        assertEquals("Profile_1", profiles.single().token)
        assertEquals("mainStream", profiles.single().name)
        assertTrue(profiles.single().fixed)
    }

    @Test
    fun parseStreamUriReadsRtspUri() {
        val streamUri = OnvifXmlParser.parseStreamUri(
            """
            <trt:GetStreamUriResponse xmlns:trt="http://www.onvif.org/ver10/media/wsdl">
                <trt:MediaUri>
                    <tt:Uri xmlns:tt="http://www.onvif.org/ver10/schema">rtsp://198.51.100.50/live</tt:Uri>
                    <tt:InvalidAfterConnect xmlns:tt="http://www.onvif.org/ver10/schema">false</tt:InvalidAfterConnect>
                    <tt:InvalidAfterReboot xmlns:tt="http://www.onvif.org/ver10/schema">true</tt:InvalidAfterReboot>
                    <tt:Timeout xmlns:tt="http://www.onvif.org/ver10/schema">PT60S</tt:Timeout>
                </trt:MediaUri>
            </trt:GetStreamUriResponse>
            """.trimIndent(),
        )

        assertEquals("rtsp://198.51.100.50/live", streamUri.uri)
        assertEquals("PT60S", streamUri.timeout)
    }

    @Test
    fun parseSoapFaultReadsCodeAndReason() {
        val fault = OnvifXmlParser.parseSoapFault(
            """
            <s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope">
                <s:Body>
                    <s:Fault>
                        <s:Code><s:Value>s:Sender</s:Value></s:Code>
                        <s:Reason><s:Text>Not authorized</s:Text></s:Reason>
                    </s:Fault>
                </s:Body>
            </s:Envelope>
            """.trimIndent(),
        )

        assertEquals("s:Sender", fault?.code)
        assertEquals("Not authorized", fault?.reason)
    }

    @Test
    fun rejectsDoctypeBeforeParsing() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            OnvifXmlParser.parseProbeMatches(
                """
                <!DOCTYPE root [
                    <!ENTITY external SYSTEM "file:///etc/passwd">
                ]>
                <root>&external;</root>
                """.trimIndent(),
            )
        }

        assertEquals("DOCTYPE não é permitido em XML ONVIF.", error.message)
    }

    @Test
    fun rejectsMixedCaseDoctypeBeforeParsing() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            OnvifXmlParser.parseCapabilities(
                """
                <!DocType root [
                    <!ENTITY external SYSTEM "file:///etc/passwd">
                ]>
                <root>&external;</root>
                """.trimIndent(),
            )
        }

        assertEquals("DOCTYPE não é permitido em XML ONVIF.", error.message)
    }

    @Test
    fun rejectsOversizedXmlBeforeParsing() {
        val oversizedXml = "<root>${"a".repeat(OnvifXmlParser.MAX_XML_BYTES)}</root>"

        val error = assertThrows(IllegalArgumentException::class.java) {
            OnvifXmlParser.parseProbeMatches(oversizedXml)
        }

        assertEquals("XML ONVIF excede o limite de tamanho.", error.message)
    }
}
