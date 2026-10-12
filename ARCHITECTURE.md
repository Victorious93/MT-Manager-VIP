# Victorious-Manager — Architecture & Milestone Plan

Status: **Approved** for M0–M1 (owner replied "All yes" to the open questions in §10). Distribution channel (Play Store vs sideload/F-Droid) was not decided by that reply and remains open.

Victorious-Manager is an original, clean-room Android file manager and APK/text toolkit.
It shares no code, assets, strings or branding with any existing app.

## 1. Goals and non-goals

Goals: dual-pane file management, archive browsing, code editing, offline APK inspection/re-signing, media/hex viewers, network locations, terminal, utilities.

Non-goals (deliberate):
- No ads, telemetry, obfuscation or anti-tamper. Apache-2.0.
- No accessibility-service or overlay usage (Play policy; not needed).
- No bypassing of any app's licensing. APK tools operate on user-owned APKs only.

## 2. Stack

| Concern | Choice |
|---|---|
| Language/UI | Kotlin, Jetpack Compose, Material 3 |
| SDK | minSdk 26, targetSdk 35 |
| Async | Coroutines + Flow |
| Persistence | Room (bookmarks, history), DataStore (settings) |
| Build | Gradle Kotlin DSL, version catalog |
| DI | Hilt (assumption — alternative: manual DI; decide at M1) |
| CI | GitHub Actions: lint, unit tests, assembleDebug; signed release via secrets |

Library choices below are **candidates, not verified**; each must be checked for current version, license (Apache-2.0-compatible) and Android compatibility before adoption.

## 3. Module graph

```
:app ──► :feature-browser ──► :core-fs ◄── :core-archive
  │  ├─► :feature-editor ───► :core-fs
  │  ├─► :feature-apk ──────► :core-fs, :core-archive
  │  ├─► :feature-terminal
  │  ├─► :feature-network ──► :core-fs
  │  └─► :feature-tools (compare, dupes, analyzer, checksum, app manager)
  └─► :core-ui (theme, shared Compose components)
:core-data (Room, DataStore) ◄── :app, features
```

Rules: features never depend on each other; they depend only on `core-*`. `:app` wires navigation and DI.
The initial milestone creates `:app`, `:core-fs`, `:core-ui`, `:core-data`, `:feature-browser` only; other modules are added in their milestone.

## 4. Key interfaces (`:core-fs`)

```kotlin
sealed interface FsLocation { val uri: String }          // local path, SAF tree, archive entry, remote

interface FileProvider {
    suspend fun list(dir: FsLocation, page: PageRequest): Page<FsEntry>   // paged for 100k+ dirs
    suspend fun stat(loc: FsLocation): FsEntry
    fun openRead(loc: FsLocation): Source
    fun openWrite(loc: FsLocation, append: Boolean = false): Sink
    suspend fun mkdir(loc: FsLocation)
    suspend fun delete(loc: FsLocation)
    suspend fun rename(from: FsLocation, to: FsLocation)
}

interface FileOperation { val id: JobId; fun run(ctx: JobContext): Flow<Progress> }   // copy/move/delete/extract
interface ConflictResolver { suspend fun resolve(src: FsEntry, dst: FsEntry): Resolution } // Skip/Overwrite/Rename
```

Providers: `LocalFileProvider` (java.io/NIO, needs all-files access), `SafFileProvider` (fallback), `RootFileProvider` (opt-in, M-later), `ArchiveFileProvider` (`:core-archive`, read-only virtual folders first), remote providers (`:feature-network`).
A `ProviderRegistry` resolves the provider from the location scheme. This is what makes copy/move work uniformly across local, archive and remote.

## 5. Data flow

```
Compose UI ─intent─► ViewModel ─► UseCase ─► FileProvider / JobQueue
     ▲                    │                        │
     └──── StateFlow ◄────┴──── Flow<Progress> ◄───┘
```

- Each pane has its own `PaneState` (tabs, current location, selection, sort/filter) held in a ViewModel; panes are independent.
- Long operations run in a foreground `JobService` backed by a `JobQueue` (progress notification, pause/cancel); the UI observes job state via Flow.
- Main thread never performs IO; listing is paged (`Page<FsEntry>`, e.g. 500 entries) and diffed into LazyColumn.

## 6. Permissions strategy

