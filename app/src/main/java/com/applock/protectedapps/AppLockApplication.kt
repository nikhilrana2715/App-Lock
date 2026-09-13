package com.applock.protectedapps

import android.app.Application
import android.content.Intent
import android.os.Build
import com.applock.protectedapps.service.AppLockMonitoringService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AppLockApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startMonitoringService()
    }

    private fun startMonitoringService() {
        try {
            val intent = Intent(this, AppLockMonitoringService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            // Handle edge case where background service start is restricted before unlock
            e.printStackTrace()
        }
    }
}
