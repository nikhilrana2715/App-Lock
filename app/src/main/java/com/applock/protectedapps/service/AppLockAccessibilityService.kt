package com.applock.protectedapps.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.applock.protectedapps.core.EventSource
import com.applock.protectedapps.core.ForegroundDetectionEngine
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppLockAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var detectionEngine: ForegroundDetectionEngine

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return
        if (packageName.isBlank()) return

        detectionEngine.onForegroundEvent(packageName, EventSource.ACCESSIBILITY)
    }

    override fun onInterrupt() {
        // Service interrupted
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }
}
