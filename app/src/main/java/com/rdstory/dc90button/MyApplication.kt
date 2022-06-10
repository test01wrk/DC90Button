package com.rdstory.dc90button

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.service.quicksettings.TileService
import android.util.Log

class MyApplication : Application() {
    companion object {
        private val TAG = MyApplication::class.java.simpleName
        lateinit var application: MyApplication
            private set

        @SuppressLint("NewApi")
        fun updateQSTile() {
            TileService.requestListeningState(application, ComponentName(application, DCQSTileService::class.java))
        }
    }

    init {
        application = this
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "application created")
        updateQSTile()
    }
}