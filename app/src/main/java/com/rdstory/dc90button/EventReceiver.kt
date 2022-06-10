package com.rdstory.dc90button

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log

class EventReceiver: BroadcastReceiver() {
    companion object {
        private val TAG = EventReceiver::class.java.simpleName
    }
    private val handler = Handler(Looper.getMainLooper())

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "onReceive: ${intent.action}")
        when (intent.action) {
            Intent.ACTION_USER_PRESENT -> {
                if (SettingsHelper.isDC90Enabled() || SettingsHelper.isCurrentDC90State()) {
                    handler.postDelayed({
                        SettingsHelper.setEnableDC90(true) {
                            MyApplication.updateQSTile()
                        }
                    }, 200)
                } else {
                    MyApplication.updateQSTile()
                }
            }
            else -> {
                MyApplication.updateQSTile()
            }
        }
    }
}