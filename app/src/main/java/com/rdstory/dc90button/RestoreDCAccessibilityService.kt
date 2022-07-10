package com.rdstory.dc90button

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class RestoreDCAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}
}