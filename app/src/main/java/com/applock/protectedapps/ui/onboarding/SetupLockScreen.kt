package com.applock.protectedapps.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.protectedapps.data.repository.LockType
import com.applock.protectedapps.domain.AuthenticationManager
import com.applock.protectedapps.ui.lockscreen.PatternView
import com.applock.protectedapps.ui.lockscreen.PinPadView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupLockScreen(
    authManager: AuthenticationManager,
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    var selectedType by remember { mutableStateOf(LockType.PIN) }
    var step by remember { mutableStateOf(1) } // 1: Enter, 2: Confirm, 3: Security Question
    var firstEntry by remember { mutableStateOf("") }
    var inputBuffer by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var selectedQuestionIndex by remember { mutableStateOf(0) }
    var securityAnswerInput by remember { mutableStateOf("") }
    var enableFingerprint by remember { mutableStateOf(true) }

    val questions = listOf(
        "What is your first pet's name?",
        "What city were you born in?",
        "What is your mother's maiden name?"
    )

    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(
                text = when (step) {
                    1 -> "Choose Your Lock Credentials"
                    2 -> "Confirm Security Credential"
                    else -> "Set Recovery Question"
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (step == 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FilterChip(
                        selected = selectedType == LockType.PIN,
                        onClick = { selectedType = LockType.PIN; inputBuffer = "" },
                        label = { Text("4-Digit PIN") }
                    )
                    FilterChip(
                        selected = selectedType == LockType.PATTERN,
                        onClick = { selectedType = LockType.PATTERN; inputBuffer = "" },
                        label = { Text("3x3 Pattern") }
                    )
                }
            }

            if (!errorMessage.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (step == 3) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "If you forget your PIN or Pattern, this question allows you to reset it safely.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = questions[selectedQuestionIndex], fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = securityAnswerInput,
                        onValueChange = {
                            securityAnswerInput = it
                            errorMessage = null
                        },
                        label = { Text("Your Answer") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Fingerprint Unlock", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Use enrolled device biometrics for instant unlock", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = enableFingerprint,
                                onCheckedChange = { enableFingerprint = it }
                            )
                        }
                    }
                }
            } else {
                if (selectedType == LockType.PIN) {
                    PinPadView(
                        inputBuffer = inputBuffer,
                        onDigitClick = { digit ->
                            if (inputBuffer.length < 4) {
                                val nextBuffer = inputBuffer + digit
                                inputBuffer = nextBuffer
                                if (nextBuffer.length == 4) {
                                    handleCredentialInput(
                                        input = nextBuffer,
                                        currentStep = step,
                                        firstInput = firstEntry,
                                        onAdvanceStep2 = { firstEntry = nextBuffer; inputBuffer = ""; step = 2; errorMessage = null },
                                        onMismatch = { errorMessage = "PINs do not match. Try again."; inputBuffer = ""; step = 1 },
                                        onSuccessStep2 = {
                                            scope.launch { authManager.saveNewCredentials(selectedType, firstEntry) }
                                            step = 3
                                            errorMessage = null
                                        }
                                    )
                                }
                            }
                        },
                        onDeleteClick = {
                            if (inputBuffer.isNotEmpty()) inputBuffer = inputBuffer.dropLast(1)
                        },
                        onOkClick = {
                            if (inputBuffer.length < 4) {
                                errorMessage = "Please enter 4-digit PIN"
                            } else {
                                handleCredentialInput(
                                    input = inputBuffer,
                                    currentStep = step,
                                    firstInput = firstEntry,
                                    onAdvanceStep2 = { firstEntry = inputBuffer; inputBuffer = ""; step = 2; errorMessage = null },
                                    onMismatch = { errorMessage = "PINs do not match. Try again."; inputBuffer = ""; step = 1 },
                                    onSuccessStep2 = {
                                        scope.launch { authManager.saveNewCredentials(selectedType, firstEntry) }
                                        step = 3
                                        errorMessage = null
                                    }
                                )
                            }
                        }
                    )
                } else {
                    PatternView(
                        onPatternComplete = { patternString ->
                            handleCredentialInput(
                                input = patternString,
                                currentStep = step,
                                firstInput = firstEntry,
                                onAdvanceStep2 = { firstEntry = patternString; inputBuffer = ""; step = 2; errorMessage = null },
                                onMismatch = { errorMessage = "Patterns do not match. Try again."; inputBuffer = ""; step = 1 },
                                onSuccessStep2 = {
                                    scope.launch { authManager.saveNewCredentials(selectedType, firstEntry) }
                                    step = 3
                                    errorMessage = null
                                }
                            )
                        }
                    )
                }
            }
        }

        if (step == 3) {
            Button(
                enabled = !isSubmitting,
                onClick = {
                    if (securityAnswerInput.isBlank()) {
                        errorMessage = "Please enter a recovery answer"
                    } else {
                        isSubmitting = true
                        errorMessage = null
                        scope.launch {
                            try {
                                authManager.saveNewCredentials(selectedType, firstEntry)
                                authManager.saveSecurityRecovery(selectedQuestionIndex, securityAnswerInput)
                                authManager.setBiometricEnabled(enableFingerprint)
                                onSetupComplete()
                            } catch (e: Exception) {
                                errorMessage = "Error: ${e.localizedMessage}"
                                isSubmitting = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "Complete Setup", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}

private fun handleCredentialInput(
    input: String,
    currentStep: Int,
    firstInput: String,
    onAdvanceStep2: () -> Unit,
    onMismatch: () -> Unit,
    onSuccessStep2: () -> Unit
) {
    if (currentStep == 1) {
        onAdvanceStep2()
    } else {
        if (input == firstInput) {
            onSuccessStep2()
        } else {
            onMismatch()
        }
    }
}
