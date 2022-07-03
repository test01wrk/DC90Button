package com.rdstory.dc90button

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.database.ContentObserver
import android.provider.Settings
import android.service.quicksettings.TileService
import android.util.Log

class MyApplication : Application() {
    companion object {
        private val TAG = MyApplication::class.java.simpleName
        lateinit var application: MyApplication
            private set

        @SuppressLint("NewApi")
        fun updateQSTile() {
            TileService.requestListeningState(
                application,
                ComponentName(application, DCQSTileService::class.java)
            )
            TileService.requestListeningState(
                application,
                ComponentName(application, DC60QSTileService::class.java)
            )
        }
    }

    init {
        application = this
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "application created")
        SettingsHelper.checkRestoreDC90()
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                updateQSTile()
            }
        }
        contentResolver.registerContentObserver(
            Settings.System.getUriFor("dc_back_light"), false, observer)
    }
}