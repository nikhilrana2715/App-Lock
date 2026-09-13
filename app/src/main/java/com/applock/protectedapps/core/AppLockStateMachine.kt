package com.applock.protectedapps.core

import android.content.Context
import android.content.Intent
import com.applock.protectedapps.data.dao.LockedAppDao
import com.applock.protectedapps.data.repository.RelockPolicy
import com.applock.protectedapps.data.repository.SettingsRepository
import com.applock.protectedapps.ui.lockscreen.OverlayLockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockStateMachine @Inject constructor(
    private val lockedAppDao: LockedAppDao,
    private val settingsRepository: SettingsRepository
) {
    private val _currentState = MutableStateFlow<AppLockState>(AppLockState.Idle)
    val currentState: StateFlow<AppLockState> = _currentState.asStateFlow()

    private val mutex = Mutex()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    @Volatile var currentForegroundPackage: String = ""
        private set

    @Volatile var unlockedPackage: String? = null
        private set

    @Volatile var isOverlayShowing: Boolean = false
        private set

    private val ignoredPackages = setOf(
        "com.applock.protectedapps",
        "com.applock.protectedapps.debug",
        "com.android.systemui",
        "com.google.android.inputmethod.latin",
        "com.sec.android.inputmethod"
    )

    /**
     * Process foreground package change from AccessibilityService or UsageStatsManager
     */
    fun onForegroundPackageChanged(context: Context, packageName: String) {
        if (packageName.isBlank() || ignoredPackages.contains(packageName)) {
            return
        }

        coroutineScope.launch {
            mutex.withLock {
                if (packageName == currentForegroundPackage && !isOverlayShowing && unlockedPackage == packageName) {
                    return@launch
                }

                val previousPackage = currentForegroundPackage
                currentForegroundPackage = packageName

                // Relock evaluation for previously exited app
                val policy = settingsRepository.relockPolicyFlow.first()
                if (previousPackage.isNotBlank() && previousPackage != packageName) {
                    if (policy == RelockPolicy.IMMEDIATELY) {
                        if (unlockedPackage == previousPackage) {
                            unlockedPackage = null
                            _currentState.value = AppLockState.RelockRequired(previousPackage, "Left app")
                        }
                    }
                }

                // Check if newly entered app is locked
                val isLocked = lockedAppDao.isAppLocked(packageName) ?: false
                if (!isLocked) {
                    _currentState.value = AppLockState.Idle
                    return@launch
                }

                _currentState.value = AppLockState.ProtectedAppDetected(packageName)

                // Check if active valid unlock session exists
                if (unlockedPackage == packageName) {
                    _currentState.value = AppLockState.ProtectedAppActive(packageName)
                    return@launch
                }

                // Trigger Lock Screen
                if (!isOverlayShowing) {
                    _currentState.value = AppLockState.LockRequired(packageName)
                    launchOverlayLockScreen(context, packageName)
                }
            }
        }
    }

    private fun launchOverlayLockScreen(context: Context, packageName: String) {
        try {
            isOverlayShowing = true
            _currentState.value = AppLockState.LockScreenVisible(packageName)
            val intent = Intent(context, OverlayLockActivity::class.java).apply {
                putExtra(OverlayLockActivity.EXTRA_PACKAGE_NAME, packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            isOverlayShowing = false
        }
    }

    fun onLockScreenDismissed() {
        isOverlayShowing = false
    }

    fun onAuthenticationSuccess(packageName: String) {
        coroutineScope.launch {
            mutex.withLock {
                unlockedPackage = packageName
                isOverlayShowing = false
                _currentState.value = AppLockState.Unlocked(packageName, System.currentTimeMillis())
                _currentState.value = AppLockState.ProtectedAppActive(packageName)
            }
        }
    }

    fun onScreenOff() {
        coroutineScope.launch {
            mutex.withLock {
                val currentUnlocked = unlockedPackage
                unlockedPackage = null
                isOverlayShowing = false
                if (currentUnlocked != null) {
                    _currentState.value = AppLockState.RelockRequired(currentUnlocked, "Screen off")
                }
                _currentState.value = AppLockState.Locked
            }
        }
    }

    fun invalidateSession() {
        coroutineScope.launch {
            mutex.withLock {
                unlockedPackage = null
                _currentState.value = AppLockState.Locked
            }
        }
    }
}
