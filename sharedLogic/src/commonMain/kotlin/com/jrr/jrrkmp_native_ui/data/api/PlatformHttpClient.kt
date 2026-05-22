package com.jrr.jrrkmp_native_ui.data.api

import io.ktor.client.HttpClient

expect fun createPlatformHttpClient(): HttpClient
