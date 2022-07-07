package com.rdstory.dc90button

import android.annotation.TargetApi
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

class MyApplication : Application() {
    companion object {
        private val TAG = MyApplication::class.java.simpleName
        lateinit var application: MyApplication
            private set

        @TargetApi(Build.VERSION_CODES.Q)
        fun updateQSTile() {
            TileService.requestListeningState(
                application,
                ComponentName(application, DCQSTileService::class.java)
            )
        }
    }

    init {
        application = this
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "application created")
        SettingsHelper.checkRestoreDC()
        val observerHandler = Handler(Looper.getMainLooper())
        contentResolver.registerContentObserver(
            Settings.System.getUriFor("dc_back_light"),
            false,
            object : ContentObserver(observerHandler) {
                override fun onChange(selfChange: Boolean) {
                    updateQSTile()
                }
            })
        contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
            false,
            object : ContentObserver(observerHandler) {
                override fun onChange(selfChange: Boolean) {
                    SettingsHelper.notifyAutoBrightnessChanged()
                }
            })
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S) {
            val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            displayManager.registerDisplayListener(object : DisplayManager.DisplayListener {
                override fun onDisplayAdded(displayId: Int) {}
                override fun onDisplayRemoved(displayId: Int) {}
                override fun onDisplayChanged(displayId: Int) {
                    val state = displayManager.getDisplay(displayId)?.state
                    if (state == Display.STATE_OFF && SettingsHelper.isDCButtonEnabled()) {
                        Log.i(TAG, "screen off, set peak refresh rate to 60")
                        SettingsHelper.setPeakUserRefreshRate(60)
                    }
                }

            }, observerHandler)
        }
    }
}