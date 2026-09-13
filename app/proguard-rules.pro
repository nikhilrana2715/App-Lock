# ProGuard & R8 configuration rules for AppLock

# Hilt & Dagger
-keep class * extends dagger.hilt.internal.UnsafeCasts { *; }
-keep class dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}

# Jetpack Compose
-keepclassmembers class * extends androidx.compose.ui.Modifier { *; }
-dontwarn androidx.compose.**

# Tink Security & Crypto
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# Android Biometrics
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# Keep Data Models & Room Entities
-keep class com.applock.protectedapps.data.entity.** { *; }
