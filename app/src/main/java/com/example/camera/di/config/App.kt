package com.example.camera.di.config

import android.app.Application
import com.example.camera.di.module.Modules.ui
import com.example.camera.di.module.Modules.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@App)
            modules(listOf(viewModelModule, ui))
        }
    }
}