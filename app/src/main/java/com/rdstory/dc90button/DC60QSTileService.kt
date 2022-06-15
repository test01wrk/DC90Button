package com.rdstory.dc90button

import android.annotation.SuppressLint
import android.service.quicksettings.Tile

@SuppressLint("NewApi")
class DC60QSTileService : DCQSTileService() {
    override fun toggleButton() {
        SettingsHelper.setEnableDC60(qsTile.state != Tile.STATE_ACTIVE)
    }

    override fun getTileState(): Int {
        return if (SettingsHelper.isCurrentDC60State()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
    }
}