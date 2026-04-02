# SmartTask AI — VS Code Setup & Run Guide

This guide explains how to open, edit, build, and run the SmartTask AI Android project
entirely from **Visual Studio Code** (no Android Studio required).

---

## Prerequisites — Install These First

### 1. Java Development Kit (JDK 17)

```bash
# macOS (Homebrew)
brew install openjdk@17
echo 'export JAVA_HOME=/opt/homebrew/opt/openjdk@17' >> ~/.zshrc
source ~/.zshrc

# Ubuntu / Debian
sudo apt update && sudo apt install -y openjdk-17-jdk
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Windows (use Scoop)
scoop install openjdk17
```

Verify: `java -version`  → should show `17.x.x`

---

### 2. Android SDK (Command-Line Tools)

```bash
# Download from: https://developer.android.com/studio#command-tools
# Extract to ~/Android/sdk/cmdline-tools/latest/

# macOS / Linux — set env vars in ~/.zshrc or ~/.bashrc:
export ANDROID_HOME=~/Android/sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/emulator

# Windows — add to System Environment Variables:
# ANDROID_HOME = C:\Users\YourName\AppData\Local\Android\sdk
# PATH += %ANDROID_HOME%\cmdline-tools\latest\bin
# PATH += %ANDROID_HOME%\platform-tools
```

Install required SDK components:
```bash
sdkmanager --install "platform-tools"
sdkmanager --install "platforms;android-34"
sdkmanager --install "build-tools;34.0.0"
sdkmanager --install "emulator"
sdkmanager --install "system-images;android-34;google_apis;x86_64"
sdkmanager --licenses   # accept all licences
```

---

### 3. VS Code + Extensions

Install VS Code from https://code.visualstudio.com/

Then install these extensions (press `Ctrl+Shift+X`):

| Extension | ID | Purpose |
|-----------|-----|---------|
| **Kotlin** | `fwcd.kotlin` | Syntax highlighting + IntelliSense |
| **Android iOS Emulator** | `DiemasMichiels.emulate` | Launch emulator from VS Code |
| **Gradle for Java** | `vscjava.vscode-gradle` | Sync and run Gradle tasks |
| **XML** | `redhat.vscode-xml` | XML layout editing |
| **Android Full Support** | `JiuLing.android-full-support` | ADB, logcat, file browser |
| **Prettier** | `esbenp.prettier-vscode` | Code formatting |

Install all at once from the terminal:
```bash
code --install-extension fwcd.kotlin
code --install-extension vscjava.vscode-gradle
code --install-extension DiemasMichiels.emulate
code --install-extension redhat.vscode-xml
code --install-extension JiuLing.android-full-support
```

---

## Step-by-Step Project Setup

### Step 1 — Open the Project in VS Code

```bash
# Navigate to project root
cd /path/to/SmartTaskAI

# Open in VS Code
code .
```

### Step 2 — Configure `local.properties`

Create `local.properties` in the project root (it is git-ignored):

```bash
# macOS / Linux
echo "sdk.dir=$HOME/Android/sdk" > local.properties

# Windows (PowerShell)
echo "sdk.dir=C:\\Users\\YourName\\AppData\\Local\\Android\\sdk" > local.properties
```

### Step 3 — Add Your Gemini API Key

Open `app/build.gradle` and replace the placeholder:

```groovy
// Line ~18 in app/build.gradle
buildConfigField "String", "GEMINI_API_KEY", "\"YOUR_GEMINI_API_KEY_HERE\""
```

Get a **free** Gemini API key at:
👉 https://makersuite.google.com/app/apikey

**Alternative (more secure):** Put the key in `local.properties`:
```
GEMINI_API_KEY=your_actual_key_here
```

Then in `app/build.gradle`:
```groovy
def localProps = new Properties()
localProps.load(new FileInputStream(rootProject.file("local.properties")))
buildConfigField "String", "GEMINI_API_KEY", "\"${localProps['GEMINI_API_KEY']}\""
```

---

## Building the Project

### Method A — VS Code Integrated Terminal (Recommended)

