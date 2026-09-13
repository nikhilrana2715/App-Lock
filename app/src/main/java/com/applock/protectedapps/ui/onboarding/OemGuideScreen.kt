package com.applock.protectedapps.ui.onboarding

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OemGuideScreen(
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val manufacturer = Build.MANUFACTURER.uppercase()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Background Protection Settings ($manufacturer)",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            Text(
                text = "Custom Android skins aggressive background killer policies may stop AppLock. Enable the settings below for uninterrupted protection.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            when {
                manufacturer.contains("XIAOMI") || manufacturer.contains("REDMI") || manufacturer.contains("POCO") -> {
                    OemStep(title = "1. Autostart Permission", desc = "Turn ON Autostart for AppLock.")
                    OemStep(title = "2. Display Pop-up Windows", desc = "Enable 'Display pop-up windows while running in background'.")
                    OemStep(title = "3. Battery Saver", desc = "Set Battery Saver to 'No restrictions'.")
                }
                manufacturer.contains("SAMSUNG") -> {
                    OemStep(title = "1. Auto-start App Management", desc = "Ensure AppLock is not in 'Sleeping apps' or 'Deep sleeping apps'.")
                    OemStep(title = "2. Unrestricted Battery", desc = "Set App Battery usage to 'Unrestricted'.")
                }
                manufacturer.contains("OPPO") || manufacturer.contains("REALME") -> {
                    OemStep(title = "1. Auto-launch", desc = "Enable 'Auto-launch' and 'Allow background activity'.")
                    OemStep(title = "2. Display Over Other Apps", desc = "Ensure floating window permission is ON.")
                }
                manufacturer.contains("VIVO") -> {
                    OemStep(title = "1. Autostart", desc = "Turn ON Autostart in iManager / Settings.")
                    OemStep(title = "2. High Power Consumption", desc = "Enable 'High background power consumption'.")
                }
                else -> {
                    OemStep(title = "Unrestricted Battery Mode", desc = "Set battery optimization to 'Unrestricted' or 'No restrictions'.")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { openOemAutostartSettings(context) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Open Device Settings")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onContinueClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Done & Start AppLock", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OemStep(title: String, desc: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = desc, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun openOemAutostartSettings(context: Context) {
    val manufacturer = Build.MANUFACTURER.lowercase()
    val intents = mutableListOf<Intent>()

    when {
        manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") -> {
            intents.add(Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")))
        }
        manufacturer.contains("oppo") -> {
            intents.add(Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")))
        }
        manufacturer.contains("realme") -> {
            intents.add(Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")))
        }
        manufacturer.contains("vivo") -> {
            intents.add(Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")))
        }
    }

    // Fallback to App Info Details
    intents.add(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:${context.packageName}")
    })

    for (intent in intents) {
        try {
            context.startActivity(intent)
            return
        } catch (e: Exception) {
            // Try next fallback intent
        }
    }
}
