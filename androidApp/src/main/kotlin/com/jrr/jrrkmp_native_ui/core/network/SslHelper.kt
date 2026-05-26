package com.jrr.jrrkmp_native_ui.core.network

import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * Trust-all + verify-nothing TLS helpers used for talking to JRiver Media
 * Center, which typically presents a self-signed certificate on its admin port.
 *
 * NOT safe for general HTTPS — use only when the user has explicitly opted into
 * the JRiver server connection.
 */

val trustAllTrustManager: X509TrustManager = object : X509TrustManager {
    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
}

val trustAllSslSocketFactory: SSLSocketFactory by lazy {
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, arrayOf(trustAllTrustManager), SecureRandom())
    sslContext.socketFactory
}

val acceptAllHostnameVerifier: HostnameVerifier = HostnameVerifier { _, _ -> true }
