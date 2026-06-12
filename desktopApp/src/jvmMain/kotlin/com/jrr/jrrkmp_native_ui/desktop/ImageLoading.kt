package com.jrr.jrrkmp_native_ui.desktop

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

/**
 * Coil image-loading setup for the desktop host.
 *
 * Coil 3 ships no network fetcher in its core artifact (which is all
 * :composeUi brings in), so without explicit registration `AsyncImage` cannot
 * load any remote URL. This mirrors the Android setup in
 * `JrrApplication.newImageLoader`: an OkHttp fetcher backed by a trust-all
 * client, because JRiver Media Center presents a self-signed certificate on
 * its HTTPS port. The TLS helpers duplicate androidCore's `SslHelper`, which
 * lives in an Android library the JVM target cannot depend on.
 *
 * NOT safe for general HTTPS — the loader is used only for artwork fetches
 * from the user-configured MCWS server.
 */

private val trustAllTrustManager: X509TrustManager = object : X509TrustManager {
    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
}

/** Trust-all client for image loads from JRiver's self-signed admin port. */
private fun imageOkHttpClient(): OkHttpClient {
    val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, arrayOf(trustAllTrustManager), SecureRandom())
    }
    return OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .sslSocketFactory(sslContext.socketFactory, trustAllTrustManager)
        .hostnameVerifier { _, _ -> true }
        .build()
}

/**
 * Installs the app-wide [ImageLoader]. Call once from `main` before the first
 * composition so it is in place before any `AsyncImage` resolves.
 */
fun configureImageLoader() {
    SingletonImageLoader.setSafe { context ->
        ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { imageOkHttpClient() }))
            }
            .build()
    }
}
