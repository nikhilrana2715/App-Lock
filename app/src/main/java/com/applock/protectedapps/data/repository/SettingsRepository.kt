package com.applock.protectedapps.data.repository

import android.content.Context
import android.os.SystemClock
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "applock_settings")

enum class LockType { PIN, PATTERN }
enum class RelockPolicy { IMMEDIATELY, AFTER_SCREEN_OFF }

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val LOCK_TYPE = stringPreferencesKey("lock_type")
        val CREDENTIAL_HASH = stringPreferencesKey("credential_hash")
        val CREDENTIAL_SALT = stringPreferencesKey("credential_salt")
        val SECURITY_QUESTION_INDEX = intPreferencesKey("security_question_index")
        val SECURITY_ANSWER_HASH = stringPreferencesKey("security_answer_hash")
        val SECURITY_ANSWER_SALT = stringPreferencesKey("security_answer_salt")
        val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        val IS_FAST_LOCK_ENABLED = booleanPreferencesKey("is_fast_lock_enabled")
        val RELOCK_POLICY = stringPreferencesKey("relock_policy")
        val HIDE_PATTERN_PATH = booleanPreferencesKey("hide_pattern_path")
        val VIBRATE_ON_PRESS = booleanPreferencesKey("vibrate_on_press")
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val FAILED_ATTEMPT_COUNT = intPreferencesKey("failed_attempt_count")
        val LOCKOUT_END_ELAPSED_REALTIME = longPreferencesKey("lockout_end_elapsed_realtime")
        val LOCKOUT_END_SYSTEM_TIME = longPreferencesKey("lockout_end_system_time")
    }

    val isOnboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.IS_ONBOARDING_COMPLETED] ?: false
    }

    val lockTypeFlow: Flow<LockType> = context.dataStore.data.map { prefs ->
        val type = prefs[PreferencesKeys.LOCK_TYPE] ?: LockType.PIN.name
        runCatching { LockType.valueOf(type) }.getOrDefault(LockType.PIN)
    }

    val isBiometricEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.IS_BIOMETRIC_ENABLED] ?: false
    }

    val isFastLockEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.IS_FAST_LOCK_ENABLED] ?: true
    }

    val relockPolicyFlow: Flow<RelockPolicy> = context.dataStore.data.map { prefs ->
        val policy = prefs[PreferencesKeys.RELOCK_POLICY] ?: RelockPolicy.IMMEDIATELY.name
        runCatching { RelockPolicy.valueOf(policy) }.getOrDefault(RelockPolicy.IMMEDIATELY)
    }

    val hidePatternPathFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.HIDE_PATTERN_PATH] ?: false
    }

    val vibrateOnPressFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.VIBRATE_ON_PRESS] ?: true
    }

    suspend fun getCredentialHashAndSalt(): Pair<String?, String?> {
        val prefs = context.dataStore.data.first()
        val hash = prefs[PreferencesKeys.CREDENTIAL_HASH]
        val salt = prefs[PreferencesKeys.CREDENTIAL_SALT]
        return Pair(hash, salt)
    }

    suspend fun saveCredentials(lockType: LockType, hashHex: String, saltHex: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.LOCK_TYPE] = lockType.name
            prefs[PreferencesKeys.CREDENTIAL_HASH] = hashHex
            prefs[PreferencesKeys.CREDENTIAL_SALT] = saltHex
        }
    }

    suspend fun saveSecurityRecovery(questionIndex: Int, answerHashHex: String, answerSaltHex: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SECURITY_QUESTION_INDEX] = questionIndex
            prefs[PreferencesKeys.SECURITY_ANSWER_HASH] = answerHashHex
            prefs[PreferencesKeys.SECURITY_ANSWER_SALT] = answerSaltHex
        }
    }

    suspend fun getSecurityRecovery(): Triple<Int, String?, String?> {
        val prefs = context.dataStore.data.first()
        val index = prefs[PreferencesKeys.SECURITY_QUESTION_INDEX] ?: 0
        val hash = prefs[PreferencesKeys.SECURITY_ANSWER_HASH]
        val salt = prefs[PreferencesKeys.SECURITY_ANSWER_SALT]
        return Triple(index, hash, salt)
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setFastLockEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_FAST_LOCK_ENABLED] = enabled
        }
    }

    suspend fun setRelockPolicy(policy: RelockPolicy) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.RELOCK_POLICY] = policy.name
        }
    }

    suspend fun setHidePatternPath(hide: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.HIDE_PATTERN_PATH] = hide
        }
    }

    suspend fun setVibrateOnPress(vibrate: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.VIBRATE_ON_PRESS] = vibrate
        }
    }

    // Failed Attempts and Lockout Persistence (handles reboot survival)
    suspend fun getFailedAttemptData(): Pair<Int, Long> {
        val prefs = context.dataStore.data.first()
        val count = prefs[PreferencesKeys.FAILED_ATTEMPT_COUNT] ?: 0
        val elapsedEnd = prefs[PreferencesKeys.LOCKOUT_END_ELAPSED_REALTIME] ?: 0L
        val systemEnd = prefs[PreferencesKeys.LOCKOUT_END_SYSTEM_TIME] ?: 0L
        
        // Check current time against elapsed realtime and fallback system time
        val nowElapsed = SystemClock.elapsedRealtime()
        val nowSystem = System.currentTimeMillis()

        val lockoutEnd = if (nowElapsed < elapsedEnd || nowSystem < systemEnd) {
            maxOf(elapsedEnd - nowElapsed, systemEnd - nowSystem)
        } else {
            0L
        }
        return Pair(count, lockoutEnd)
    }

    suspend fun recordFailedAttempt(): Long {
        var remainingLockout = 0L
        context.dataStore.edit { prefs ->
            val currentCount = (prefs[PreferencesKeys.FAILED_ATTEMPT_COUNT] ?: 0) + 1
            prefs[PreferencesKeys.FAILED_ATTEMPT_COUNT] = currentCount

            if (currentCount >= 6) {
                val lockoutDuration = 60_000L // 60 seconds
                val elapsedEnd = SystemClock.elapsedRealtime() + lockoutDuration
                val systemEnd = System.currentTimeMillis() + lockoutDuration
                prefs[PreferencesKeys.LOCKOUT_END_ELAPSED_REALTIME] = elapsedEnd
                prefs[PreferencesKeys.LOCKOUT_END_SYSTEM_TIME] = systemEnd
                remainingLockout = lockoutDuration
            }
        }
        return remainingLockout
    }

    suspend fun resetFailedAttempts() {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.FAILED_ATTEMPT_COUNT] = 0
            prefs[PreferencesKeys.LOCKOUT_END_ELAPSED_REALTIME] = 0L
            prefs[PreferencesKeys.LOCKOUT_END_SYSTEM_TIME] = 0L
        }
    }
}
