<div align="center">

![YaFeed](https://socialify.git.ci/1shin-7/YaFeed/image?description=1&font=Raleway&language=1&name=1&owner=1&pattern=Plus&theme=Auto)

# YaFeed

[![Release](https://img.shields.io/github/v/release/1shin-7/YaFeed?style=for-the-badge&logo=github)](https://github.com/1shin-7/YaFeed/releases/latest)
[![Dev Build](https://img.shields.io/github/actions/workflow/status/1shin-7/YaFeed/dev.yml?branch=master&style=for-the-badge&logo=github-actions&label=Dev%20Build)](https://github.com/1shin-7/YaFeed/actions/workflows/dev.yml)
[![License](https://img.shields.io/github/license/1shin-7/YaFeed?style=for-the-badge)](LICENSE)

> **Why YaFeed?** Existing RSS readers lack proper image support, suffer from outdated UI design, poor performance, and layout issues. YaFeed was built to address these problems with a modern, high-performance solution for both Android mobile and Wear OS.

A modern RSS feed reader for Android Mobile and Wear OS with image support, beautiful UI, and optimized performance.

</div>

## ✨ Features

- 📱 **Dual Platform**: Native support for Android Mobile and Wear OS
- 🖼️ **Image Support**: Display images in RSS feeds with cloud storage integration
- 🎨 **Modern UI**: Material 3 design with Wear OS optimized components
- ⚡ **High Performance**: Optimized startup, scrolling, and image loading
- 🔄 **Bidirectional Sync**: Mobile-Wear synchronization with connection detection
- 📝 **Markdown Rendering**: Rich content display with custom Wear OS renderer

## 🚀 Getting Started

### Download

[![Mobile APK](https://img.shields.io/badge/Download-Mobile%20APK-blue?style=for-the-badge&logo=android)](https://github.com/1shin-7/YaFeed/releases/latest)
[![Wear APK](https://img.shields.io/badge/Download-Wear%20OS%20APK-green?style=for-the-badge&logo=wear-os)](https://github.com/1shin-7/YaFeed/releases/latest)

Download the latest release from the [Releases](https://github.com/1shin-7/YaFeed/releases) page.

**Requirements:**
- Mobile: Android 9.0 (API 28) or higher
- Wear: Wear OS 3.0 (API 28) or higher

### Installation

1. Download the appropriate APK for your device
2. Enable "Install from unknown sources" in your device settings
3. Install the APK
4. Grant necessary permissions

## 🛠️ Development

### Prerequisites

- JDK 17 or higher
- Android Studio Ladybug or later
- Android SDK with API 36

### Build

```bash
# Clone the repository
git clone https://github.com/1shin-7/YaFeed.git
cd YaFeed

# Build mobile debug APK
./gradlew :mobile:assembleDebug

# Build wear debug APK
./gradlew :wear:assembleDebug

# Build both release APKs
./gradlew assembleRelease
```

### Project Structure

```
YaFeed/
├── mobile/          # Android mobile app module
├── wear/            # Wear OS app module
└── libs/            # Local library dependencies
```

## 📦 Tech Stack

- **Language**: Kotlin 2.3.10
- **UI**: Jetpack Compose + Wear Compose Material 3
- **Architecture**: MVVM with Room + DataStore
- **Networking**: RSS Parser + Coil for images
- **Build**: AGP 9.1.0, Gradle 8.x

## 🙏 Credits

### Libraries

- [RSS-Parser](https://github.com/prof18/RSS-Parser) - RSS feed parsing library
- [multiplatform-markdown-renderer](https://github.com/mikepenz/multiplatform-markdown-renderer) - Markdown rendering for Compose
- [jetbrains/markdown](https://github.com/JetBrains/markdown) - Markdown parsing engine

### AI Agents

![Claude](https://img.shields.io/badge/Claude-D97757?style=for-the-badge&logo=claude&logoColor=white) Performance optimization and code refinement

![Gemini](https://img.shields.io/badge/Gemini-8E75B2?style=for-the-badge&logo=googlegemini&logoColor=white) UI layout design and component architecture

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

<div align="center">
Made with ❤️ for RSS enthusiasts
</div>
