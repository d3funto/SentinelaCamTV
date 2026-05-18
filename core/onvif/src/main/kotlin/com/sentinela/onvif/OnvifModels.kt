package com.sentinela.onvif

data class DiscoveredOnvifDevice(
    val endpointReference: String,
    val types: List<String>,
    val xAddrs: List<String>,
    val scopes: List<String>,
)

data class OnvifCapabilities(
    val mediaXAddr: String?,
    val deviceXAddr: String?,
)

data class OnvifMediaProfile(
    val token: String,
    val name: String,
    val fixed: Boolean,
)

data class OnvifStreamUri(
    val uri: String,
    val invalidAfterConnect: Boolean,
    val invalidAfterReboot: Boolean,
    val timeout: String?,
)

sealed interface OnvifFailure {
    data object NetworkOffline : OnvifFailure
    data object AuthenticationFailed : OnvifFailure
    data object Timeout : OnvifFailure
    data class SoapFault(val code: String?, val reason: String?) : OnvifFailure
    data class Unknown(val message: String?) : OnvifFailure
}
