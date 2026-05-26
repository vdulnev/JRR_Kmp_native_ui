package com.jrr.jrrkmp_native_ui

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.jrr.jrrkmp_native_ui.core.di.AppContainer
import com.jrr.jrrkmp_native_ui.core.network.SslHelper
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
                    .sslSocketFactory(SslHelper.sslSocketFactory, SslHelper.trustAllTrustManager)
                    .hostnameVerifier(SslHelper.hostnameVerifier)
                    .build()
            }
            .build()
    }
}
