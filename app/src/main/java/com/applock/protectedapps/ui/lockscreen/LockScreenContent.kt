package com.applock.protectedapps.ui.lockscreen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.protectedapps.data.repository.LockType
import kotlinx.coroutines.launch

@Composable
fun LockScreenContent(
    packageName: String,
    appName: String,
    viewModel: LockScreenViewModel,
    onTriggerBiometric: () -> Unit,
    onSuccess: () -> Unit,
    onResetLockClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showForgotDialog by remember { mutableStateOf(false) }
    var recoveryAnswer by remember { mutableStateOf("") }
    var recoveryError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSuccess()
        }
    }

    LaunchedEffect(uiState.isBiometricEnabled) {
        if (uiState.isBiometricEnabled) {
            onTriggerBiometric()
        }
    }

    if (showForgotDialog && onResetLockClick != null) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("Reset PIN / Pattern") },
            text = {
                Column {
                    Text("Enter your recovery answer to reset your security lock:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = recoveryAnswer,
                        onValueChange = { recoveryAnswer = it; recoveryError = null },
                        label = { Text("Recovery Answer") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!recoveryError.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(recoveryError!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val isValid = viewModel.verifyRecoveryAnswer(recoveryAnswer)
                            if (isValid) {
                                showForgotDialog = false
                                onResetLockClick()
                            } else {
                                recoveryError = "Incorrect recovery answer"
                            }
                        }
                    }
                ) {
                    Text("Verify & Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "App Lock Icon",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = appName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Application Locked",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!uiState.errorMessage.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Lock View (PIN or Pattern)
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.lockType == LockType.PIN) {
                PinPadView(
                    inputBuffer = uiState.inputBuffer,
                    onDigitClick = { digit -> viewModel.onDigitEntered(digit) },
                    onDeleteClick = { viewModel.onDeleteDigit() },
                    onOkClick = { viewModel.submitCurrentInput() }
                )
            } else {
                PatternView(
                    onPatternComplete = { patternString -> viewModel.onPatternCompleted(patternString) },
                    hidePath = uiState.hidePatternPath
                )
            }
        }

        // Bottom Actions (Biometrics & Forgot PIN)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            if (onResetLockClick != null) {
                TextButton(onClick = { showForgotDialog = true }) {
                    Text("Forgot", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (uiState.isBiometricEnabled) {
                IconButton(
                    onClick = onTriggerBiometric,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Use Biometric Unlock",
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
