# 🤝 Contributing to Rushd-ul-Ilm

> **بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيم** — *In the name of Allah, the Most Gracious, the Most Merciful*

Thank you for your interest in contributing to Rushd-ul-Ilm! This project serves Muslims with low digital literacy by providing voice-based access to authenticated Islamic knowledge. Every contribution — whether code, translation, testing, or documentation — directly helps real people learn about their religion.

---

## 📋 Table of Contents

- [Code of Conduct](#-code-of-conduct)
- [How to Contribute](#-how-to-contribute)
- [Development Setup](#-development-setup)
- [Coding Standards](#-coding-standards)
- [Islamic Content Rules](#-islamic-content-rules)
- [Privacy Requirements](#-privacy-requirements)
- [Pull Request Process](#-pull-request-process)
- [Areas Where Help is Needed](#-areas-where-help-is-needed)
- [Reporting Issues](#-reporting-issues)
- [Community](#-community)

---

## 📜 Code of Conduct

### Our Values
- **Respect** — Treat all contributors and users with the respect Islam teaches
- **Accuracy** — Islamic content must come only from authenticated scholar sources
- **Privacy** — User data must never leave the device or self-hosted server
- **Inclusivity** — No sectarian bias; the app serves all Muslims regardless of madhab
- **Patience** — The project founder is learning Android development; be supportive and educational

### Unacceptable Behavior
- Sectarian arguments or promoting one madhab over another as "more correct"
- Adding unverified Islamic rulings or fabricated hadith references
- Introducing telemetry, analytics, or cloud services that compromise privacy
- Disrespectful or dismissive comments toward contributors of any skill level

---

## 🔧 How to Contribute

### 1. Fork & Clone
```bash
git clone https://github.com/YOUR_USERNAME/rushdul-ilm.git
cd rushdul-ilm
```

### 2. Create a Feature Branch
```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/bug-description
```

### 3. Make Your Changes
Follow the [Coding Standards](#-coding-standards) below.

### 4. Test Your Changes
```bash
# Android app
cd android-app
./gradlew :app:compileDebugKotlin  # Must pass!

# Backend
python3 -m py_compile backend/your_file.py  # Must pass!
```

### 5. Submit a Pull Request
- Write a clear description of what changed and why
- Reference any related issues
- Include screenshots for UI changes

---

## 🛠️ Development Setup

### Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| Android Studio | Panda 4 (2025.3.4+) | Android IDE |
| JDK | 21 (bundled with Android Studio) | Build system |
| Kotlin | 2.x | Android app language |
| Python | 3.11+ | Backend services |
| Docker + Compose | Latest | Service orchestration |
| NVIDIA Container Toolkit | Latest | GPU access in Docker |
| NVIDIA GPU | 4GB+ VRAM | AI model inference |
| Linux OS | Any (Parrot OS recommended) | Development environment |

### Android Setup
```bash
# Set JAVA_HOME to Android Studio's bundled JDK
export JAVA_HOME=/path/to/android-studio/jbr

# Build the app
cd android-app
./gradlew assembleDebug
```

### Backend Setup
```bash
# Start the backend services
cd backend
docker compose up -d

# Verify
curl http://localhost:8000/health
```

### Important Paths
- Android package: `com.rushdululilm.app` (**never change this**)
- Minimum SDK: API 31 (Android 12)
- Target SDK: API 34 (Android 14)

---

## 📐 Coding Standards

### Rule 1: Every Line Must Have a Comment

This is the **most important rule** in this project. The developer and community members are learning — every single line of code must have a beginner-friendly comment.

```kotlin
// ✅ CORRECT — beginner-friendly comments
val micButton = remember { mutableStateOf(false) }
// ^ 'remember' keeps this value alive across Compose redraws
// ^ 'mutableStateOf(false)' creates a true/false toggle, starts as OFF
```

```python
# ✅ CORRECT — Python style
qdrant_client = QdrantClient(host="localhost", port=6333)
# ^ QdrantClient is the Python library that talks to the Qdrant vector database
# ^ port=6333 is the default port that Qdrant listens on
```

```kotlin
// ❌ WRONG — no comments
val micButton = remember { mutableStateOf(false) }
```

### Rule 2: Jetpack Compose Only
- **No XML layouts** — everything is in Kotlin Compose
- Use `@Composable` functions for UI
- Use `MaterialTheme` from the app's custom theme

### Rule 3: MVVM Architecture
- **Screens** → call **ViewModels** → call **Repositories** → call **Data Sources**
- Never access the network or database directly from a Screen
- Use `StateFlow` for all observable state
- Use `Hilt` for dependency injection

### Rule 4: String Resources
- All user-facing text goes in `res/values/strings.xml`
- Never hardcode strings in Kotlin code
- Add translations for all supported languages: `values/`, `values-te/`, `values-ur/`, `values-hi/`

### Rule 5: UI Accessibility
- Minimum **16sp** font size for all text
- Minimum **48dp** touch targets for all interactive elements
- Bilingual labels (local language + English) for all UI text
- Content descriptions for all interactive elements

### Rule 6: File Headers
Every code file must start with:
```kotlin
// File: [filename]
// Purpose: [what this file does in one sentence]
// Layer: [which of the 6 layers this belongs to]
// Depends on: [other files this imports from]
// Created: [date] | Modified: [date]
// Developer: [your name]
```

### Rule 7: No Magic Numbers
```kotlin
// ❌ Wrong
Thread.sleep(200)

// ✅ Correct
val LAN_PING_TIMEOUT_MS = 200L
Thread.sleep(LAN_PING_TIMEOUT_MS)
```

---

## 🕌 Islamic Content Rules

> **These rules are absolute and non-negotiable.**

### Rule 1: No AI-Generated Islamic Rulings
The LLM is **never** allowed to generate Islamic rulings from its own training data. The RAG prompt strictly says:
> *"Answer ONLY from the provided context. Do NOT use your own knowledge."*

### Rule 2: Source URLs Are Mandatory
Every answer displayed in the app **must** include the original source URL. No source = the answer cannot be displayed.

### Rule 3: Approved Sources Only
| Source | URL | Madhab |
|--------|-----|--------|
| IslamQA.info | https://islamqa.info/en | Neutral |
| IslamQA.org | https://islamqa.org | Neutral |
| Darul Ifta Deoband | https://darulifta-deoband.com/en | Hanafi |

**Do NOT add new Islamic sources without explicit maintainer approval.** Open an issue to discuss new sources first.

### Rule 4: Fallback Message
If the RAG pipeline finds no relevant answer, the app must display:
> *"I could not find an answer in the approved Islamic sources. Please consult a qualified Islamic scholar."*

— translated to the user's language and read aloud by TTS.

---

## 🔒 Privacy Requirements

### Absolute Prohibitions
- ❌ **No Google Analytics** or Firebase Analytics
- ❌ **No Crashlytics** or any external crash reporter
- ❌ **No cloud LLM APIs** (OpenAI, Anthropic, Google Gemini API)
- ❌ **No third-party SDKs** that collect user data
- ❌ **No telemetry** of any kind

### What Must Stay Local
- ✅ Voice recordings — processed on-device or self-hosted server only
- ✅ User questions — never sent to any cloud service
- ✅ Query history — stored only in local Room database
- ✅ All AI inference — local GPU (Ollama) or NVIDIA NIM API only

---

## 📤 Pull Request Process

1. **Ensure your code compiles**: Run `./gradlew :app:compileDebugKotlin` for Android changes
2. **Follow coding standards**: Every line must have a comment
3. **Update documentation**: If you create a new file, document it in the Report Documentation
4. **Test on emulator**: If making UI changes, verify on the Android emulator
5. **Write a clear PR description**:
   - What does this PR do?
   - Which files were created/modified?
   - Screenshots for UI changes
   - Any new dependencies added?

### PR Title Format
```
[Phase X] Brief description of change

Examples:
[Phase 5] Add offline translation using Opus-MT ONNX
[Phase 4] Fix null pointer in answer screen
[Docs] Update architecture diagram
[Bug] Fix Hindi repetition loop in translation
```

---

## 🎯 Areas Where Help is Needed

| Area | Skill Level | Description |
|------|-------------|-------------|
| 🌍 **Translation** | Beginner | Translate UI strings into Indian languages (Kannada, Tamil, Malayalam) |
| 🕌 **Islamic Review** | Domain Expert | Validate fatwa source accuracy and completeness |
| 📱 **Android UI/UX** | Intermediate | Improve screen designs, animations, accessibility |
| 🐍 **Python Backend** | Intermediate | Optimize RAG pipeline, reduce latency |
| 🎤 **Audio/STT** | Advanced | Improve whisper.cpp JNI integration and audio quality |
| 📝 **Documentation** | Beginner | Improve setup guides, API documentation, user guides |
| 🧪 **Testing** | Beginner | Test on various Android devices, report bugs |
| 🎨 **Design** | Intermediate | App icon, splash screen, promotional graphics |
| 🌐 **Deployment** | Advanced | Help with VPS deployment, CI/CD pipeline |

---

## 🐛 Reporting Issues

### Bug Report Template
```markdown
**Description**: [What went wrong?]
**Steps to Reproduce**:
1. [Step 1]
2. [Step 2]
3. [What happened]

**Expected Behavior**: [What should have happened]
**Device**: [Phone model, Android version, or "Emulator"]
**Screenshots**: [Attach if relevant]
**Logs**: [Paste relevant logcat output]
```

### Feature Request Template
```markdown
**Feature**: [Brief title]
**Problem**: [What problem does this solve?]
**Who Benefits**: [Which user type — illiterate users, Telugu readers, etc.]
**Proposed Solution**: [How would this work?]
**Alternatives Considered**: [Other approaches you thought about]
```

---

## 💬 Community

- **GitHub Issues**: Report bugs, request features
- **GitHub Discussions**: Ask questions, share ideas, get help
- **Pull Requests**: Contribute code, translations, documentation

---

## 🙏 Thank You

Every contribution to Rushd-ul-Ilm helps bridge the digital divide for Muslims who lack access to authentic Islamic knowledge. Whether you fix a typo, translate a string, or build a new feature — you're making a difference.

**May Allah reward your efforts. جزاك الله خيرا**

---

> **Navigation**: [← README](README.md) | [Architecture →](docs/ARCHITECTURE.md) | [Development Status →](docs/DEVELOPMENT_STATUS.md)

