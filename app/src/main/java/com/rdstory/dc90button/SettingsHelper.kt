package com.rdstory.dc90button

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.rdstory.dc90button.MyApplication.Companion.application as context

object SettingsHelper {
    private const val KEY_DC90_ENABLED = "dc90_enabled"
    private const val KEY_DC60_ENABLED = "dc60_enabled"
    private const val KEY_ANDROID_S_NOTICED = "android_s_noticed"

    private val mainHandler = Handler(Looper.getMainLooper())
    private val toggleHandler = Handler(Looper.getMainLooper())
    private val sp = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    
    fun setUserRefreshRate(refreshRate: Int) {
        Settings.System.putInt(context.contentResolver, "user_refresh_rate", refreshRate)
        setPeakUserRefreshRate(refreshRate)
    }

    fun getUserRefreshRate(): Int {
        return Settings.System.getInt(context.contentResolver, "user_refresh_rate", 60)
    }

    fun setPeakUserRefreshRate(refreshRate: Int) {
        Settings.System.putInt(context.contentResolver, "peak_refresh_rate", refreshRate)
    }

    fun getEnableDC(): Boolean {
        return Settings.System.getInt(context.contentResolver, "dc_back_light", 0) == 1
    }

    fun isCurrentDC90State(): Boolean {
        return getEnableDC() && getUserRefreshRate() == 90
    }

    fun isCurrentDC60State(): Boolean {
        return getEnableDC() && getUserRefreshRate() == 60
    }

    fun isDCIncompatible(): Boolean {
        return FeatureParser.getBoolean("dc_backlight_fps_incompatible", false)
    }

    fun setEnableDC90(enable: Boolean, callback: (() -> Unit)? = null) {
        toggleHandler.removeCallbacksAndMessages(null)
        if (enable) {
            val refreshRate = getUserRefreshRate()
            if (refreshRate == 60) {
                setUserRefreshRate(90)
                callback?.invoke()
            } else {
                setUserRefreshRate(60)
                toggleHandler.postDelayed({
                    setUserRefreshRate(90)
                    callback?.invoke()
                }, 500)
            }
        } else {
            setUserRefreshRate(120) // TODO restore
            callback?.invoke()
        }
        sp.edit().putBoolean(KEY_DC90_ENABLED, enable).apply()
    }

    fun isDC90Enabled(): Boolean {
        return sp.getBoolean(KEY_DC90_ENABLED, false)
    }

    fun isDC60Enabled(): Boolean {
        return sp.getBoolean(KEY_DC60_ENABLED, false)
    }

    fun setEnableDC60(enable: Boolean, callback: (() -> Unit)? = null) {
        setUserRefreshRate(if (enable) 60 else 120)
        callback?.invoke()
        sp.edit().putBoolean(KEY_DC60_ENABLED, enable).apply()
    }

    fun checkRestoreDC90() {
        MyApplication.updateQSTile()
        if (isDC90Enabled() || isCurrentDC90State()) {
            mainHandler.postDelayed({
                setEnableDC90(true) { MyApplication.updateQSTile() }
            }, 500)
        }
        if (isDC60Enabled() || isCurrentDC60State()) {
            mainHandler.postDelayed({
                setEnableDC60(true) { MyApplication.updateQSTile() }
            }, 500)
        }
    }

    fun shouldRestore(): Boolean {
        return isDC90Enabled() || isCurrentDC90State() || isDC60Enabled() || isCurrentDC60State()
    }

    fun isAndroidSNoticed(): Boolean {
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.S) {
            return true
        }
        val noticed = sp.getBoolean(KEY_ANDROID_S_NOTICED, false)
        if (!noticed) {
            sp.edit().putBoolean(KEY_ANDROID_S_NOTICED, true).apply()
        }
        return noticed
    }
}