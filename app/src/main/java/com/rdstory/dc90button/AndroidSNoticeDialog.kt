package com.rdstory.dc90button

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context

@SuppressLint("InlinedApi")
class AndroidSNoticeDialog(context: Context) : AlertDialog(context, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert) {
    init {
        setTitle(context.getString(R.string.android_s_notice_title))
        setMessage(context.getString(R.string.android_s_notice_content))
        setButton(BUTTON_POSITIVE, context.getString(R.string.got_it)) { dialog, _ ->
            dialog.dismiss()
        }
    }
}