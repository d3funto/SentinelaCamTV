package com.sentinela.onvif

import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

object OnvifXmlParser {
    fun parseProbeMatches(xml: String): List<DiscoveredOnvifDevice> {
        val document = xml.toDocument()
        return document.elements("ProbeMatch").map { match ->
            DiscoveredOnvifDevice(
                endpointReference = match.firstText("Address").orEmpty(),
                types = match.firstText("Types").splitWords(),
                xAddrs = match.firstText("XAddrs").splitWords(),
                scopes = match.firstText("Scopes").splitWords(),
            )
        }
    }

    fun parseCapabilities(xml: String): OnvifCapabilities {
        val document = xml.toDocument()
        return OnvifCapabilities(
            mediaXAddr = document.elements("Media").firstOrNull()?.firstText("XAddr"),
            deviceXAddr = document.elements("Device").firstOrNull()?.firstText("XAddr"),
        )
    }

    fun parseProfiles(xml: String): List<OnvifMediaProfile> {
        val document = xml.toDocument()
        return document.elements("Profiles").mapNotNull { profile ->
            val token = profile.getAttribute("token").takeIf { it.isNotBlank() } ?: return@mapNotNull null
            OnvifMediaProfile(
                token = token,
                name = profile.firstText("Name").orEmpty(),
                fixed = profile.getAttribute("fixed").equals("true", ignoreCase = true),
            )
        }
    }

    fun parseStreamUri(xml: String): OnvifStreamUri {
        val document = xml.toDocument()
        val mediaUri = document.elements("MediaUri").firstOrNull() ?: document.documentElement
        return OnvifStreamUri(
            uri = mediaUri.firstText("Uri").orEmpty(),
            invalidAfterConnect = mediaUri.firstText("InvalidAfterConnect")?.toBooleanStrictOrNull() ?: false,
            invalidAfterReboot = mediaUri.firstText("InvalidAfterReboot")?.toBooleanStrictOrNull() ?: false,
            timeout = mediaUri.firstText("Timeout"),
        )
    }

    fun parseSoapFault(xml: String): OnvifFailure.SoapFault? {
        val document = runCatching { xml.toDocument() }.getOrNull() ?: return null
        val fault = document.elements("Fault").firstOrNull() ?: return null
        return OnvifFailure.SoapFault(
            code = fault.firstText("Value") ?: fault.firstText("Code"),
            reason = fault.firstText("Text") ?: fault.firstText("Reason"),
        )
    }

    private fun String.toDocument(): Document {
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        }
        return factory.newDocumentBuilder().parse(ByteArrayInputStream(toByteArray()))
    }

    private fun Document.elements(localName: String): List<Element> =
        documentElement.descendantElements(localName)

    private fun Element.firstText(localName: String): String? =
        descendantElements(localName).firstOrNull()?.textContent?.trim()

    private fun Element.descendantElements(localName: String): List<Element> {
        val result = mutableListOf<Element>()
        fun visit(node: Node) {
            if (node is Element && node.localName == localName) {
                result += node
            }
            val children = node.childNodes
            for (index in 0 until children.length) {
                visit(children.item(index))
            }
        }
        visit(this)
        return result
    }

    private fun String?.splitWords(): List<String> =
        orEmpty().split(Regex("\\s+")).filter { it.isNotBlank() }
}
