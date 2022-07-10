package com.rdstory.dc90button

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

@TargetApi(Build.VERSION_CODES.Q)
class ButtonSettingDialog(context: Context) : AlertDialog(context, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert) {
    init {
        val inflater = LayoutInflater.from(getContext())
        @SuppressLint("InflateParams")
        val contentView = inflater.inflate(R.layout.button_setting_dialog, null)
        val checkContainer = contentView.findViewById<GridView>(R.id.refresh_rate_check_container)
        val refreshRateList = SettingsHelper.supportedRefreshRateList.filter { it >= 60 }
        val selectRefreshRate = mutableSetOf<Int>()
        checkContainer.adapter = object : ArrayAdapter<Int>(getContext(), R.layout.button_setting_dialog_check_item, refreshRateList) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return super.getView(position, convertView, parent).also { view ->
                    (view as CheckBox).setOnCheckedChangeListener { _, isChecked ->
                        refreshRateList.getOrNull(position)?.let { r ->
                            if (isChecked) {
                                selectRefreshRate.add(r)
                            } else {
                                selectRefreshRate.remove(r)
                            }
                        }
                    }
                }
            }
        }
        val switch = contentView.findViewById<Switch>(R.id.disable_auto_brightness_switch)
        switch.setOnCheckedChangeListener { _, isChecked ->
            SettingsHelper.setDCCDisableAutoBrightness(isChecked)
        }

        setTitle(context.getString(R.string.button_settings_dialog_title))
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S) {
            setMessage(context.getString(R.string.button_settings_dialog_desc_s))
        } else {
            setMessage(context.getString(R.string.button_settings_dialog_desc))
        }
        setView(contentView)
        setButton(BUTTON_POSITIVE, context.getString(R.string.confirm)) { dialog, _ ->
            if (selectRefreshRate.isEmpty()) {
                return@setButton
            }
            SettingsHelper.setDCRefreshRateList(selectRefreshRate)
            MyApplication.updateQSTile()
            dialog.dismiss()
        }
        setButton(BUTTON_NEGATIVE, context.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
    }
}