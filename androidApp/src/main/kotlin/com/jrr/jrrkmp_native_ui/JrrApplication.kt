package com.jrr.jrrkmp_native_ui

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.jrr.jrrkmp_native_ui.core.di.AppContainer
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
