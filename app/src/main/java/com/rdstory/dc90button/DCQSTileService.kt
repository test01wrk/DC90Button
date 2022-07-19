package com.rdstory.dc90button

import android.annotation.TargetApi
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log

@TargetApi(Build.VERSION_CODES.Q)
class DCQSTileService : TileService() {
    companion object {
        private val TAG = DCQSTileService::class.java.simpleName
        private val URI_SET_REFRESH_RATE = Uri.parse("content://com.rdstory.miuiperfsaver.config_provider/refresh_rate")
        private val URI_SET_REFRESH_RATE_DEBUG = Uri.parse("content://com.rdstory.miuiperfsaver.config_provider.debug/refresh_rate")
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = Runnable {
        updateTileStatus()
    }

    private fun updateTileStatus(state: Int? = null) {
        val tile = qsTile ?: return
        val oldState = tile.state
        val oldLabel = tile.label
        tile.state = state
            ?: if (SettingsHelper.isDCButtonActive()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        val dcRefreshRate = SettingsHelper.getDCRefreshRate().takeIf { it > 0 }
            ?: SettingsHelper.getDCRefreshRateList().takeIf { it.size == 1 }?.get(0)
            ?: 0
        tile.label = if (dcRefreshRate > 0) "DC$dcRefreshRate" else getString(R.string.dc_button_label)
        tile.updateTile()
        if (oldState != tile.state || oldLabel != tile.label) {
            notifyFixedRefreshRate(if (tile.state == Tile.STATE_ACTIVE) dcRefreshRate else 0)
        }
        Log.i(TAG, "tile state: ${tile.state}, " +
                "dcSetting: ${SettingsHelper.isDCSettingEnabled()}, " +
                "userRR: ${SettingsHelper.getUserRefreshRate()}, " +
                "dcRR: ${SettingsHelper.getDCRefreshRate()}")
    }

    /**
     * if com.rdstory.miuiperfsaver installed, it should use our refresh rate
     */
    private fun notifyFixedRefreshRate(refreshRate: Int) {
        for (uri in listOf(URI_SET_REFRESH_RATE_DEBUG, URI_SET_REFRESH_RATE)) {
            try {
                val value = ContentValues(1).apply { put("refresh_rate", refreshRate) }
                contentResolver.update(uri, value, null, null)
            } catch (ignore: Exception) {
            }
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        mainHandler.postDelayed(updateRunnable, 500)
    }

    override fun onStopListening() {
        super.onStopListening()
        if (mainHandler.hasCallbacks(updateRunnable)) {
            mainHandler.removeCallbacks(updateRunnable)
            updateRunnable.run()
        }
    }

    override fun onClick() {
        super.onClick()
        mainHandler.removeCallbacks(updateRunnable)
        if (!SettingsHelper.isDCSettingEnabled()) {
            showDialog(OpenDCDialog(this))
            updateTileStatus(Tile.STATE_INACTIVE)
            return
        }
        val dcRefreshRateList = SettingsHelper.getDCRefreshRateList()
        if (dcRefreshRateList.isEmpty()) {
            showDialog(ButtonSettingDialog(this))
            updateTileStatus(Tile.STATE_INACTIVE)
            return
        }
        val dcRefreshRateIndex = dcRefreshRateList.indexOf(SettingsHelper.getDCRefreshRate())
        val dcRefreshRate = dcRefreshRateList.getOrNull(dcRefreshRateIndex + 1) ?: 0
        SettingsHelper.setDCButtonRefreshRate(dcRefreshRate) {
            // check status again later
            MyApplication.updateQSTile()
        }
        updateTileStatus(if (dcRefreshRate > 0) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE)
    }

    override fun onTileAdded() {
        super.onTileAdded()
        MyApplication.updateQSTile()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        SettingsHelper.reset()
        SettingsHelper.setDCButtonRefreshRate(0)
        SettingsHelper.setDCRefreshRateList(emptyList())
        SettingsHelper.setDCCDisableAutoBrightness(false)
    }
}