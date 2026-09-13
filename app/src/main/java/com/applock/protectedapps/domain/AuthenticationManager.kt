package com.applock.protectedapps.domain

import com.applock.protectedapps.data.repository.LockType
import com.applock.protectedapps.data.repository.SettingsRepository
import com.applock.protectedapps.data.security.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    object Success : AuthResult()
    data class Failure(val remainingAttempts: Int, val isLockedOut: Boolean, val lockoutSeconds: Int) : AuthResult()
    data class Lockout(val remainingSeconds: Int) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

@Singleton
class AuthenticationManager @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend fun checkLockoutState(): AuthResult? = withContext(Dispatchers.IO) {
        val (failedCount, lockoutRemainingMs) = settingsRepository.getFailedAttemptData()
        if (lockoutRemainingMs > 0) {
            val seconds = (lockoutRemainingMs / 1000).toInt() + 1
            return@withContext AuthResult.Lockout(seconds)
        }
        return@withContext null
    }

    suspend fun verifyInput(input: String): AuthResult = withContext(Dispatchers.IO) {
        val lockoutState = checkLockoutState()
        if (lockoutState != null) return@withContext lockoutState

        val (savedHashHex, savedSaltHex) = settingsRepository.getCredentialHashAndSalt()
        if (savedHashHex.isNullOrBlank() || savedSaltHex.isNullOrBlank()) {
            return@withContext AuthResult.Error("No security credential configured")
        }

        val salt = SecurityUtils.hexToBytes(savedSaltHex)
        val expectedHash = SecurityUtils.hexToBytes(savedHashHex)

        if (salt.isEmpty() || expectedHash.isEmpty()) {
            return@withContext AuthResult.Error("Corrupted credential data. Please reset PIN via Forgot PIN.")
        }

        val computedHash = SecurityUtils.hashPassword(input, salt)

        val isMatch = SecurityUtils.constantTimeEquals(expectedHash, computedHash)

        if (isMatch) {
            settingsRepository.resetFailedAttempts()
            AuthResult.Success
        } else {
            val lockoutRemainingMs = settingsRepository.recordFailedAttempt()
            val (failedCount, _) = settingsRepository.getFailedAttemptData()

            if (lockoutRemainingMs > 0) {
                val seconds = (lockoutRemainingMs / 1000).toInt() + 1
                AuthResult.Failure(remainingAttempts = 0, isLockedOut = true, lockoutSeconds = seconds)
            } else {
                val remainingAttempts = 6 - failedCount
                AuthResult.Failure(remainingAttempts = remainingAttempts, isLockedOut = false, lockoutSeconds = 0)
            }
        }
    }

    suspend fun verifySecurityAnswer(answerInput: String): Boolean = withContext(Dispatchers.IO) {
        val (_, savedHashHex, savedSaltHex) = settingsRepository.getSecurityRecovery()
        if (savedHashHex.isNullOrBlank() || savedSaltHex.isNullOrBlank()) return@withContext true

        val salt = SecurityUtils.hexToBytes(savedSaltHex)
        val expectedHash = SecurityUtils.hexToBytes(savedHashHex)

        if (salt.isEmpty() || expectedHash.isEmpty()) {
            // Corrupted legacy recovery hash from previous code version - allow reset
            return@withContext true
        }

        val computedHash = SecurityUtils.hashPassword(answerInput.trim().lowercase(), salt)

        SecurityUtils.constantTimeEquals(expectedHash, computedHash)
    }

    suspend fun saveNewCredentials(lockType: LockType, credentialInput: String) = withContext(Dispatchers.IO) {
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(credentialInput, salt)
        val saltHex = SecurityUtils.bytesToHex(salt)
        val hashHex = SecurityUtils.bytesToHex(hash)

        settingsRepository.saveCredentials(lockType, hashHex, saltHex)
        settingsRepository.resetFailedAttempts()
    }

    suspend fun saveSecurityRecovery(questionIndex: Int, answerInput: String) = withContext(Dispatchers.IO) {
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(answerInput.trim().lowercase(), salt)
        val saltHex = SecurityUtils.bytesToHex(salt)
        val hashHex = SecurityUtils.bytesToHex(hash)

        settingsRepository.saveSecurityRecovery(questionIndex, hashHex, saltHex)
    }

    suspend fun setBiometricEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        settingsRepository.setBiometricEnabled(enabled)
    }
}
