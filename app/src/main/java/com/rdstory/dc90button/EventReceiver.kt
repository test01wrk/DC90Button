package com.rdstory.dc90button

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class EventReceiver: BroadcastReceiver() {
    companion object {
        private val TAG = EventReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "onReceive: ${intent.action}")
        when (intent.action) {
            Intent.ACTION_USER_PRESENT -> SettingsHelper.checkRestoreDC()
            else -> MyApplication.updateQSTile()
        }
    }
}