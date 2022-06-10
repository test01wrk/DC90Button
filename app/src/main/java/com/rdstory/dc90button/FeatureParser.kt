package com.rdstory.dc90button

import android.util.Log
import java.lang.reflect.Method

object FeatureParser {
    private val TAG = FeatureParser::class.java.simpleName
    private var getBooleanMethod: Method? = null
    init {
        try {
            val clazz = Class.forName("miui.util.FeatureParser")
            getBooleanMethod = clazz.getDeclaredMethod(
                "getBoolean", String::class.java, Boolean::class.java
            ).apply { isAccessible = true }
        } catch (e: Throwable) {
            Log.e(TAG, "failed to init FeatureParser", e)
        }
    }

    fun getBoolean(key: String, fallback: Boolean): Boolean {
        return try {
            getBooleanMethod?.invoke(null, key, fallback) as Boolean
        } catch (e: Throwable) {
            fallback
        }
    }
}