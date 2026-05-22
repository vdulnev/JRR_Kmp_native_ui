package com.jrr.jrrkmp_native_ui

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform