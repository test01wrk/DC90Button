package com.rdstory.dc90button

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.rdstory.dc90button.MyApplication.Companion.application as context

object SettingsHelper {
    private const val KEY_DC90_ENABLED = "dc90_enabled"

    private val mainHandler = Handler(Looper.getMainLooper())
    private val sp = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    
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

    fun isCurrentDC90State(): Boolean {
        return getEnableDC() && getUserRefreshRate() == 90
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
                }, 1000)
            }
        } else {
            setEnableDC(false)
            setUserRefreshRate(120) // TODO restore
            callback?.invoke()
        }
        sp.edit().putBoolean(KEY_DC90_ENABLED, enable).apply()
    }

    fun isDC90Enabled(): Boolean {
        return sp.getBoolean(KEY_DC90_ENABLED, false)
    }

    fun checkRestoreDC90() {
        if (isDC90Enabled() || isCurrentDC90State()) {
            mainHandler.postDelayed({
                setEnableDC90(true) { MyApplication.updateQSTile() }
            }, 1000)
        } else {
            MyApplication.updateQSTile()
        }
    }
}