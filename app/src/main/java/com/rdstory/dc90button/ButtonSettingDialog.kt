package com.rdstory.dc90button

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Switch

@TargetApi(Build.VERSION_CODES.Q)
class ButtonSettingDialog(context: Context) : AlertDialog(context, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert) {
    init {
        @SuppressLint("InflateParams")
        val contentView = LayoutInflater.from(getContext()).inflate(R.layout.button_setting_dialog, null)
        val spinnerView = contentView.findViewById<Spinner>(R.id.refresh_rate_spinner)
        val refreshRateList = SettingsHelper.supportedRefreshRateList.filter { it >= 60 }.reversed()
        spinnerView.adapter = ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, refreshRateList).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        var setRefreshRate = refreshRateList[0]
        spinnerView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                refreshRateList.getOrNull(position)?.let { setRefreshRate = it }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinnerView.setSelection(0)
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
            SettingsHelper.setDCRefreshRate(setRefreshRate)
            MyApplication.updateQSTile()
            dialog.dismiss()
        }
        setButton(BUTTON_NEGATIVE, context.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
    }
}