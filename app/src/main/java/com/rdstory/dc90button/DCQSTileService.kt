package com.rdstory.dc90button

import android.annotation.SuppressLint
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log

@SuppressLint("NewApi")
class DCQSTileService : TileService() {
    companion object {
        private val TAG = DCQSTileService::class.java.simpleName
    }

    private fun updateTileStatus() {
        val tile = qsTile ?: return
        tile.state = if (SettingsHelper.isCurrentDC90State()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
        Log.i(TAG, "tile state: ${tile.state}")
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileStatus()
    }

    override fun onClick() {
        super.onClick()
        SettingsHelper.setEnableDC90(qsTile.state != Tile.STATE_ACTIVE) {
            // check status again later
            MyApplication.updateQSTile()
        }
        qsTile.state = if (qsTile.state != Tile.STATE_ACTIVE) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }
}