package com.jrr.jrrkmp_native_ui.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jrr.jrrkmp_native_ui.tv.ui.TvRootScreen
import com.jrr.jrrkmp_native_ui.tv.ui.theme.JrrTvTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as TvApplication).container
        setContent {
            JrrTvTheme {
                TvRootScreen(container = container)
            }
        }
    }
}
