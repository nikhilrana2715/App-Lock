package com.applock.protectedapps.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.applock.protectedapps.data.repository.SettingsRepository
import com.applock.protectedapps.domain.AuthenticationManager
import com.applock.protectedapps.ui.home.HomeScreen
import com.applock.protectedapps.ui.home.HomeViewModel
import com.applock.protectedapps.ui.lockscreen.LockScreenContent
import com.applock.protectedapps.ui.lockscreen.LockScreenViewModel
import com.applock.protectedapps.ui.onboarding.*
import com.applock.protectedapps.ui.settings.SettingsScreen
import com.applock.protectedapps.ui.settings.SettingsViewModel
import com.applock.protectedapps.ui.splash.SplashScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var authManager: AuthenticationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppLockNavHost(
                        activity = this,
                        settingsRepository = settingsRepository,
                        authManager = authManager
                    )
                }
            }
        }
    }

    fun promptBiometricUnlock(onSuccess: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock AppLock")
            .setSubtitle("Use your fingerprint sensor to access AppLock")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .setNegativeButtonText("Use PIN / Pattern")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

@Composable
fun AppLockNavHost(
    activity: MainActivity,
    settingsRepository: SettingsRepository,
    authManager: AuthenticationManager
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val isOnboardingCompleted by settingsRepository.isOnboardingCompletedFlow.collectAsState(initial = false)
    var isAuthenticatedForAppLock by remember { mutableStateOf(false) }

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    val targetRoute = if (!isOnboardingCompleted) {
                        "welcome"
                    } else if (!isAuthenticatedForAppLock) {
                        "applock_auth"
                    } else {
                        "home"
                    }
                    navController.navigate(targetRoute) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("welcome") {
            WelcomeScreen(
                onStartClick = { navController.navigate("permissions") }
            )
        }
        composable("permissions") {
            PermissionsScreen(
                onContinueClick = { navController.navigate("oem_guide") }
            )
        }
        composable("oem_guide") {
            OemGuideScreen(
                onContinueClick = { navController.navigate("setup_lock") }
            )
        }
        composable("setup_lock") {
            SetupLockScreen(
                authManager = authManager,
                onSetupComplete = {
                    scope.launch {
                        settingsRepository.setOnboardingCompleted(true)
                        isAuthenticatedForAppLock = true
                        navController.navigate("home") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("applock_auth") {
            val lockViewModel: LockScreenViewModel = hiltViewModel()
            LockScreenContent(
                packageName = "com.applock.protectedapps",
                appName = "AppLock Security",
                viewModel = lockViewModel,
                onTriggerBiometric = {
                    activity.promptBiometricUnlock {
                        isAuthenticatedForAppLock = true
                        navController.navigate("home") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onSuccess = {
                    isAuthenticatedForAppLock = true
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onResetLockClick = {
                    navController.navigate("setup_lock")
                }
            )
        }
        composable("home") {
            val homeViewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onChangeCredentialsClick = { navController.navigate("setup_lock") },
                onHealthCheckClick = { navController.navigate("permissions") }
            )
        }
    }
}
