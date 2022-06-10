package com.rdstory.dc90button

import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.rdstory.dc90button.MyApplication.Companion.application as context

object SettingsHelper {
    private val mainHandler = Handler(Looper.getMainLooper())
    
    fun setUserRefreshRate(refreshRate: Int) {
        Settings.System.putInt(context.contentResolver, "user_refresh_rate", refreshRate)
        Settings.System.putInt(context.contentResolver, "peak_refresh_rate", refreshRate)
    }

    fun setEnableDC(enable: Boolean) {
        Settings.System.putInt(context.contentResolver, "dc_back_light", if (enable) 1 else 0)
    }

    fun getUserRefreshRate(): Int {
        return Settings.System.getInt(context.contentResolver, "user_refresh_rate", 60)
    }

    fun getEnableDC(): Boolean {
        return Settings.System.getInt(context.contentResolver, "dc_back_light", 0) == 1
    }

    fun isDCIncompatible(): Boolean {
        return FeatureParser.getBoolean("dc_backlight_fps_incompatible", false)
    }

    fun setEnableDC90(enable: Boolean, callback: (() -> Unit)? = null) {
        if (enable) {
            val refreshRate = getUserRefreshRate()
            if (refreshRate == 60) {
                setEnableDC(true)
                setUserRefreshRate(90)
                callback?.invoke()
            } else {
                setUserRefreshRate(60)
                mainHandler.postDelayed({
                    setEnableDC(true)
                    setUserRefreshRate(90)
                    callback?.invoke()
                }, 100)
            }
        } else {
            setEnableDC(false)
            setUserRefreshRate(120) // TODO restore
            callback?.invoke()
        }
    }
}