<![CDATA[<div align="center">

# 🧠 SmartTask AI

### AI-Powered Task Management & Alarm System for Android

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-API%2026+-34A853?logo=android&logoColor=white)](https://developer.android.com/)
[![Material 3](https://img.shields.io/badge/Material%203-1.11-757575?logo=material-design&logoColor=white)](https://m3.material.io/)
[![Hilt](https://img.shields.io/badge/Hilt-2.50-4285F4?logo=google&logoColor=white)](https://dagger.dev/hilt/)
[![Gemini AI](https://img.shields.io/badge/Gemini%20AI-1.5%20Flash-8E75B2?logo=google&logoColor=white)](https://ai.google.dev/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**SmartTask AI** is a production-ready Android application that combines intelligent task management with a powerful alarm system. It leverages **Google's Gemini 1.5 Flash** model to understand natural language, auto-create tasks from voice and text, hold contextual conversations, and intelligently prioritize your to-do list.

[Features](#-features) · [Screenshots](#-screens-overview) · [Quick Start](#-quick-start) · [Architecture](#-architecture) · [Contributing](#-contributing)

</div>

---

## ✨ Features

### 🤖 AI-Powered Intelligence
| Feature | Description |
|---------|-------------|
| **Natural Language Parsing** | Type or speak naturally — *"Remind me to take medicine every day at 8 AM"* — and Gemini extracts the title, date, time, category, priority, and recurring schedule automatically |
| **Voice Input** | Tap the 🎤 mic icon on the Add Task screen, speak your task, and let AI parse it into structured fields |
| **Conversational Assistant** | Full chat interface with conversation context. Ask *"What tasks do I have today?"* or say *"Add a workout session for Saturday morning"* — tasks are auto-created from chat |
| **Smart Prioritization** | One-tap AI re-prioritization scores all pending tasks 0.0–1.0 by urgency and importance using Gemini |

### 📋 Task Management
- **Create / Edit / Delete** tasks with title, description, date, time, priority, category, and more
- **Three priority levels** — High 🔴, Medium 🟡, Low 🟢 — with color-coded UI indicators
- **Five categories** — General, Work, Health, Study, Personal — each with a distinct icon
- **Recurring tasks** — None, Daily, Weekly, or Custom (e.g., `MON,WED,FRI`)
- **Search & filter** — Full-text search, filter by All / Pending / Completed
- **Swipe-to-delete** with undo snackbar
- **Overdue highlighting** — Missed tasks are visually flagged
- **Completion tracking** — Analytics with completion rate percentage

### 📅 Calendar View
- Beautiful **monthly grid** powered by [Kizitonwose Calendar](https://github.com/kizitonwose/Calendar)
- **Task indicator dots** on dates with tasks
- Tap any date to view that day's task list
- Seamless navigation between months

### ⏰ Full-Featured Alarm System
- **Exact alarms** via `AlarmManager.setExactAndAllowWhileIdle()` — fires even in Doze mode
- **Full-screen alarm activity** — shows over lock screen with animated bell 🔔
- **Foreground sound service** — MediaPlayer + Vibrator running as a foreground service
- **Three alarm actions**: Snooze (10 min), Dismiss, or Mark Done
- **Notification action buttons** — Snooze / Dismiss / Done directly from the notification shade
- **Boot-persistent** — Alarms automatically reschedule after device reboot
- **Priority-colored** alarm screen with real-time clock display

### 🏠 Home Screen Widget
- Glance-powered widget showing **today's top 3 pending tasks**
- **Tap to open** the full app
- Auto-updates with task count and formatted times

### ⚙️ Settings & Preferences
- **Dark / Light mode** toggle with instant `AppCompatDelegate` switching
- **Notification toggle** (enable/disable reminders)
- **AI Features toggle** (on/off) with separate **API key input**
- **Task analytics dashboard** — total, completed, pending, high-priority counts with circular progress bar
- **Encrypted storage** — API keys stored via `EncryptedSharedPreferences` (AES-256-GCM)

---

## 📱 Screens Overview

| Screen | Key Features |
|--------|-------------|
| **Tasks** | Filter All/Pending/Completed, search bar, swipe-to-delete, overdue badges, FAB to add |
| **Calendar** | Monthly grid with task dots, day task list, tap any date to drill down |
| **Add / Edit Task** | AI natural-language input, voice input 🎤, date/time pickers, priority/category selectors, recurring options |
| **AI Assistant** | Full chat UI, quick actions, auto task creation, conversation history, typing indicator |
| **Settings** | Dark mode switch, notification toggle, AI on/off, API key input, task analytics with completion rate |
| **Alarm (Full-screen)** | Live clock, animated bell pulse, Snooze/Dismiss/Done buttons, priority color bar, shows over lock screen |
| **Widget** | Home screen widget displaying today's top 3 tasks with times |

---

## 🚀 Quick Start

### Prerequisites

| Tool | Required Version |
|------|-----------------|
| **Android Studio** | Hedgehog (2023.1.1) or newer |
| **JDK** | 17+ |
| **Android SDK** | API 26 (min) — API 34 (target) |
| **Kotlin** | 1.9.22 |
| **Gradle** | 8.2+ |

### Step 1 — Clone the Repository

```bash
git clone https://github.com/your-username/SmartTaskAI.git
cd SmartTaskAI
```

Or open in Android Studio: **File → Open → select the `SmartTaskAI` folder**

### Step 2 — Configure `local.properties`

Copy the example and fill in your SDK path:

```bash
cp local.properties.example local.properties
```

Edit `local.properties`:

```properties
# Android SDK location (required)
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\sdk

# Gemini API key (optional — can also be set in-app)
GEMINI_API_KEY=your_gemini_api_key_here
```

### Step 3 — Get a Gemini API Key (Free)

1. Go to [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Click **"Create API key"**
3. Copy the key

Add it in **one of three ways** (in order of recommendation):

| Method | How | Best For |
|--------|-----|----------|
| **In-App Settings** ⭐ | Launch app → Settings → AI Features → Enter API Key → Save | Production / daily use |
| **`local.properties`** | Add `GEMINI_API_KEY=your_key` | Team development |
| **`build.gradle`** | Set `buildConfigField "String", "GEMINI_API_KEY", ..."` | Quick local testing |

> [!IMPORTANT]
> Never commit real API keys to version control. The `local.properties` file is git-ignored by default. In-app keys are encrypted with AES-256-GCM.

### Step 4 — Sync & Build

```bash
# Via terminal:
./gradlew build

# Or in Android Studio:
# Build → Make Project (Ctrl+F9)
```

### Step 5 — Run

```bash
# Connect a device (USB debugging enabled) or start an emulator:
./gradlew installDebug

# Or click ▶ Run in Android Studio
```

---

## 🏗️ Architecture

SmartTask AI follows a clean **MVVM + Repository + Single Source of Truth** architecture.

```
┌─────────────────────────────────────────────────────────────────────┐
│                         UI LAYER                                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌────────┐ │
│  │ TasksFragment│  │CalendarFrag. │  │AssistantFrag.│  │Settings│ │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └───┬────┘ │
│         │ observe         │ observe         │ observe       │      │
│         ▼                 ▼                 ▼               ▼      │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                   VIEWMODEL LAYER                           │   │
│  │   TaskViewModel          │       AssistantViewModel         │   │
│  └─────────────┬────────────┴──────────────┬──────────────────┘   │
│                │ suspend functions          │                      │
│                ▼                            ▼                      │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                   REPOSITORY LAYER                          │   │
│  │                    TaskRepository                           │   │
│  │              (Single Source of Truth)                        │   │
│  └──────┬──────────────────┬──────────────────┬───────────────┘   │
│         │                  │                  │                    │
│         ▼                  ▼                  ▼                    │
│  ┌────────────┐     ┌────────────┐     ┌────────────────┐         │
│  │  Room DB   │     │  Alarm     │     │ GeminiAI       │         │
│  │ TaskDao    │     │ Scheduler  │     │ Service        │         │
│  │ ChatDao    │     │            │     │ (Retrofit)     │         │
│  └────────────┘     └────────────┘     └────────────────┘         │
│                                              │                    │
│                                              ▼                    │
│                                     Gemini 1.5 Flash API          │
└─────────────────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

| Layer | Responsibilities |
|-------|-----------------|
| **UI** | Fragments, Activities, Adapters — observes LiveData, renders UI, delegates user actions to ViewModels |
| **ViewModel** | Holds UI state as `LiveData`/`MutableLiveData`, calls repository suspend functions within `viewModelScope`, exposes AI parsing/prioritization |
| **Repository** | Orchestrates TaskDao + ChatMessageDao + AlarmScheduler + GeminiAIService. Single source of truth for all data operations |
| **Data** | Room database (SQLite), Retrofit HTTP client (Gemini API), AlarmManager (system alarms), EncryptedSharedPreferences |

### Dependency Injection (Hilt)

All dependencies are provided via Dagger Hilt:

```
@HiltAndroidApp SmartTaskApp
    └── @Module AppModule (provides: Room DB, DAOs, OkHttpClient, Gson, PreferencesManager)
        ├── @Singleton TaskRepository
        ├── @Singleton GeminiAIService
        ├── @Singleton AlarmScheduler
        ├── @HiltViewModel TaskViewModel
        └── @HiltViewModel AssistantViewModel
```

---

## 📁 Project Structure

```
SmartTaskAI/
├── app/
│   ├── build.gradle                        # Dependencies, SDK versions, KSP config
│   └── src/main/
│       ├── AndroidManifest.xml             # Permissions, activities, receivers, services
│       └── java/com/smarttask/
│           ├── SmartTaskApp.kt             # Application class — Hilt init, notification channels,
│           │                               #   WorkManager config
│           ├── database/
│           │   ├── AppDatabase.kt          # Room database definition (tasks + chat_messages)
│           │   ├── dao/
│           │   │   ├── TaskDao.kt          # All task queries (CRUD, search, stats, recurring)
│           │   │   └── ChatMessageDao.kt   # Chat message persistence
│           │   └── entities/
│           │       ├── TaskEntity.kt       # Task data model (15+ fields) + Priority,
│           │       │                       #   Category, Recurring enums
│           │       └── ChatMessageEntity.kt# Chat message data model
│           │
│           ├── repository/
│           │   └── TaskRepository.kt       # Single source of truth — coordinates DB,
│           │                               #   alarms, AI, chat, stats, recurring generation
│           │
│           ├── viewmodel/
│           │   ├── TaskViewModel.kt        # Tasks MVVM — CRUD, search, calendar, AI parse/prioritize
│           │   └── AssistantViewModel.kt   # Chat MVVM — send message, auto-create tasks,
│           │                               #   conversation context
│           │
│           ├── ui/
│           │   ├── MainActivity.kt         # Host activity — NavHostFragment + BottomNavigationView
│           │   ├── tasks/
│           │   │   ├── TasksFragment.kt    # Task list with filters, search, swipe-to-delete
│           │   │   └── TasksAdapter.kt     # RecyclerView adapter with priority colors
│           │   ├── calendar/
│           │   │   └── CalendarFragment.kt # Monthly calendar with task dots
│           │   ├── assistant/
│           │   │   ├── AssistantFragment.kt# AI chat interface
│           │   │   └── ChatAdapter.kt      # Chat bubble RecyclerView adapter
│           │   ├── settings/
│           │   │   └── SettingsFragment.kt # Dark mode, notifications, AI toggle, analytics
│           │   ├── add_edit/
│           │   │   └── AddEditTaskFragment.kt  # Full task form — AI NLP input, voice,
│           │   │                               #   date/time pickers, category/priority
│           │   └── alarm/
│           │       └── AlarmActivity.kt    # Full-screen alarm — clock, animated bell,
│           │                               #   Snooze/Dismiss/Done
│           │
│           ├── service/
│           │   └── AlarmSoundService.kt    # Foreground service — MediaPlayer + Vibrator
│           │
│           ├── receiver/
│           │   ├── AlarmReceiver.kt        # BroadcastReceiver — alarm trigger + BOOT_COMPLETED
│           │   └── NotificationActionReceiver.kt  # Handles Done/Snooze/Dismiss from notification
│           │
│           ├── utils/
│           │   ├── GeminiAIService.kt      # Gemini 1.5 Flash API wrapper — NLP parse, chat,
│           │   │                           #   prioritization, key resolution
│           │   ├── AlarmScheduler.kt       # Exact alarm scheduling — schedule, snooze, cancel,
│           │   │                           #   reschedule all after reboot
│           │   ├── NotificationHelper.kt   # Notification builder with action buttons
│           │   └── PreferencesManager.kt   # EncryptedSharedPreferences — API key, dark mode,
│           │                               #   AI toggle, notification toggle
│           │
│           ├── widget/
│           │   └── TasksWidgetProvider.kt  # Home screen widget — today's top 3 tasks
│           │
│           ├── worker/
│           │   └── RecurringTaskWorker.kt  # WorkManager job — generates daily/weekly task
│           │                               #   instances + reschedules alarms
│           │
│           └── di/
│               └── AppModule.kt           # Hilt @Module — provides DB, DAOs, HTTP client,
│                                          #   Gson, PreferencesManager
│
└── res/
    ├── layout/                            # All XML layouts (fragments, activities, items, widget)
    ├── navigation/nav_graph.xml           # Navigation Component graph
    ├── menu/bottom_nav_menu.xml           # Bottom navigation items
    ├── anim/                              # Slide transitions + pulse animation
    ├── drawable/                          # Icons, backgrounds, shapes
    ├── font/                              # Custom fonts
    ├── color/                             # Color state lists
    ├── values/
    │   ├── colors.xml                     # Full dark/light palette (50+ colors)
    │   ├── themes.xml                     # Material 3 day/night themes
    │   └── strings.xml                    # All UI strings
    └── xml/                               # Widget info, backup rules, file paths, data extraction
```

---

## ⏰ Alarm System Deep Dive

The alarm system is a multi-component pipeline that ensures reliable, exact delivery:

```
┌──────────────────┐
│   Task Saved     │
│  (Repository)    │
└────────┬─────────┘
         ▼
┌──────────────────────────────────────┐
│  AlarmScheduler.scheduleAlarm()      │
│  AlarmManager.setExactAndAllowWhile  │
│  Idle(RTC_WAKEUP, triggerMs, PI)     │
└────────┬─────────────────────────────┘
         ▼  [time arrives]
┌──────────────────────────────────────┐
│  AlarmReceiver.onReceive()           │
│  (BroadcastReceiver)                 │
├──────────────────────────────────────┤
│  ├─ Starts AlarmSoundService         │
│  │  (foreground: MediaPlayer +       │
│  │   Vibrator)                       │
│  │                                   │
│  ├─ Launches AlarmActivity           │
│  │  (full-screen, over lock screen)  │
│  │                                   │
│  └─ Posts notification with actions  │
│     [Snooze] [Dismiss] [Done]        │
└────────┬─────────────────────────────┘
         ▼  [user interacts]
┌──────────────────────────────────────┐
│  User Action:                        │
│  • Snooze → reschedule +10 min       │
│  • Dismiss → stop sound, keep task   │
│  • Done → mark complete, stop sound  │
└──────────────────────────────────────┘
```

### Android 12+ Exact Alarm Permission

On Android 12+ (API 31+), the app needs the `SCHEDULE_EXACT_ALARM` permission. If not granted, it falls back gracefully:

1. **Exact alarm** (`setExactAndAllowWhileIdle`) → if `canScheduleExactAlarms()` returns true
2. **Inexact allow-while-idle** (`setAndAllowWhileIdle`) → fallback
3. **Standard alarm** (`set`) → final fallback on `SecurityException`

To grant manually:
```
Settings → Apps → SmartTask AI → Alarms & Reminders → Allow
```

---

## 🤖 AI Features Explained

### How It Works

SmartTask AI communicates with the **Gemini 1.5 Flash** API via Retrofit + OkHttp. All prompts are crafted to return structured JSON, and responses are parsed with Gson.

### 1. Natural Language Task Creation

**Input:** `"Remind me to take medicine every day at 8 AM"`

**AI extracts:**
```json
{
  "title": "Take medicine",
  "description": "",
  "date": "2026-04-05",
  "time": "08:00",
  "priority": "medium",
  "category": "health",
  "recurring": "daily",
  "recurring_custom": ""
}
```

**Context clues the AI understands:**
- `medicine / doctor / gym` → Health category
- `meeting / deadline / project` → Work category
- `study / exam / homework` → Study category
- `urgent / asap / important` → High priority

### 2. Voice Input

1. Tap the 🎤 mic icon on the Add Task screen
2. Speak your task description
3. Android Speech-to-Text transcribes → AI parses automatically
4. Fields auto-populate in the form

### 3. AI Chat Assistant

The assistant receives:
- Your current **pending tasks** (up to 10) for context
- The last **6 conversation turns** for continuity
- Today's date for temporal reasoning

Example commands:
- *"What tasks do I have today?"*
- *"Add a workout session for Saturday morning"*
- *"What's my most urgent task?"*
- *"Reschedule my meeting to Friday at 3 PM"*

Tasks mentioned in chat are **auto-created** and saved to the database with alarms scheduled.

### 4. AI Task Prioritization

Navigate to **Settings → AI Re-Prioritize**:
- Gemini receives up to 20 pending tasks with their titles, priorities, due dates, and categories
- Each task is scored **0.0 – 1.0** (1.0 = most urgent)
- Scores are saved to the database (`ai_priority_score` column)

---

## 📦 Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| **Kotlin** | 1.9.22 | Language |
| **Android Gradle Plugin** | 8.2.0 | Build system |
| **KSP** | 1.9.22-1.0.17 | Kotlin Symbol Processing (Room, Hilt) |
| **Room** | 2.6.1 | Local SQLite database (offline-first) |
| **Hilt** | 2.50 | Dependency injection |
| **Navigation Component** | 2.7.6 | Fragment navigation + safe args |
| **Lifecycle (ViewModel + LiveData)** | 2.7.0 | MVVM reactive streams |
| **WorkManager** | 2.9.0 | Background recurring task generation |
| **Coroutines** | 1.7.3 | Asynchronous programming |
| **Retrofit** | 2.9.0 | HTTP client for Gemini API |
| **OkHttp** | 4.12.0 | HTTP transport + logging interceptor |
| **Gson** | 2.10.1 | JSON serialization / deserialization |
| **Kizitonwose Calendar** | 2.5.0 | Beautiful monthly calendar view |
| **Lottie** | 6.3.0 | Animations |
| **Glide** | 4.16.0 | Image loading |
| **Glance** | 1.0.0 | Home screen widget (Jetpack) |
| **DataStore Preferences** | 1.0.0 | Modern key-value storage |
| **Security Crypto** | 1.1.0-alpha06 | EncryptedSharedPreferences (AES-256-GCM) |
| **Biometric** | 1.1.0 | Biometric authentication (optional) |
| **Material 3** | 1.11.0 | UI component library |

---

## 🔒 Security

SmartTask AI takes security seriously:

| Concern | Implementation |
|---------|---------------|
| **API Key Storage** | Stored in `EncryptedSharedPreferences` using AES-256-GCM encryption via AndroidX Security Crypto |
| **No Plaintext Secrets** | API keys are never hardcoded in source. Default `build.gradle` value is a placeholder |
| **Runtime Permissions** | `POST_NOTIFICATIONS` (Android 13+), `RECORD_AUDIO`, `SCHEDULE_EXACT_ALARM` — all requested at runtime |
| **Backup Exclusion** | `backup_rules.xml` and `data_extraction_rules.xml` exclude sensitive data from cloud backup |
| **Network Security** | `android:usesCleartextTraffic="false"` — HTTPS only |
| **Key Resolution Order** | Explicit param → Runtime (Settings) → BuildConfig fallback |

---

## 🏗️ Build APK

### Debug APK

```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release APK (Signed)

```bash
# 1. Generate a keystore (one-time):
keytool -genkey -v -keystore smarttask.keystore \
  -alias smarttask -keyalg RSA -keysize 2048 -validity 10000

# 2. Add signing config to app/build.gradle:
android {
    signingConfigs {
        release {
            storeFile file("smarttask.keystore")
            storePassword "YOUR_STORE_PASSWORD"
            keyAlias "smarttask"
            keyPassword "YOUR_KEY_PASSWORD"
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
        }
    }
}

# 3. Build:
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

> [!WARNING]
> Never commit your keystore or passwords to version control. Store them in `local.properties` or CI/CD secrets.

---

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| **Alarm not firing** | Check exact alarm permission: Settings → Apps → SmartTask AI → Alarms & Reminders → Allow |
| **AI not responding** | Verify your Gemini API key is correct in Settings → AI Features. Ensure internet connectivity |
| **Voice input missing** | Grant `RECORD_AUDIO` permission when prompted, or enable it in device Settings → App Permissions |
| **Build fails** | Run `./gradlew clean` then rebuild. Verify JDK 17 is configured in Android Studio |
| **Room migration error** | Clear app data (`Settings → Apps → SmartTask AI → Storage → Clear Data`) or increment the DB version in `AppDatabase.kt` |
| **Notifications not showing** | Grant `POST_NOTIFICATIONS` permission (required on Android 13+). Check Settings → Notifications |
| **Widget not updating** | Remove and re-add the widget. Ensure the app is not battery-optimized (Settings → Battery) |
| **Black screen on launch** | Clear app data and cache. If persists, check logcat for `PreferencesManager` or nav graph errors |
| **API key not saving** | Make sure you're entering a new key (not masked dots `•••`) and tapping the Save button |
| **Snooze not working** | Verify exact alarm permission is granted. Snooze uses a separate PendingIntent with offset request code |

---

## 🗺️ Roadmap

- [ ] **Firebase Firestore** cloud sync across devices
- [ ] **Biometric lock** — fingerprint / face unlock for app access
- [ ] **Rich notifications** — media-style with progress, images
- [ ] **Google Calendar** integration — two-way sync
- [ ] **Task sharing** — collaborative to-do lists
- [ ] **Analytics dashboard** — charts for productivity trends (daily, weekly, monthly)
- [ ] **Multiple AI models** — support for OpenAI GPT, Anthropic Claude alongside Gemini
- [ ] **Location-based reminders** — geofence-triggered task alerts
- [ ] **Task attachments** — photos, documents, voice memos
- [ ] **Drag-and-drop reordering** in task list
- [ ] **Sub-tasks** — break tasks into smaller steps with progress tracking

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. **Fork** the repository
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit your changes**: `git commit -m 'Add amazing feature'`
4. **Push to the branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request**

### Development Guidelines

- Follow **Kotlin coding conventions** and the existing code style
- Use **Hilt** for all dependency injection — avoid manual instantiation
- All data access goes through `TaskRepository` — never call DAOs directly from UI
- New UI elements should follow the **Material 3** design language
- Write meaningful **KDoc** comments for public classes and functions
- Test alarm-related changes on both **pre-API 31** and **API 31+** devices

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- [Google Gemini AI](https://ai.google.dev/) — Natural language understanding and conversational AI
- [Kizitonwose Calendar](https://github.com/kizitonwose/Calendar) — Beautiful Android calendar view
- [Lottie](https://airbnb.io/lottie/) — Lightweight animations
- [Dagger Hilt](https://dagger.dev/hilt/) — Dependency injection for Android
- [Material Design 3](https://m3.material.io/) — Google's design system

---

<div align="center">

**SmartTask AI** — Built with ❤️ using Kotlin, Jetpack, Material 3, and Gemini AI

*Manage smarter. Achieve more.*

</div>
]]>
