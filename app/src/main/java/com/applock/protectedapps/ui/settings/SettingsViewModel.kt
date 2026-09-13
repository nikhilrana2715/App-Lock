package com.applock.protectedapps.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.applock.protectedapps.data.repository.LockType
import com.applock.protectedapps.data.repository.RelockPolicy
import com.applock.protectedapps.data.repository.SettingsRepository
import com.applock.protectedapps.domain.AuthenticationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val lockType: LockType = LockType.PIN,
    val isBiometricEnabled: Boolean = false,
    val isFastLockEnabled: Boolean = true,
    val relockPolicy: RelockPolicy = RelockPolicy.IMMEDIATELY,
    val hidePatternPath: Boolean = false,
    val vibrateOnPress: Boolean = true,
    val isRecoveryVerified: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authManager: AuthenticationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
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
            settingsRepository.isFastLockEnabledFlow.collect { enabled ->
                _uiState.value = _uiState.value.copy(isFastLockEnabled = enabled)
            }
        }
        viewModelScope.launch {
            settingsRepository.relockPolicyFlow.collect { policy ->
                _uiState.value = _uiState.value.copy(relockPolicy = policy)
            }
        }
        viewModelScope.launch {
            settingsRepository.hidePatternPathFlow.collect { hide ->
                _uiState.value = _uiState.value.copy(hidePatternPath = hide)
            }
        }
        viewModelScope.launch {
            settingsRepository.vibrateOnPressFlow.collect { vibrate ->
                _uiState.value = _uiState.value.copy(vibrateOnPress = vibrate)
            }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setBiometricEnabled(enabled) }
    }

    fun setFastLockEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setFastLockEnabled(enabled) }
    }

    fun setRelockPolicy(policy: RelockPolicy) {
        viewModelScope.launch { settingsRepository.setRelockPolicy(policy) }
    }

    fun setHidePatternPath(hide: Boolean) {
        viewModelScope.launch { settingsRepository.setHidePatternPath(hide) }
    }

    fun setVibrateOnPress(vibrate: Boolean) {
        viewModelScope.launch { settingsRepository.setVibrateOnPress(vibrate) }
    }

    suspend fun verifyRecoveryAnswer(answer: String): Boolean {
        val result = authManager.verifySecurityAnswer(answer)
        if (result) {
            _uiState.value = _uiState.value.copy(isRecoveryVerified = true)
        }
        return result
    }
}
