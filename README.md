# FileForge

**A clean-room Android file manager and APK/text toolkit.**

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)
![Min SDK 26](https://img.shields.io/badge/minSdk-26-3DDC84?logo=android&logoColor=white)
![Status: early development](https://img.shields.io/badge/status-early_development-orange)

## Overview

FileForge is an original Android app built with Kotlin, Jetpack Compose and Material 3. It shares no code, assets, or branding with any other app, and it has no ads, telemetry, obfuscation, or anti-tamper.

**Current status: M0, project skeleton.** The Gradle multi-module build, CI, and an app shell exist. The file browser and the other features below are not implemented yet. The design and milestone plan are in [ARCHITECTURE.md](ARCHITECTURE.md).

## Key features

Implemented today (M0):

- Gradle Kotlin DSL multi-module project (`:app`, `:core-fs`, `:core-ui`, `:core-data`) with a version catalog
- Jetpack Compose and Material 3 app shell with a shared theme
- GitHub Actions CI that runs lint, unit tests, and a debug build

Planned (see [ARCHITECTURE.md](ARCHITECTURE.md) for milestones):

- Dual-pane file browser with tabs, selection, sorting, and background file operations (M1)
- Archive browsing, extraction, and creation (M2)
- Text and code editor with syntax highlighting (M3)
- Media, PDF, and hex viewers, checksums, bookmarks, and search (M4)
- Offline APK inspection, re-signing with your own keystore, and zipalign (M5)
- Utilities such as compare, duplicate finder, and storage analyzer (M6)
- Terminal and opt-in root mode (M7)
- Network locations and an HTTP share server (M8)

Non-goals: no accessibility-service or overlay abuse, no bypassing other apps' licensing, and APK tools operate on APKs you own.

Screenshots coming soon.

## Install

There are no published releases yet. To try the current skeleton, build it from source (below) and install the debug APK on a device or emulator running Android 8.0 (API 26) or newer:

```sh
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Usage

The current build only launches an app shell that shows the FileForge name. File management features arrive with milestone M1 and later.

## Build from source

Requirements:

- JDK 17 or newer
- Android SDK with platform 36 and build-tools 35
- `minSdk` 26, `targetSdk` 35, `applicationId` `app.fileforge`

```sh
git clone https://github.com/Victorious93/MT-Manager-VIP.git
cd MT-Manager-VIP
./gradlew lintDebug testDebugUnitTest assembleDebug
```

## Project structure

```text
app/              Application shell (Compose)
core-fs/          File-system abstractions
core-ui/          Shared UI and theme
core-data/        Persistence module (Room and DataStore are planned)
ARCHITECTURE.md   Goals, module graph, milestones M0-M9
.github/workflows CI
```

## Contributing

Issues and pull requests are welcome. Prefer small changes aligned with the milestones in [ARCHITECTURE.md](ARCHITECTURE.md), and keep the clean-room rule: no code or assets from third-party file-manager apps.

## License

Licensed under the [Apache License 2.0](LICENSE).
