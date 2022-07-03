package com.rdstory.dc90button

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log

@SuppressLint("NewApi")
open class DCQSTileService : TileService() {
    companion object {
        private val TAG = DCQSTileService::class.java.simpleName
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = Runnable {
        updateTileStatus()
    }

    private fun updateTileStatus() {
        val tile = qsTile ?: return
        tile.state = getTileState()
        tile.updateTile()
        Log.i(TAG, "tile state: ${tile.state}")
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
        if (!SettingsHelper.getEnableDC()) {
            showDialog(OpenDCDialog(this))
            qsTile.state = Tile.STATE_INACTIVE
            qsTile.updateTile()
            return
        }
        toggleButton()
        qsTile.state = if (qsTile.state != Tile.STATE_ACTIVE) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
        if (!SettingsHelper.isAndroidSNoticed()) {
            showDialog(AndroidSNoticeDialog(this))
        }
    }

    open fun toggleButton() {
        SettingsHelper.setEnableDC90(qsTile.state != Tile.STATE_ACTIVE) {
            // check status again later
            MyApplication.updateQSTile()
        }
    }

    open fun getTileState(): Int {
        return if (SettingsHelper.isCurrentDC90State()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
    }
}