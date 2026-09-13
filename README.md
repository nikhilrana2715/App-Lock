# 🔒 AppLock - Privacy & Security Utility

[![Release](https://img.shields.io/badge/Release-NKJ.15.18-indigo.svg)](https://github.com/nikhilrana2715)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-blue.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-purple.svg)](https://developer.android.com/jetpack/compose)

**AppLock** is a modern, high-performance Android security application built with **Kotlin** and **Jetpack Compose**. It provides real-time foreground app protection, hardware biometric authentication, customizable security patterns/PINs, and a clean Light Theme user experience.

---

## 📲 App Installation & Download Link

Click below to download and install the latest signed release APK directly on your Android device:

📥 **[Download Release APK (Version NKJ.15.18)](https://firebaseappdistribution.googleapis.com/app-binary-downloads/projects/837855615393/apps/1:837855615393:android:af228e02ea6fa0598c13e0/releases/4u5cuftsu5flg/binaries/8694f9ed00f50eaef9feb84a8b7900d356523b0f74c0f40cd44215a051f07ca2/app.apk?utm_source=firebase-tools&token=AEbaB5wAAAAAaqau8mJTOzTqNAcZSYYVZ1SBxjY8xf5dwn_saWZI5gMSyEb9zXASKrKTl1wvShOoPbEr-ufyz9GDmKamvAHGG7dgLJ_XebECrAJHX0L9KgGn7KwV-XiUkex7Ls4OgbxeNLpjRSBNYoCitO_GgJ0A0VLcaoQVjTQ2c95tqSBBQSr-X-Hmzs9NEN6tt2S93_QdUzZo8dq55dYuYaThImSZny1YqsYjgbQmTgB4JAwswkV42mduGcbW0wnHj4vN_54ELt4MbGwMdDgCFZEYNrv3LNcMHJewHGrv9DK51OseCX618gbs5UxM6TFKphKW-mhiY-EshWcK_rMeIdMUpvIggaKpfKk)**

🌐 **[Firebase App Distribution Tester Link](https://appdistribution.firebase.google.com/testerapps/1:837855615393:android:af228e02ea6fa0598c13e0/releases/4u5cuftsu5flg)**

---

## ✨ Features

- **Porcelain Light Theme UI (`#F8FAFC`)**: Sleek, modern user interface with soft drop shadows and high readability.
- **20 Real Brand App Icons**: Official icons for WhatsApp, Instagram, Facebook, Snapchat, YouTube, Spotify, Gallery, Settings, and more.
- **6-Second Animated Emblem**: Smooth rotating neon gradient ring with lock-to-happy smile transition.
- **Biometric & PIN/Pattern Lock**: Hardware Fingerprint integration alongside 4-digit PIN and 3x3 Grid Pattern security.
- **Foreground Overlay Protection Engine**: Instant system overlay lock window when protected applications launch.
- **Customizable Security Settings**:
  - Change PIN / Change Pattern
  - Toggle Fingerprint Authentication
  - Relock Policy Configuration
  - Hide Pattern Path Trail
  - About AppLock & Privacy Policy (`Version NKJ.15.18` - Developed By Nikhil)

---

## 🛠 Tech Stack & Architecture

- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose + Material 3 Design
- **Architecture**: MVVM + Clean Architecture with Coroutines & Flow
- **Dependency Injection**: Hilt (Dagger)
- **Local Storage**: Room Database + Jetpack DataStore
- **Encryption**: Google Tink Security Crypto
- **Authentication**: AndroidX Biometric Prompt API

---

## ⚙️ Building & Running

1. **Clone the repository**:
   ```bash
   git clone https://github.com/nikhilrana2715/AppLock.git
   cd AppLock
   ```
2. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
3. **Build Release APK**:
   ```bash
   ./gradlew assembleRelease
   ```

---

## 👨‍💻 Developer Information

- **Developer**: Nikhil (`nikhilrana2715`)
- **GitHub Profile**: [https://github.com/nikhilrana2715](https://github.com/nikhilrana2715)
- **Version**: `NKJ.15.18`
