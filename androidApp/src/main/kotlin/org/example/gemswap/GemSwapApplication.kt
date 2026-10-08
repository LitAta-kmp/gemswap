package org.example.gemswap

import android.app.Application
import org.example.gemswap.di.androidModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.example.gemswap.di.appModule

class GemSwapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GemSwapApplication)
            modules(appModule, androidModule)
        }
    }
}