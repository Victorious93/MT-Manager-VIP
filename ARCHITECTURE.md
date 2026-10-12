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
| **M1a** | Dual-pane browser, `LocalFileProvider` paged listing, tabs, selection, sort/filter/hidden, storage-permission flow (all-files and SAF), theme setting, Help viewer (TOC, sections, search) | Unit tests for listing, paging and sort green; Help renderer tests green; manual sections for browsing and permissions written |
| **M1b** | File operations (copy, move, rename, delete, new, properties), conflict dialog, `JobQueue` + foreground service, Recycle bin (Room index, per-volume bin, retention), "?" context help for these screens | Unit tests for `FileOperations` and `JobQueue` green; recycle-bin tests in §11.1 green; manual sections for file operations and the Recycle bin written |
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

## 11. Additions from owner review

### 11.1 Recycle bin (decided: yes; milestone M1)

- **Behaviour.** Delete moves an item into the Recycle bin instead of removing it. Restore puts it back at its original path. Items are removed for good when the bin is emptied, when "Delete permanently" is used, or after the retention period.
- **Location.** One bin per storage volume: `<volume root>/.victorious-bin/`. The name starts with a dot, so it is hidden, and the folder contains a `.nomedia` file so gallery apps skip it.
- **Why one bin per volume.** A rename within one volume is instant and copies no data. A single shared bin would force a cross-volume copy on every delete.
- **Index.** A Room table `TrashEntry` in `:core-data` with `id`, `originalPath`, `binPath`, `volumeId`, `isDirectory`, `sizeBytes` and `deletedAt`. Restore and retention use this table.
- **Retention.** Setting "Keep deleted items for N days", default 30. Expired items are purged when the app starts and when the bin is opened. A background purge with WorkManager is a later option, not part of M1.
- **Not sent to the bin.** These are deleted permanently, and the app warns before the user confirms:
  - items inside archives;
  - items on network locations (FTP, SFTP, SMB, WebDAV);
  - items the app cannot rename into the bin, for example files it has no permission to move;
  - every item on a volume where the bin folder cannot be created, for example under SAF-only access when the volume root is not granted.
  The confirmation must state which case applies.
- **Symbolic links.** The link itself moves to the bin. Its target is never touched.
- **Restore conflicts.** Reuse the conflict choices from §4.3 of the manual: Skip, Overwrite, Keep both.
- **M1 exit tests.** Delete, then restore round-trip; restore conflict; purge of expired entries; permanent-delete path; symlink moved and not followed; bin folder created with `.nomedia`.
- **Known limit.** If the app's data is cleared or the app is reinstalled, the Room index is lost, but the files remain in the bin folders. Re-indexing them is out of scope for M1.

### 11.2 In-app manual (decided: yes; viewer in M1, content grows per phase)

- **Single source.** The manual text lives in `docs/USER_MANUAL.md` and is copied into the app's assets at build time. One source avoids two copies drifting apart. When the viewer is built, the manual is split into one file per section so the build can copy sections individually.
- **Viewer.** A Help screen with a table of contents, a section view, and text search across all sections. It works offline.
- **Context help.** Each feature screen gets a "?" button that opens that feature's manual section. Each phase adds its own buttons.
- **Rendering.** The renderer supports only the Markdown the manual uses: headings, paragraphs, bullet lists, bold, italic, tables, links and in-manual anchors. A small in-house renderer keeps dependencies down. If the subset grows, adopt a Markdown library after a licence check.
- **Version shown.** The Help screen shows the app version (`BuildConfig.VERSION_NAME`), so a reader knows which manual version they have.
- **Language.** English until M9. Translated sections will live in separate asset folders.
- **Tests.** Renderer unit tests for each supported element. A test fails the build if a "?" button points at a section that does not exist.

### 11.3 Milestone impact

- M1 grows to include the recycle bin and the manual viewer, so it was the largest phase.
- **Decided and applied to §9:** M1 is split into **M1a** (browsing, storage permission, Help viewer) and **M1b** (file operations, job queue, recycle bin). Each PR stays reviewable, and each thread stays short. The owner delegated this choice.

### 11.4 Distribution channel (decided: direct APK download)

"Distribution channel" means how users get the app onto their phones. There are three realistic options:

| Route | How users get it | Review and rules | Updates | Likely effect on this app |
|---|---|---|---|---|
| **Google Play** | Google's official store | Google reviews each release against its policies. Apps that request all-files access must justify it in a declaration form, and the request can be refused. | Automatic | All-files access may be refused, so SAF-only browsing would need to work well |
| **Direct APK** (for example GitHub Releases) | User downloads the APK file and installs it | No store review | Manual, unless the app adds its own update check (not planned) | Full all-files access. Users must allow installs from the source in Android settings |
| **F-Droid** | A store for free and open-source Android apps | No all-files review, but its own inclusion rules and release schedule | Through F-Droid | Full all-files access. Source must be public |

[Likely] These policy details should be checked against the current Google Play policy and F-Droid documentation before a decision.

**Decision: direct APK download, published on GitHub Releases.** The assistant chose this at the owner's request ("U choose"). The owner can override it.

Reasons:
- The app needs all-files access, and its planned features (APK re-signing in M5, root in M7) are the ones most likely to be questioned in a store review. A store refusal is the outcome we cannot control.
- With no store review, releases ship on our schedule.
- F-Droid can be added later without changing the app.

Cost: no automatic updates. Users install each new APK themselves (see the user manual, section 1.2).

[Likely] Google Play's current policy on all-files access, and on installing or re-signing apps, should be checked before any future Play submission.