Open a terminal in VS Code (`Ctrl+\`` `` ` ``), then:

```bash
# 1. Sync dependencies (first time, downloads ~300 MB)
./gradlew dependencies

# 2. Build debug APK
./gradlew assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk

# 3. Build release APK (needs signing config)
./gradlew assembleRelease
```

### Method B — Gradle Tasks Panel

1. Click the Gradle elephant icon in the VS Code sidebar
2. Expand `SmartTaskAI` → `app` → `Tasks`
3. Double-click: `build` → `assembleDebug`

### Common Gradle Tasks

```bash
./gradlew clean                  # Clean build directory
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew installDebug           # Build + install on connected device
./gradlew test                   # Run unit tests
./gradlew lint                   # Run lint checks
./gradlew dependencies           # List all dependencies
./gradlew :app:signingReport     # Show signing info
```

---

## Running on a Device or Emulator

### Option A — Physical Android Device

1. Enable **Developer Options** on your phone:
   `Settings → About Phone → tap Build Number 7 times`

2. Enable **USB Debugging**:
   `Settings → Developer Options → USB Debugging → ON`

3. Connect via USB, then:

```bash
# Verify device is detected
adb devices
# Should show: List of devices attached
#              XXXXXXXX    device

# Install and launch the app
./gradlew installDebug

# Or push the APK manually
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch the app
adb shell am start -n com.smarttask/.ui.MainActivity

# Watch live logs
adb logcat -s SmartTask,AlarmReceiver,GeminiAIService
```

### Option B — Android Emulator

```bash
# Create a virtual device (AVD)
avdmanager create avd \
  --name "Pixel6_API34" \
  --package "system-images;android-34;google_apis;x86_64" \
  --device "pixel_6"

# List available AVDs
emulator -list-avds

# Launch the emulator
emulator -avd Pixel6_API34 &

# Wait for boot, then install
./gradlew installDebug
```

Or use the **Android iOS Emulator** VS Code extension:
- Press `F1` → `Android: Launch Emulator`
- Select your AVD and it boots automatically

---

## VS Code Tasks (`.vscode/tasks.json`)

Create `.vscode/tasks.json` for one-click builds:

```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Build Debug APK",
      "type": "shell",
      "command": "./gradlew assembleDebug",
      "group": { "kind": "build", "isDefault": true },
      "presentation": { "reveal": "always" },
      "problemMatcher": []
    },
    {
      "label": "Install on Device",
      "type": "shell",
      "command": "./gradlew installDebug",
      "group": "build",
      "presentation": { "reveal": "always" },
      "problemMatcher": []
    },
    {
      "label": "Run Tests",
      "type": "shell",
      "command": "./gradlew test",
      "group": { "kind": "test", "isDefault": true },
      "presentation": { "reveal": "always" },
      "problemMatcher": []
    },
    {
      "label": "Clean Build",
      "type": "shell",
      "command": "./gradlew clean assembleDebug",
      "group": "build",
      "presentation": { "reveal": "always" },
      "problemMatcher": []
    },
    {
      "label": "Watch Logs",
      "type": "shell",
      "command": "adb logcat -s SmartTask,AlarmReceiver,GeminiAIService,AlarmSoundService",
      "group": "none",
      "isBackground": true,
      "presentation": { "reveal": "always", "panel": "new" },
      "problemMatcher": []
    }
  ]
}
```

Run any task: `Ctrl+Shift+P` → `Tasks: Run Task` → select task.
Or press `Ctrl+Shift+B` to run the default **Build Debug APK**.

---

## VS Code Launch Config (`.vscode/launch.json`)

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "android",
      "request": "launch",
      "name": "Run SmartTask AI",
      "appSrcRoot": "${workspaceRoot}/app/src/main",
      "apkFile": "${workspaceRoot}/app/build/outputs/apk/debug/app-debug.apk",
      "adbPort": 5037
    }
  ]
}
```

---

## VS Code Settings (`.vscode/settings.json`)

```json
{
  "java.home": "/usr/lib/jvm/java-17-openjdk-amd64",
  "android.adbPath": "~/Android/sdk/platform-tools/adb",
  "files.exclude": {
    "**/.gradle": true,
    "**/build": true,
    "**/.idea": true
  },
  "editor.formatOnSave": true,
  "[kotlin]": {
    "editor.defaultFormatter": "fwcd.kotlin"
  },
  "[xml]": {
    "editor.defaultFormatter": "redhat.vscode-xml"
  },
  "kotlin.languageServer.enabled": true,
  "kotlin.debugAdapter.enabled": true,
  "gradle.nestedProjects": true
}
```

---

## Exact Alarm Permission (Android 12+)

On first launch on Android 12+ the app needs exact alarm permission:

```bash
# Grant via ADB (skip the settings UI entirely):
adb shell pm grant com.smarttask android.permission.SCHEDULE_EXACT_ALARM

# Check it was granted:
adb shell dumpsys package com.smarttask | grep EXACT_ALARM
```

Or go to: **Settings → Apps → SmartTask AI → Alarms & Reminders → Allow**

---

## Quick ADB Reference