- Request lazily, per feature, with a rationale screen first.
- Storage: `MANAGE_EXTERNAL_STORAGE` for full browsing; SAF fallback if denied.
  **Risk:** Play requires a policy declaration/justification for all-files access; a file manager is an allowed use case but approval is not guaranteed. Sideload/F-Droid distribution has no such gate.
- `REQUEST_INSTALL_PACKAGES`, `QUERY_ALL_PACKAGES`: only in `:feature-apk` / app manager, with the same Play-policy caveat.
- `INTERNET`: only in `:feature-network`.
- Root: strictly opt-in, off by default.

## 7. Feature notes and known risks

| Feature | Approach | Risk / uncertainty |
|---|---|---|
| Archives | zip/tar/gz/xz/bz2 via a vetted library (candidate: Apache Commons Compress); 7z likewise | Password-protected zip and split archives need a library that supports them (candidate: zip4j); verify license/maintenance |
| Editor | Custom Compose text surface or embedded editor view; syntax via own tokenizer rules | **Highest UI risk:** Compose has no mature large-file code editor. >50MB streaming mode likely needs a custom piece-table/rope. Prototype early |
| APK tools | Parse binary manifest + signatures; re-sign with user keystore; zipalign | Writing a correct binary XML decoder is nontrivial; evaluate existing Apache-licensed libs first. Modified APKs cannot update the original (different signature) |
| Media | Media3 for audio/video, PdfRenderer for PDF | Codec coverage varies by device |
| Terminal | PTY via native code (JNI) or existing terminal-emulator library | Native build adds NDK complexity; license check required |
| Network | SFTP (SSHJ/JSch fork), SMB (SMBJ), FTP (commons-net), WebDAV (OkHttp) | SMB/SFTP library licenses and maintenance need verification; mountable as virtual locations via `FileProvider` |
| Root | libsu | Behavior differs per root manager; test coverage limited without devices |
| Storage on Android 11+ | Direct File access to `Android/data` is restricted | Browsing other apps' `Android/data` may need SAF or root; cannot be fully guaranteed |

## 8. Testing strategy

- `:core-fs`, `:core-archive`: JVM unit tests with temp dirs (copy/move/conflict, paging, archive round-trips).
- ViewModels: unit tests with fake providers.
- UI: Compose UI tests for browse → select → copy to other pane → conflict dialog.
- Instrumented tests need an emulator; **not available in this cloud environment**, so UI tests will be written but may be unverified here. Each milestone report will state exactly what was built, run, or only written.

## 9. Milestones

| # | Scope | Exit criteria |
|---|---|---|
| **M0** | Repo skeleton: Gradle KTS, version catalog, modules `:app :core-fs :core-ui :core-data`, CI workflow, license, README | `./gradlew assembleDebug testDebugUnitTest lint` passes |
| **M1** | Dual-pane browser, `LocalFileProvider`, paged listing, tabs, selection, sort/filter/hidden, file ops via JobQueue + foreground service, conflict dialog, storage-permission flow, settings (theme) | Unit tests green; manual checklist documented; user-manual sections for the new screens written in `docs/USER_MANUAL.md` |
| M2 | Archives as virtual folders (read), extract/create, passwords | Round-trip tests |
| M3 | Text/code editor + syntax highlighting + find/replace | Editor prototype benchmark on 50MB file |
| M4 | Media/PDF/hex viewers, checksum, bookmarks, recents, search | |
| M5 | APK info/browse/re-sign/zipalign, app manager | Signed output verified with `apksigner` |
| M6 | Utilities: compare, dupes, storage analyzer | |
| M7 | Terminal, root mode (opt-in) | |
| M8 | Network providers, HTTP share server + QR | |
| M9 | i18n (en + 5), polish, release pipeline | |

## 10. Open questions for you

1. **Name/package:** resolved. Renamed to "Victorious-Manager" with package `app.victoriousmanager`; a hyphen is not valid in a Java package name, so the package drops it.
2. **DI:** Hilt vs manual — recommend Hilt for this module count.
3. **Distribution:** Play Store, or sideload/F-Droid? This affects permission scope (all-files, install packages).
4. **Android SDK:** this environment may lack the SDK/NDK. M0 will start by checking; if absent I will need network access to install it, or CI will be the only build check.
5. **Scope order:** is the milestone order above right, or should APK tools come before the editor?

Nothing beyond this document has been created. Approve, or tell me what to change, and I will start M0.
