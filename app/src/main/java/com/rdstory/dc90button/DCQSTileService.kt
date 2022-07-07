package com.rdstory.dc90button

import android.annotation.TargetApi
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
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = Runnable {
        updateTileStatus()
    }

    private fun updateTileStatus() {
        val tile = qsTile ?: return
        tile.state = if (SettingsHelper.isDCButtonState()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        val dcRefreshRate = SettingsHelper.getDCRefreshRate()
        tile.label = if (dcRefreshRate > 0) "DC$dcRefreshRate" else getString(R.string.dc_button_label)
        tile.updateTile()
        Log.i(TAG, "tile state: ${tile.state}, " +
                "dcSetting: ${SettingsHelper.isDCSettingEnabled()}, " +
                "userRR: ${SettingsHelper.getUserRefreshRate()}, " +
                "dcRR: ${SettingsHelper.getDCRefreshRate()}")
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
            qsTile.state = Tile.STATE_INACTIVE
            qsTile.updateTile()
            return
        }
        val dcRefreshRate = SettingsHelper.getDCRefreshRate()
        if (dcRefreshRate <= 0) {
            showDialog(ButtonSettingDialog(this))
            qsTile.state = Tile.STATE_INACTIVE
            qsTile.updateTile()
            return
        }
        val toEnable = qsTile.state != Tile.STATE_ACTIVE
        SettingsHelper.setDCButtonEnable(toEnable) {
            // check status again later
            MyApplication.updateQSTile()
        }
        qsTile.state = if (toEnable) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        MyApplication.updateQSTile()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        SettingsHelper.reset()
        SettingsHelper.setDCButtonEnable(false)
        SettingsHelper.setDCRefreshRate(0)
        SettingsHelper.setDCCDisableAutoBrightness(false)
    }
}