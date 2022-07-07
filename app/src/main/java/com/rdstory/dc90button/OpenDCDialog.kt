package com.rdstory.dc90button

import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build

@TargetApi(Build.VERSION_CODES.Q)
class OpenDCDialog(context: Context) : AlertDialog(context, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert) {
    init {
        setTitle(context.getString(R.string.dc_not_enable))
        setMessage(context.getString(R.string.open_dc_desc))
        setButton(BUTTON_POSITIVE, context.getString(R.string.goto_settings)) { _, _ ->
            context.startActivity(Intent("miui.intent.action.ANTI_FLICKER").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
        setButton(BUTTON_NEGATIVE, context.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
    }
}