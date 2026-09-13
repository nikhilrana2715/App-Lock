package com.applock.protectedapps.core

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class EventSource {
    ACCESSIBILITY,
    USAGE_STATS
}

@Singleton
class ForegroundDetectionEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stateMachine: AppLockStateMachine
) {
    @Volatile private var lastPackage: String = ""
    @Volatile private var lastEventTime: Long = 0L

    fun onForegroundEvent(packageName: String, source: EventSource) {
        val now = System.currentTimeMillis()
        
        // Deduplicate events arriving within 100ms for the same package
        if (packageName == lastPackage && (now - lastEventTime) < 100) {
            return
        }

        lastPackage = packageName
        lastEventTime = now

        stateMachine.onForegroundPackageChanged(context, packageName)
    }
}
