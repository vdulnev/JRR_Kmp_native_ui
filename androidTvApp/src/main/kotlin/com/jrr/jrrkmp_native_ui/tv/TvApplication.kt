package com.jrr.jrrkmp_native_ui.tv

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.jrr.jrrkmp_native_ui.core.logging.AppLogger
import com.jrr.jrrkmp_native_ui.core.network.acceptAllHostnameVerifier
import com.jrr.jrrkmp_native_ui.core.network.trustAllSslSocketFactory
import com.jrr.jrrkmp_native_ui.core.network.trustAllTrustManager
import com.jrr.jrrkmp_native_ui.tv.di.TvAppContainer
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class TvApplication : Application(), SingletonImageLoader.Factory {

    lateinit var container: TvAppContainer
        private set

    /** Trust-all client for artwork loads from JRiver's self-signed admin port. */
    private val imageOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .sslSocketFactory(trustAllSslSocketFactory, trustAllTrustManager)
            .hostnameVerifier(acceptAllHostnameVerifier)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        // Bootstrap logging FIRST so container init events are captured.
        val isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        AppLogger.configure(isDebug = isDebug)
        container = TvAppContainer(this)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { imageOkHttpClient }))
            }
            .build()
}

/** Convenience accessor: `context.tvContainer.facade`. */
val Context.tvContainer: TvAppContainer
    get() = (applicationContext as TvApplication).container
