package com.rdstory.dc90button

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.database.ContentObserver
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.quicksettings.TileService
import android.util.Log
import android.view.Display

@SuppressLint("NewApi")
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
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S) {
            val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            displayManager.registerDisplayListener(object : DisplayManager.DisplayListener {
                override fun onDisplayAdded(displayId: Int) {}
                override fun onDisplayRemoved(displayId: Int) {}
                override fun onDisplayChanged(displayId: Int) {
                    val state = displayManager.getDisplay(displayId)?.state
                    if (state == Display.STATE_OFF) {
                        SettingsHelper.setPeakUserRefreshRate(60)
                    }
                }

            }, Handler(Looper.getMainLooper()))
        }
    }
}