```bash
# List connected devices
adb devices

# Install APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Uninstall app
adb uninstall com.smarttask

# Launch MainActivity
adb shell am start -n com.smarttask/.ui.MainActivity

# Simulate alarm trigger (test alarm screen)
adb shell am broadcast \
  -a com.smarttask.ALARM_TRIGGER \
  --el task_id 1 \
  --es task_title "Medicine Reminder" \
  --es task_priority "high" \
  com.smarttask/.receiver.AlarmReceiver

# Clear app data (reset Room DB)
adb shell pm clear com.smarttask

# View crash logs only
adb logcat -s AndroidRuntime:E

# View SmartTask logs
adb logcat -s SmartTask GeminiAIService AlarmReceiver AlarmSoundService

# Take screenshot
adb exec-out screencap -p > screen.png

# Pull APK from device
adb shell pm path com.smarttask
adb pull /data/app/com.smarttask-.../base.apk smarttask_backup.apk
```

---

## Troubleshooting

| Error | Cause | Fix |
|-------|-------|-----|
| `SDK location not found` | Missing `local.properties` | `echo "sdk.dir=~/Android/sdk" > local.properties` |
| `Gradle sync failed` | No internet / wrong Gradle version | Run `./gradlew --version` and check Java 17 |
| `AAPT2 error` | Missing drawable/layout resource | Check `res/drawable/` and `res/layout/` have all files |
| `BuildConfig not found` | `buildFeatures { buildConfig true }` missing | Already added in `app/build.gradle` |
| `Hilt component not found` | Missing `@HiltAndroidApp` | Already on `SmartTaskApp.kt` |
| `Room schema export error` | Missing export directory | Add `room.schemaLocation` to `build.gradle` if needed |
| `AlarmManager SecurityException` | No exact alarm permission | Grant via ADB (see above) |
| `AI response null` | Bad/missing API key | Add key to `build.gradle` or Settings screen |
| `KAPT / KSP errors` | Wrong Kotlin/KSP version mismatch | Ensure `kotlin = 1.9.22` and `ksp = 1.9.22-1.0.17` |
| `App crashes on launch` | Check logcat | `adb logcat -s AndroidRuntime:E` |

---

## Project Structure Quick Reference

```
SmartTaskAI/
├── app/build.gradle           ← dependencies, applicationId, buildConfig
├── build.gradle               ← root plugin declarations
├── settings.gradle            ← project name, module include
├── gradle.properties          ← JVM args, AndroidX flags
├── local.properties           ← sdk.dir (create this yourself, git-ignored)
├── gradlew / gradlew.bat      ← Gradle wrapper scripts
└── app/src/main/
    ├── AndroidManifest.xml    ← permissions, activities, receivers, services
    ├── java/com/smarttask/
    │   ├── SmartTaskApp.kt    ← @HiltAndroidApp, notification channels
    │   ├── database/          ← Room DB, DAOs, Entities
    │   ├── repository/        ← TaskRepository (single source of truth)
    │   ├── viewmodel/         ← TaskViewModel, AssistantViewModel
    │   ├── ui/                ← Fragments, Activities, Adapters
    │   ├── service/           ← AlarmSoundService (foreground)
    │   ├── receiver/          ← AlarmReceiver, NotificationActionReceiver
    │   ├── utils/             ← GeminiAIService, AlarmScheduler, etc.
    │   ├── widget/            ← TasksWidgetProvider
    │   └── di/                ← AppModule (Hilt)
    └── res/
        ├── layout/            ← All XML UI layouts (11 files)
        ├── drawable/          ← Vector icons + shape backgrounds (35 files)
        ├── navigation/        ← nav_graph.xml
        ├── values/            ← colors, strings, themes
        ├── anim/              ← slide, fade, pulse animations
        ├── menu/              ← bottom_nav_menu.xml
        ├── mipmap-*/          ← launcher icons
        └── xml/               ← widget_info, backup_rules, file_paths
```

---

## Building a Signed Release APK

```bash
# Step 1: Generate a keystore (one-time)
keytool -genkey -v \
  -keystore smarttask-release.keystore \
  -alias smarttask \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

# Step 2: Add signing to app/build.gradle
# Inside android { } block:
signingConfigs {
    release {
        storeFile     file("../smarttask-release.keystore")
        storePassword "your_store_password"
        keyAlias      "smarttask"
        keyPassword   "your_key_password"
    }
}
buildTypes {
    release {
        signingConfig     signingConfigs.release
        minifyEnabled     true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}

# Step 3: Build signed release
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

---

*SmartTask AI — Full Android Source · Kotlin · MVVM · Jetpack · Gemini AI*
