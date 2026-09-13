package com.applock.protectedapps.core

sealed class AppLockState {
    object Idle : AppLockState()
    data class ProtectedAppDetected(val packageName: String) : AppLockState()
    data class LockRequired(val packageName: String) : AppLockState()
    data class LockScreenVisible(val packageName: String) : AppLockState()
    data class Authenticating(val packageName: String) : AppLockState()
    data class Unlocked(val packageName: String, val timestamp: Long) : AppLockState()
    data class ProtectedAppActive(val packageName: String) : AppLockState()
    data class RelockRequired(val packageName: String, val reason: String) : AppLockState()
    object Locked : AppLockState()
}
