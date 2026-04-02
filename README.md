# SmartTask AI — Complete Android App

## 🚀 Production-ready AI-powered To-Do & Alarm App

Built with **Kotlin**, **MVVM**, **Jetpack**, **Room**, and **Gemini AI**.

---

## 📁 Project Structure

```
SmartTaskAI/
├── app/
│   ├── build.gradle                    ← All dependencies
│   └── src/main/
│       ├── AndroidManifest.xml         ← Permissions + components
│       └── java/com/smarttask/
│           ├── SmartTaskApp.kt         ← Application class (Hilt + channels)
│           ├── database/
│           │   ├── AppDatabase.kt      ← Room database
│           │   ├── dao/
│           │   │   ├── TaskDao.kt      ← All task queries
│           │   │   └── ChatMessageDao.kt
│           │   └── entities/
│           │       ├── TaskEntity.kt   ← Task data model
│           │       └── ChatMessageEntity.kt
│           ├── repository/
│           │   └── TaskRepository.kt  ← Single source of truth
│           ├── viewmodel/
│           │   ├── TaskViewModel.kt    ← Tasks + AI MVVM
│           │   └── AssistantViewModel.kt ← Chat MVVM
│           ├── ui/
│           │   ├── MainActivity.kt     ← Host + bottom nav
│           │   ├── tasks/
│           │   │   ├── TasksFragment.kt
│           │   │   └── TasksAdapter.kt
│           │   ├── calendar/
│           │   │   └── CalendarFragment.kt
│           │   ├── assistant/
│           │   │   ├── AssistantFragment.kt
│           │   │   └── ChatAdapter.kt
│           │   ├── settings/
│           │   │   └── SettingsFragment.kt
│           │   ├── add_edit/
│           │   │   └── AddEditTaskFragment.kt ← Full form + voice + AI
│           │   └── alarm/
│           │       └── AlarmActivity.kt ← Full-screen alarm
│           ├── service/
│           │   └── AlarmSoundService.kt ← Foreground alarm sound+vibration
│           ├── receiver/
│           │   ├── AlarmReceiver.kt    ← Boot + alarm trigger
│           │   └── NotificationActionReceiver.kt ← Done/Snooze/Dismiss
│           ├── utils/
│           │   ├── GeminiAIService.kt  ← Gemini API integration
│           │   ├── AlarmScheduler.kt   ← Exact alarm scheduling
│           │   ├── NotificationHelper.kt
│           │   └── PreferencesManager.kt ← Encrypted preferences
│           ├── widget/
│           │   └── TasksWidgetProvider.kt ← Home screen widget
│           └── di/
│               └── AppModule.kt        ← Hilt dependency injection
└── res/
    ├── layout/                         ← All XML layouts
    ├── navigation/nav_graph.xml        ← Navigation Component
    ├── menu/bottom_nav_menu.xml
    ├── anim/                           ← Slide + pulse animations
    ├── values/
    │   ├── colors.xml                  ← Full dark/light palette
    │   ├── themes.xml                  ← Material 3 themes
    │   └── strings.xml
    └── xml/                            ← Widget info, backup rules
```

---

## ⚡ Quick Setup (5 Steps)

### Step 1 — Prerequisites

| Tool | Version |
|------|---------|
| Android Studio | Hedgehog (2023.1.1) or newer |
| JDK | 17+ |
| Android SDK | API 26–34 |
| Kotlin | 1.9.22 |
| Gradle | 8.2+ |

### Step 2 — Clone / Import

```bash
# If using git
git clone <your-repo-url> SmartTaskAI
cd SmartTaskAI

# Or: File → Open in Android Studio → select SmartTaskAI folder
```

### Step 3 — Get Gemini API Key (FREE)

