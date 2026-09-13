package com.applock.protectedapps.ui.home

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.applock.protectedapps.data.dao.LockedAppDao
import com.applock.protectedapps.data.entity.LockedAppEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AppItem(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isLocked: Boolean,
    val isRecommended: Boolean = false
)

data class HomeUiState(
    val allApps: List<AppItem> = emptyList(),
    val searchQuery: String = "",
    val selectedTab: Int = 0, // 0: All Apps, 1: Locked Apps
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lockedAppDao: LockedAppDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val sensitivePackages = setOf(
        "com.android.settings",
        "com.android.vending",
        "com.google.android.apps.photos",
        "com.sec.android.app.myfiles",
        "com.android.documentsui",
        "com.google.android.packageinstaller"
    )

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val intent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = pm.queryIntentActivities(intent, 0)
                val lockedPackageNames = lockedAppDao.getLockedPackageNames().toSet()

                val appsList = resolveInfos.mapNotNull { resolveInfo ->
                    val pkg = resolveInfo.activityInfo.packageName
                    if (pkg == context.packageName) return@mapNotNull null

                    val name = resolveInfo.loadLabel(pm).toString()
                    val icon = resolveInfo.loadIcon(pm)
                    val isLocked = lockedPackageNames.contains(pkg)
                    val isRecommended = sensitivePackages.contains(pkg)

                    AppItem(
                        packageName = pkg,
                        appName = name,
                        icon = icon,
                        isLocked = isLocked,
                        isRecommended = isRecommended
                    )
                }.sortedBy { it.appName.lowercase() }

                _uiState.value = _uiState.value.copy(allApps = appsList, isLoading = false)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onTabSelected(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tabIndex)
    }

    fun toggleLockState(appItem: AppItem) {
        viewModelScope.launch {
            val newLockedState = !appItem.isLocked
            lockedAppDao.insertOrUpdate(
                LockedAppEntity(
                    packageName = appItem.packageName,
                    appName = appItem.appName,
                    isLocked = newLockedState
                )
            )
            // Update local state list
            val updated = _uiState.value.allApps.map {
                if (it.packageName == appItem.packageName) it.copy(isLocked = newLockedState)
                else it
            }
            _uiState.value = _uiState.value.copy(allApps = updated)
        }
    }
}
