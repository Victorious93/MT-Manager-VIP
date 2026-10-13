# FileForge

**An original, clean-room Android file manager and APK/text toolkit** (dual-pane browser, archives, editor, APK tools). It shares no code, assets, or branding with any other app.

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-Android-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
![Status: M0 skeleton](https://img.shields.io/badge/status-M0%20skeleton-orange)

> **Repo name note:** this GitHub repository is still named `MT-Manager-VIP` (forked from an archived upstream “backup” repo). The **product in-tree is FileForge** (`applicationId` `app.fileforge`). Consider renaming the repository to match.

## Overview

FileForge is planned as a dual-pane file manager with archive browsing, code editing, offline APK inspection/re-signing, media/hex viewers, network locations, terminal, and utilities — with no ads, telemetry, obfuscation, or anti-tamper.

**Status today: M0 — project skeleton.** Modules `:app`, `:core-fs`, `:core-ui`, `:core-data` exist; dual-pane browser and later milestones are not implemented yet. Design and milestone plan: [ARCHITECTURE.md](ARCHITECTURE.md).

## Features (planned vs present)

| Area | Status |
|---|---|
| Gradle KTS multi-module skeleton, Compose deps, CI | Present (M0) |
| Dual-pane browser, JobQueue, storage permission flow | Planned (M1+) |
| Archives, editor, APK tools, network, terminal, root | Planned (M2–M8) |

Non-goals (from architecture): no accessibility-service/overlay abuse; no bypassing other apps’ licensing; APK tools operate on user-owned APKs only.

## Screenshots

| Dual-pane browser | Editor | APK tools | Settings |
|---|---|---|---|
| *TODO (not built)* | *TODO* | *TODO* | *TODO* |

## Requirements

- JDK **17+**
- Android SDK: platform **36**, build-tools **35**
- `minSdk` 26, `targetSdk` 35, `applicationId` `app.fileforge`

## Build

```sh
git clone https://github.com/Victorious93/MT-Manager-VIP.git
cd MT-Manager-VIP
./gradlew lintDebug testDebugUnitTest assembleDebug
```

## Project structure

```text
app/          Application shell (Compose)
core-fs/      File-system abstractions (interfaces; M0 stub)
core-ui/      Shared UI / theme
core-data/    Room / DataStore placeholders
ARCHITECTURE.md   Goals, module graph, milestones M0–M9
.github/workflows   CI
```

## Contributing

Issues/PRs welcome once the repo identity is clarified (FileForge vs legacy fork name). Prefer small milestones aligned with [ARCHITECTURE.md](ARCHITECTURE.md). Keep the clean-room rule: no code or assets from third-party file-manager apps.

## License

[Apache License 2.0](LICENSE).
