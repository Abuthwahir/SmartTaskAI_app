
````md
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
|--------|------------|
| Natural Language Parsing | Convert text/voice into structured tasks |
| Voice Input | Speak tasks directly |
| Conversational Assistant | Chat-based task creation & queries |
| Smart Prioritization | AI-based urgency scoring |

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

- Monthly calendar UI  
- Task indicators on dates  
- Tap date → view tasks  
- Smooth navigation  

---

### ⏰ Alarm System  

- Exact alarms (`setExactAndAllowWhileIdle`)  
- Full-screen alarm UI  
- Foreground sound service  
- Actions: Snooze / Dismiss / Done  
- Notification controls  
- Boot persistence  

---

### 🏠 Widget  

- Shows top 3 tasks  
- Quick access to app  
- Auto updates  

---

### ⚙️ Settings  

- Dark / Light mode  
- Notifications toggle  
- AI toggle + API key  
- Analytics dashboard  
- Encrypted storage  

---

## 📱 Screens  

| Screen | Description |
|-------|------------|
| Tasks | List, filters, search |
| Calendar | Monthly view |
| Add/Edit | AI input + form |
| Assistant | Chat interface |
| Settings | Preferences + analytics |
| Alarm | Full-screen alarm |
| Widget | Home widget |

---

## 🚀 Quick Start  

### Prerequisites  

| Tool | Version |
|------|--------|
| Android Studio | Hedgehog+ |
| JDK | 17+ |
| SDK | API 26–34 |
| Kotlin | 1.9.22 |
| Gradle | 8.2+ |

---

### 1. Clone  

```bash
git clone https://github.com/your-username/SmartTaskAI.git
cd SmartTaskAI
````

---

### 2. Configure

```bash
cp local.properties.example local.properties
```

```properties
sdk.dir=YOUR_SDK_PATH
GEMINI_API_KEY=your_key
```

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

| Layer      | Role                    |
| ---------- | ----------------------- |
| UI         | Fragments & Activities  |
| ViewModel  | State & logic           |
| Repository | Single source of truth  |
| Data       | Room, API, AlarmManager |

---

### Tech Stack

* MVVM Architecture
* Room Database
* Retrofit + OkHttp
* Hilt Dependency Injection
* Coroutines
* WorkManager

---

## 📁 Project Structure

```
SmartTaskAI/
 ├── app/
 │   ├── database/
 │   ├── repository/
 │   ├── viewmodel/
 │   ├── ui/
 │   ├── service/
 │   ├── receiver/
 │   ├── utils/
 │   ├── widget/
 │   ├── worker/
 │   └── di/
 └── res/
```

---

## ⏰ Alarm Flow

```
Task → Scheduler → AlarmManager → Receiver → Service + UI
```

User actions:

* Snooze
* Dismiss
* Done

---

## 🤖 AI System

### Input

"Remind me to take medicine every day at 8 AM"

### Output

```json
{
  "title": "Take medicine",
  "time": "08:00",
  "recurring": "daily"
}
```

---

### Capabilities

* NLP parsing
* Chat assistant
* Context awareness
* Task prioritization

---

## 📦 Dependencies

| Library     | Purpose          |
| ----------- | ---------------- |
| Room        | Database         |
| Hilt        | DI               |
| Retrofit    | API              |
| Coroutines  | Async            |
| WorkManager | Background tasks |
| Material 3  | UI               |

---

## 🔒 Security

* EncryptedSharedPreferences (AES-256)
* No hardcoded secrets
* HTTPS only
* Runtime permissions

---

## 🏗️ Build APK

### Debug

```bash
./gradlew assembleDebug
```

### Release

```bash
./gradlew assembleRelease
```

---

## 🐛 Troubleshooting

| Issue                 | Fix                 |
| --------------------- | ------------------- |
| Alarm not working     | Enable exact alarms |
| AI not responding     | Check API key       |
| Build fails           | Clean & rebuild     |
| Notifications missing | Enable permissions  |

---

## 🗺️ Roadmap

* Firebase sync
* Biometric lock
* Google Calendar integration
* Task sharing
* Analytics dashboard
* Multi-AI support
* Location reminders
* Attachments
* Subtasks

---

## 🤝 Contributing

```bash
git checkout -b feature/new-feature
git commit -m "Add feature"
git push origin feature/new-feature
```

---

## 📄 License

MIT License

---

## 🙏 Acknowledgments

* Google Gemini AI
* Kizitonwose Calendar
* Lottie
* Dagger Hilt
* Material Design

---

## ❤️ Final Note

**SmartTask AI**
*Manage smarter. Achieve more.*

```

---

### ✅ What I fixed:
- Removed broken HTML (`<![CDATA[`, `<div>`, etc.)
- Cleaned structure into proper Markdown hierarchy
- Standardized headings, spacing, and tables
- Made it GitHub-ready (professional README)
- Reduced clutter while keeping all key info

---

If you want next level upgrade, I can:
- Add **GIF demos / screenshots layout**
- Add **badges for downloads, stars, etc.**
- Make it **FAANG-level README (portfolio-ready)**
```
