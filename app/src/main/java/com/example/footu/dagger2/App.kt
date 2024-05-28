package com.example.footu.dagger2

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.footu.LocationEmitter
import com.example.footu.utils.AppKey
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate() {
        super.onCreate()
        app = this
        AppKey.generateKeyPair()
        LocationEmitter.context = this
    }

    companion object {
        lateinit var app: App

        fun getInstance(): App {
            return app
        }
    }
}
