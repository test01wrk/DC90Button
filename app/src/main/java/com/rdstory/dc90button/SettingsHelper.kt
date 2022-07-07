package com.rdstory.dc90button

import android.annotation.TargetApi
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import com.rdstory.dc90button.MyApplication.Companion.application as context

@TargetApi(Build.VERSION_CODES.Q)
object SettingsHelper {
    private const val KEY_DC_BUTTON_ENABLED = "dc_button_enabled"
    private const val KEY_DC_REFRESH_RATE = "dc_refresh_rate"
    private const val KEY_DC_DISABLE_AUTO_BRIGHTNESS = "dc_disable_auto_brightness"

    private val mainHandler = Handler(Looper.getMainLooper())
    private val toggleHandler = Handler(Looper.getMainLooper())
    private val sp = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val supportedRefreshRateList =
        (context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager).getDisplay(0)
            .supportedModes?.mapTo(mutableSetOf()) { it.refreshRate.toInt() }?.sorted()?.reversed()
            ?: listOf(60)
    private var autoBrightnessMode = -1
    private var autoBrightnessModeTime = 0L

    private fun setUserRefreshRate(refreshRate: Int) {
        Settings.System.putInt(context.contentResolver, "user_refresh_rate", refreshRate)
        setPeakUserRefreshRate(refreshRate)
    }

    fun getUserRefreshRate(): Int {
        return Settings.System.getInt(context.contentResolver, "user_refresh_rate", 60)
    }

    fun setPeakUserRefreshRate(refreshRate: Int) {
        Settings.System.putInt(context.contentResolver, "peak_refresh_rate", refreshRate)
    }

    fun isDCSettingEnabled(): Boolean {
        return Settings.System.getInt(context.contentResolver, "dc_back_light", 0) == 1
    }

    fun isDCButtonState(): Boolean {
        return isDCButtonEnabled() && isDCSettingEnabled() && getUserRefreshRate() == getDCRefreshRate()
    }

    private fun setAutoBrightnessMode(mode: Int) {
        Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, mode)
    }

    private fun getAutoBrightnessMode(): Int {
        return Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, -1)
    }

    fun isDCIncompatible(): Boolean {
        return FeatureParser.getBoolean("dc_backlight_fps_incompatible", false)
    }

    fun setDCButtonEnable(enable: Boolean, callback: (() -> Unit)? = null) {
        toggleHandler.removeCallbacksAndMessages(null)
        val dcRefreshRate = getDCRefreshRate()
        if (enable && dcRefreshRate > 0) {
            autoBrightnessMode = getAutoBrightnessMode()
            autoBrightnessModeTime = SystemClock.uptimeMillis()
            val currentRefreshRate = getUserRefreshRate()
            if (currentRefreshRate == 60) {
                setUserRefreshRate(dcRefreshRate)
                callback?.invoke()
            } else {
                setUserRefreshRate(60)
                toggleHandler.postDelayed({
                    setUserRefreshRate(dcRefreshRate)
                    callback?.invoke()
                }, 500)
            }
            if (isDCDisableAutoBrightness()) {
                setAutoBrightnessMode(Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
            }
        } else {
            setUserRefreshRate(supportedRefreshRateList[0]) // TODO restore
            callback?.invoke()
            if (autoBrightnessMode >= 0 && isDCDisableAutoBrightness()) {
                setAutoBrightnessMode(autoBrightnessMode)
                autoBrightnessMode = -1
            }
        }
        sp.edit().putBoolean(KEY_DC_BUTTON_ENABLED, enable).apply()
    }

    fun isDCButtonEnabled(): Boolean {
        return sp.getBoolean(KEY_DC_BUTTON_ENABLED, false)
    }

    fun getDCRefreshRate(): Int {
        return sp.getInt(KEY_DC_REFRESH_RATE, 0)
    }

    fun setDCRefreshRate(refreshRate: Int) {
        sp.edit().putInt(KEY_DC_REFRESH_RATE, refreshRate).apply()
    }

    private fun isDCDisableAutoBrightness(): Boolean {
        return sp.getBoolean(KEY_DC_DISABLE_AUTO_BRIGHTNESS, false)
    }

    fun setDCCDisableAutoBrightness(disableAutoBrightness: Boolean) {
        sp.edit().putBoolean(KEY_DC_DISABLE_AUTO_BRIGHTNESS, disableAutoBrightness).apply()
    }

    fun notifyAutoBrightnessChanged() {
        if (SystemClock.uptimeMillis() - autoBrightnessModeTime > 15000) {
            autoBrightnessMode = -1
        }
    }

    fun checkRestoreDC() {
        MyApplication.updateQSTile()
        if (isDCButtonEnabled()) {
            mainHandler.postDelayed({
                setDCButtonEnable(true) { MyApplication.updateQSTile() }
            }, 500)
        }
    }

    fun reset() {
        toggleHandler.removeCallbacksAndMessages(null)
        mainHandler.removeCallbacksAndMessages(null)
    }
}