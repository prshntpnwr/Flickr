package com.example.myapp.util

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex

class AppController : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    companion object {
        @get:Synchronized
        lateinit var instance: AppController
    }

}