1. Go to [https://makersuite.google.com/app/apikey](https://makersuite.google.com/app/apikey)
2. Click **"Create API key"**
3. Copy the key

Then add it in **one of two ways**:

**Option A — In `app/build.gradle`** (for development):
```groovy
buildConfigField "String", "GEMINI_API_KEY", "\"YOUR_KEY_HERE\""
```

**Option B — In the app Settings screen** (recommended for production):
- Launch the app → Settings → AI Features → Enter API Key → Save

### Step 4 — Sync & Build

```bash
# In Android Studio terminal:
./gradlew build

# Or: Build → Make Project (Ctrl+F9)
```

### Step 5 — Run

```bash
# Connect a device (USB debug on) or start emulator, then:
./gradlew installDebug

# Or click ▶ Run in Android Studio
```

---

## 🔔 Exact Alarm Permission (Android 12+)

On Android 12+ devices you need exact alarm permission:

```
Settings → Apps → SmartTask AI → Alarms & Reminders → Allow
```

Or the app will request it automatically on launch.

---

## 🏗️ Build APK

### Debug APK
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release APK (requires keystore)
```bash
# 1. Generate keystore (one-time):
keytool -genkey -v -keystore smarttask.keystore \
  -alias smarttask -keyalg RSA -keysize 2048 -validity 10000

# 2. Add to app/build.gradle signingConfigs:
signingConfigs {
    release {
        storeFile file("smarttask.keystore")
        storePassword "YOUR_STORE_PASSWORD"
        keyAlias "smarttask"
        keyPassword "YOUR_KEY_PASSWORD"
    }
}

# 3. Build:
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

---

## 🤖 AI Features Explained

### 1. Natural Language Task Creation
Type: `"Remind me to take medicine every day at 8 AM"`
→ Gemini extracts: title, date, time (08:00), category (health), recurring (daily)

### 2. Voice Input
Tap the 🎤 mic icon on the Add Task screen → speak → AI parses automatically

### 3. AI Chat Assistant
Full conversation context with your task list. Commands like:
- "What tasks do I have today?"
- "Add a workout session for Saturday morning"
- "What's my most urgent task?"
→ Tasks are auto-created from chat

### 4. AI Task Prioritization
Settings → AI Re-Prioritize → Gemini scores all tasks 0.0–1.0 by urgency/importance

---

## 🔔 Alarm System Architecture

```
Task saved
    ↓
AlarmScheduler.scheduleAlarm()  ← AlarmManager.setExactAndAllowWhileIdle()
    ↓
[Time arrives]
    ↓
AlarmReceiver.onReceive()
    ↓
├── AlarmSoundService (foreground)  ← MediaPlayer + Vibrator
└── AlarmActivity (full-screen)    ← Shows over lock screen
    ↓
User: [Snooze | Dismiss | Done]
    ↓
NotificationActionReceiver  ← handles notification action buttons
```

---

## 📦 Key Dependencies

| Library | Purpose |
|---------|---------|
| Room 2.6 | Local database (offline-first) |
| Hilt 2.50 | Dependency injection |
| Navigation Component 2.7 | Fragment navigation |
| WorkManager 2.9 | Background recurring task generation |
| Kizitonwose Calendar 2.5 | Beautiful calendar view |
| Retrofit 2.9 + OkHttp 4.12 | Gemini API calls |
| DataStore + Security Crypto | Encrypted settings |
| Lottie 6.3 | Animations |
| Glance 1.0 | Home screen widget |
| Material 3 | UI components |

---

## 🔒 Security

- API key stored in **EncryptedSharedPreferences** (AES-256-GCM)
- No plaintext secrets in source code
- Permissions requested at runtime (notifications, microphone, exact alarms)
- Database not backed up to cloud (backup_rules.xml excludes sensitive data)

---

## 📱 Screens Overview

| Screen | Features |
|--------|---------|
| **Tasks** | Filter All/Pending/Completed, search, swipe-to-delete, overdue highlight |
| **Calendar** | Monthly grid with task dots, day task list, tap any date |
| **Add/Edit Task** | AI NLP input, voice input, date/time pickers, priority, category, recurring |
| **AI Assistant** | Full chat, quick actions, auto task creation, conversation history |
| **Settings** | Dark/light mode, API key, AI toggle, task analytics, completion rate |
| **Alarm (full-screen)** | Clock, animated bell, Snooze/Dismiss/Done, shows over lock screen |
| **Widget** | Home screen widget showing today's top 3 tasks |

---

## 🐛 Troubleshooting

| Problem | Fix |
|---------|-----|
| Alarm not firing | Check exact alarm permission in device settings |
| AI not responding | Verify Gemini API key is correct and internet is on |
| Voice input missing | Grant RECORD_AUDIO permission |
| Build fails | Run `./gradlew clean` then rebuild |
| Room migration error | Clear app data or increment DB version |
| Notification not showing | Grant POST_NOTIFICATIONS permission (Android 13+) |

---

## 🗺️ Roadmap / Bonus Features to Add

- [ ] Firebase Firestore cloud sync
- [ ] Biometric lock screen
- [ ] Rich push notification media style
- [ ] Google Calendar integration
- [ ] Task sharing / collaboration
- [ ] Analytics dashboard with charts
- [ ] Multiple AI models (OpenAI, Anthropic)
- [ ] Smart location-based reminders

---

## 📞 Architecture Diagram

```
UI Layer (Fragments / Activities)
         ↕ observe LiveData
ViewModel Layer (TaskViewModel, AssistantViewModel)
         ↕ call suspend functions
Repository Layer (TaskRepository)
    ↕ Room DB          ↕ GeminiAIService
TaskDao/ChatDao     Retrofit → Gemini API
```

**Pattern**: MVVM + Repository + Single Source of Truth

---

*SmartTask AI — Built with ❤️ using Kotlin, Jetpack, Material 3, and Gemini AI*
