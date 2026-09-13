package com.applock.protectedapps.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.protectedapps.data.repository.RelockPolicy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onChangeCredentialsClick: () -> Unit,
    onHealthCheckClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPrivacyDialog by remember { mutableStateOf(false) }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy & App Info", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "AppLock provides complete, real-time privacy and security for your smartphone. It protects your installed applications, system apps, private files, gallery, messaging, financial tools, and social media with biometric and passcode locks.",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "• Real-time App Protection (PIN / Pattern / Fingerprint)\n• Anti-Tamper Security & Fast Lock\n• 100% On-Device Data Storage (Zero Cloud Data Collection)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Develped By :- Nikhil",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SectionHeader(title = "Security & Locking")

            SettingsRow(
                title = "Change PIN / Pattern",
                subtitle = "Current mode: ${uiState.lockType.name}",
                onClick = onChangeCredentialsClick
            )

            SettingsSwitchRow(
                title = "Fingerprint Lock",
                subtitle = "Use enrolled device biometrics to unlock",
                checked = uiState.isBiometricEnabled,
                onCheckedChange = { viewModel.setBiometricEnabled(it) }
            )

            SettingsRow(
                title = "Relock Policy",
                subtitle = if (uiState.relockPolicy == RelockPolicy.IMMEDIATELY) "Immediately after leaving app" else "After screen off",
                onClick = {
                    val nextPolicy = if (uiState.relockPolicy == RelockPolicy.IMMEDIATELY) RelockPolicy.AFTER_SCREEN_OFF else RelockPolicy.IMMEDIATELY
                    viewModel.setRelockPolicy(nextPolicy)
                }
            )

            SettingsSwitchRow(
                title = "Hide Pattern Path",
                subtitle = "Do not show line path when drawing pattern",
                checked = uiState.hidePatternPath,
                onCheckedChange = { viewModel.setHidePatternPath(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SectionHeader(title = "Performance & Permissions")

            SettingsSwitchRow(
                title = "Fast Lock Mode (Accessibility)",
                subtitle = "Instant foreground app detection",
                checked = uiState.isFastLockEnabled,
                onCheckedChange = { viewModel.setFastLockEnabled(it) }
            )

            SettingsRow(
                title = "Permission Health Check",
                subtitle = "Verify live system permission status",
                onClick = onHealthCheckClick
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SectionHeader(title = "About AppLock")

            SettingsRow(
                title = "Version NKJ.15.18",
                subtitle = "Tap to view Privacy Policy & Developer Info",
                onClick = { showPrivacyDialog = true }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
