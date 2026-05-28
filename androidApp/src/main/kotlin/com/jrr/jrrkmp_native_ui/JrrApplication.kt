package com.jrr.jrrkmp_native_ui

import android.app.Application
import android.content.pm.ApplicationInfo
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.jrr.jrrkmp_native_ui.core.di.AppContainer
import com.jrr.jrrkmp_native_ui.core.logging.AppLogger
import com.jrr.jrrkmp_native_ui.core.network.acceptAllHostnameVerifier
import com.jrr.jrrkmp_native_ui.core.network.trustAllSslSocketFactory
import com.jrr.jrrkmp_native_ui.core.network.trustAllTrustManager
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class JrrApplication : Application(), ImageLoaderFactory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // Bootstrap logging FIRST so AppContainer init events end up captured.
        val isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        AppLogger.configure(isDebug = isDebug)
        container = AppContainer(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient {
                OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .sslSocketFactory(trustAllSslSocketFactory, trustAllTrustManager)
                    .hostnameVerifier(acceptAllHostnameVerifier)
                    .build()
            }
            .build()
    }
}
