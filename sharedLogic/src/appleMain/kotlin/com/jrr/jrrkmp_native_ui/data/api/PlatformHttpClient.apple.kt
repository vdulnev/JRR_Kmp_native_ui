package com.jrr.jrrkmp_native_ui.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import platform.Foundation.NSURLCredential
import platform.Foundation.NSURLSessionAuthChallengeUseCredential
import platform.Foundation.NSURLSessionAuthChallengePerformDefaultHandling
import platform.Foundation.create
import platform.Foundation.serverTrust

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun createPlatformHttpClient(): HttpClient {
    return HttpClient(Darwin) {
        engine {
            handleChallenge { _, _, challenge, completionHandler ->
                val serverTrust = challenge.protectionSpace.serverTrust
                if (serverTrust != null) {
                    completionHandler(
                        NSURLSessionAuthChallengeUseCredential.convert(),
                        NSURLCredential.create(serverTrust)
                    )
                } else {
                    completionHandler(NSURLSessionAuthChallengePerformDefaultHandling.convert(), null)
                }
            }
        }
    }
}


