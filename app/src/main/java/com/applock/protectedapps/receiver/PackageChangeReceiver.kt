package com.applock.protectedapps.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.applock.protectedapps.R
import com.applock.protectedapps.data.dao.LockedAppDao
import com.applock.protectedapps.data.entity.LockedAppEntity
import com.applock.protectedapps.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PackageChangeReceiver : BroadcastReceiver() {

    @Inject lateinit var lockedAppDao: LockedAppDao

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_PACKAGE_ADDED) {
            val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
            if (isReplacing) return

            val uri = intent.data ?: return
            val packageName = uri.schemeSpecificPart ?: return

            if (packageName == context.packageName) return

            // Handle direct lock action from notification
            if (intent.getBooleanExtra(EXTRA_ACTION_LOCK_DIRECT, false)) {
                val appName = getAppName(context, packageName)
                CoroutineScope(Dispatchers.IO).launch {
                    lockedAppDao.insertOrUpdate(LockedAppEntity(packageName, appName, true))
                }
                return
            }

            val appName = getAppName(context, packageName)
            showNewAppNotification(context, packageName, appName)
        }
    }

    private fun getAppName(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    private fun showNewAppNotification(context: Context, packageName: String, appName: String) {
        val channelId = "new_app_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "New App Installed Alert",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            context,
            packageName.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("New App Installed")
            .setContentText("Would you like to lock $appName?")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .build()

        notificationManager.notify(packageName.hashCode(), notification)
    }

    companion object {
        const val EXTRA_ACTION_LOCK_DIRECT = "extra_action_lock_direct"
    }
}
