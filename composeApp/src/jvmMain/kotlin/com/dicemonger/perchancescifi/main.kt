package com.dicemonger.perchancescifi

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

val appModule = module {
    single { MainViewModel() }
}

fun main() {
    startKoin {
        modules(appModule)
    }

    application {
        Window(
            onCloseRequest = {
                stopKoin()
                exitApplication()
            },
            title = "PerchanceSciFiEntryApp",
        ) {
            App()
        }
    }
}