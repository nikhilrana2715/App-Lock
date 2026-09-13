package com.applock.protectedapps.ui.lockscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.applock.protectedapps.data.repository.LockType
import com.applock.protectedapps.data.repository.SettingsRepository
import com.applock.protectedapps.domain.AuthResult
import com.applock.protectedapps.domain.AuthenticationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LockScreenUiState(
    val lockType: LockType = LockType.PIN,
    val isBiometricEnabled: Boolean = false,
    val hidePatternPath: Boolean = false,
    val inputBuffer: String = "",
    val errorMessage: String? = null,
    val lockoutSeconds: Int = 0,
    val isSuccess: Boolean = false,
    val isSubmitting: Boolean = false
)

@HiltViewModel
class LockScreenViewModel @Inject constructor(
    private val authManager: AuthenticationManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LockScreenUiState())
    val uiState: StateFlow<LockScreenUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        checkInitialLockout()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.lockTypeFlow.collect { type ->
                _uiState.value = _uiState.value.copy(lockType = type)
            }
        }
        viewModelScope.launch {
            settingsRepository.isBiometricEnabledFlow.collect { enabled ->
                _uiState.value = _uiState.value.copy(isBiometricEnabled = enabled)
            }
        }
        viewModelScope.launch {
            settingsRepository.hidePatternPathFlow.collect { hide ->
                _uiState.value = _uiState.value.copy(hidePatternPath = hide)
            }
        }
    }

    private fun checkInitialLockout() {
        viewModelScope.launch {
            val lockout = authManager.checkLockoutState()
            if (lockout is AuthResult.Lockout) {
                startLockoutCountdown(lockout.remainingSeconds)
            }
        }
    }

    fun onDigitEntered(digit: String) {
        if (_uiState.value.lockoutSeconds > 0 || _uiState.value.isSubmitting) return
        if (_uiState.value.inputBuffer.length >= 4) return
        val newBuffer = _uiState.value.inputBuffer + digit
        _uiState.value = _uiState.value.copy(inputBuffer = newBuffer, errorMessage = null)
        if (newBuffer.length == 4) {
            submitInput(newBuffer)
        }
    }

    fun onDeleteDigit() {
        if (_uiState.value.isSubmitting) return
        if (_uiState.value.inputBuffer.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                inputBuffer = _uiState.value.inputBuffer.dropLast(1),
                errorMessage = null
            )
        }
    }

    fun onPatternCompleted(patternString: String) {
        if (_uiState.value.lockoutSeconds > 0 || _uiState.value.isSubmitting) return
        _uiState.value = _uiState.value.copy(inputBuffer = patternString)
        submitInput(patternString)
    }

    private fun submitInput(input: String) {
        if (_uiState.value.isSubmitting) return
        _uiState.value = _uiState.value.copy(isSubmitting = true)
        viewModelScope.launch {
            when (val result = authManager.verifyInput(input)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSuccess = true, isSubmitting = false, errorMessage = null)
                }
                is AuthResult.Failure -> {
                    _uiState.value = _uiState.value.copy(inputBuffer = "", isSubmitting = false)
                    if (result.isLockedOut) {
                        startLockoutCountdown(result.lockoutSeconds)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Incorrect PIN/Pattern. ${result.remainingAttempts} attempts left."
                        )
                    }
                }
                is AuthResult.Lockout -> {
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    startLockoutCountdown(result.remainingSeconds)
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(inputBuffer = "", isSubmitting = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun startLockoutCountdown(seconds: Int) {
        viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                _uiState.value = _uiState.value.copy(
                    lockoutSeconds = remaining,
                    errorMessage = "Too many failed attempts. Try again in 0:${String.format("%02d", remaining)}"
                )
                delay(1000)
                remaining--
            }
            _uiState.value = _uiState.value.copy(lockoutSeconds = 0, errorMessage = null, inputBuffer = "")
        }
    }

    suspend fun verifyRecoveryAnswer(answer: String): Boolean {
        return authManager.verifySecurityAnswer(answer)
    }

    fun submitCurrentInput() {
        if (_uiState.value.lockoutSeconds > 0 || _uiState.value.isSubmitting) return
        val currentBuffer = _uiState.value.inputBuffer
        if (currentBuffer.length < 4) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter 4-digit PIN")
            return
        }
        submitInput(currentBuffer)
    }
}
