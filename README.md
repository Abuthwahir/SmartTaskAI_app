# 🧠 SmartTask AI

### AI-Powered Task Management & Alarm System for Android

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-API%2026+-34A853?logo=android&logoColor=white)](https://developer.android.com/)
[![Material 3](https://img.shields.io/badge/Material%203-1.11-757575?logo=material-design&logoColor=white)](https://m3.material.io/)
[![Hilt](https://img.shields.io/badge/Hilt-2.50-4285F4?logo=google&logoColor=white)](https://dagger.dev/hilt/)
[![Gemini AI](https://img.shields.io/badge/Gemini%20AI-1.5%20Flash-8E75B2?logo=google&logoColor=white)](https://ai.google.dev/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📌 Overview

**SmartTask AI** is a production-ready Android application that combines intelligent task management with a powerful alarm system.

It leverages **Google Gemini 1.5 Flash** to:

- Understand natural language
- Auto-create tasks from voice/text
- Provide contextual AI conversations
- Prioritize tasks intelligently

---

## ✨ Features

### 🤖 AI-Powered Intelligence

| Feature | Description |
|---------|-------------|
| Natural Language Parsing | Convert text/voice into structured tasks |
| Voice Input | Speak tasks directly via 🎤 mic |
| Conversational Assistant | Chat-based task creation & queries |
| Smart Prioritization | AI-based urgency scoring (0.0–1.0) |

---

### 📋 Task Management

- Create / Edit / Delete tasks
- Priority levels: High 🔴 / Medium 🟡 / Low 🟢
- Categories: General, Work, Health, Study, Personal
- Recurring tasks (Daily, Weekly, Custom)
- Search & filtering
- Swipe-to-delete with undo
- Overdue highlighting
- Completion analytics

---

### 📅 Calendar View

- Monthly calendar UI (Kizitonwose Calendar)
- Task indicator dots on dates
- Tap date → view tasks
- Smooth month navigation

---

### ⏰ Alarm System

- Exact alarms (`setExactAndAllowWhileIdle`)
- Full-screen alarm UI over lock screen
- Foreground sound service (MediaPlayer + Vibrator)
- Actions: Snooze / Dismiss / Done
- Notification action buttons
- Boot persistence (auto-reschedule after reboot)

---

### 🏠 Widget

- Shows today's top 3 tasks
- Quick access to app
- Auto updates

---

### ⚙️ Settings

- Dark / Light mode
- Notifications toggle
- AI toggle + API key
- Analytics dashboard
- Encrypted storage (AES-256-GCM)

---

## 📱 Screens

| Screen | Description |
|--------|-------------|
| Tasks | List, filters, search, swipe-to-delete |
| Calendar | Monthly view with task dots |
| Add/Edit | AI input + full task form |
| Assistant | Chat interface with context |
| Settings | Preferences + analytics |
| Alarm | Full-screen alarm with actions |
| Widget | Home screen widget |

---

## 🚀 Quick Start

### Prerequisites

| Tool | Version |
|------|---------|
| Android Studio | Hedgehog (2023.1.1)+ |
| JDK | 17+ |
| Android SDK | API 26–34 |
| Kotlin | 1.9.22 |
| Gradle | 8.2+ |

---

### 1. Clone

```bash
git clone https://github.com/Abuthwahir/SmartTaskAI_app.git
cd SmartTaskAI_app
```

---

### 2. Configure

```bash
cp local.properties.example local.properties
```

```properties
sdk.dir=YOUR_SDK_PATH
GEMINI_API_KEY=your_key
```

> **Note:** You can also set the API key in-app via **Settings → AI Features → Enter API Key**.

---

### 3. Build

```bash
./gradlew build
```

---

### 4. Run

```bash
./gradlew installDebug
```

---

## 🏗️ Architecture

```
UI → ViewModel → Repository → Data Sources
```

### Layers

| Layer | Role |
|-------|------|
| UI | Fragments & Activities |
| ViewModel | State & logic (LiveData) |
| Repository | Single source of truth |
| Data | Room, Retrofit, AlarmManager |

---

### Tech Stack

- MVVM Architecture
- Room Database
- Retrofit + OkHttp
- Hilt Dependency Injection
- Coroutines
- WorkManager
- Navigation Component

---

## 📁 Project Structure

```
SmartTaskAI/
├── app/
│   ├── build.gradle
│   └── src/main/java/com/smarttask/
│       ├── SmartTaskApp.kt          # Application class
│       ├── database/                # Room DB, DAOs, Entities
│       ├── repository/              # TaskRepository
│       ├── viewmodel/               # TaskViewModel, AssistantViewModel
│       ├── ui/                      # Fragments & Activities
│       ├── service/                 # AlarmSoundService
│       ├── receiver/                # AlarmReceiver, NotificationReceiver
│       ├── utils/                   # GeminiAIService, AlarmScheduler
│       ├── widget/                  # Home screen widget
│       ├── worker/                  # RecurringTaskWorker
│       └── di/                      # Hilt AppModule
└── res/                             # Layouts, navigation, themes, colors
```

---

## ⏰ Alarm Flow

```
Task Saved → AlarmScheduler → AlarmManager → AlarmReceiver → Service + AlarmActivity
```

User actions on alarm:

- **Snooze** → Reschedule +10 min
- **Dismiss** → Stop sound, task stays pending
- **Done** → Mark complete, stop sound

---

## 🤖 AI System

### Input

> "Remind me to take medicine every day at 8 AM"

### Output

```json
{
  "title": "Take medicine",
  "time": "08:00",
  "category": "health",
  "recurring": "daily"
}
```

### Capabilities

- NLP task parsing from text/voice
- Chat assistant with conversation context
- Context-aware (knows your pending tasks)
- Task prioritization scoring (0.0–1.0)

---

## 📦 Dependencies

| Library | Purpose |
|---------|---------|
| Room 2.6 | Local database |
| Hilt 2.50 | Dependency injection |
| Retrofit 2.9 | Gemini API calls |
| Coroutines 1.7 | Async operations |
| WorkManager 2.9 | Background tasks |
| Navigation 2.7 | Fragment navigation |
| Lottie 6.3 | Animations |
| Glance 1.0 | Home screen widget |
| Material 3 | UI components |
| Security Crypto | Encrypted preferences |

---

## 🔒 Security

- EncryptedSharedPreferences (AES-256-GCM)
- No hardcoded secrets in source
- HTTPS only (`usesCleartextTraffic=false`)
- Runtime permissions for notifications, mic, alarms

---

## 🏗️ Build APK

### Debug

```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release

```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

---

## 🐛 Troubleshooting

| Issue | Fix |
|-------|-----|
| Alarm not working | Enable exact alarm permission in device settings |
| AI not responding | Check API key in Settings → AI Features |
| Build fails | Run `./gradlew clean` then rebuild |
| Notifications missing | Grant POST_NOTIFICATIONS permission (Android 13+) |
| Black screen | Clear app data and cache |

---

## 🗺️ Roadmap

- [ ] Firebase cloud sync
- [ ] Biometric lock
- [ ] Google Calendar integration
- [ ] Task sharing & collaboration
- [ ] Analytics dashboard with charts
- [ ] Multi-AI support (OpenAI, Anthropic)
- [ ] Location-based reminders
- [ ] Task attachments
- [ ] Subtasks

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-feature`
3. Commit your changes: `git commit -m "Add feature"`
4. Push to the branch: `git push origin feature/new-feature`
5. Open a Pull Request

---

## 📄 License

MIT License — Owner **Abuthwahir**

---

## 🙏 Acknowledgments

- [Google Gemini AI](https://ai.google.dev/)
- [Kizitonwose Calendar](https://github.com/kizitonwose/Calendar)
- [Lottie](https://airbnb.io/lottie/)
- [Dagger Hilt](https://dagger.dev/hilt/)
- [Material Design 3](https://m3.material.io/)

---

<div align="center">

**SmartTask AI** — Built with ❤️ using Kotlin, Jetpack, Material 3, and Gemini AI
*Built by Abuthwahir H M*
*Manage smarter. Achieve more.*

</div>
