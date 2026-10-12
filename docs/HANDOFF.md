# Handoff: Victorious-Manager

Read this first in a new thread. It is the single source of current state. The design is in [ARCHITECTURE.md](../ARCHITECTURE.md).

Last updated after: **M0 complete**, PR #2 open.

## Process (agreed with the owner)

1. Build one phase at a time, following the milestone table in ARCHITECTURE.md §9.
2. At the end of each phase: open a PR (or update the open one) containing that phase's code, then update this file.
3. Tell the owner the PR link, then the owner closes the thread and starts a new one. Do not carry a long thread into the next phase.
4. Open a PR only when a unit of work is complete. Do not open PRs for in-progress work.
5. Every phase that adds a screen or a setting also adds its section to `docs/USER_MANUAL.md`, with step-by-step instructions that match the built UI. Do not write steps for screens that do not exist yet.

## Status

| Phase | State | Where |
|---|---|---|
| M0 Skeleton, CI, license, architecture | **Done, PR open** | [PR #2](https://github.com/Victorious93/MT-Manager-VIP/pull/2), not merged |
| M1 Dual-pane browser | **Not started.** Partial, untested code is parked (see below) | none |
| M2 to M9 | Not started | none |

The PR targets `main`. The branch is `ccr-8fbcfc61-nujyty`.

## Decisions

- Project name: **Victorious-Manager**. Package: `app.victoriousmanager`. A hyphen is not valid in a Java package name, so the package drops it.
- Theme class: `VictoriousManagerTheme`.
- Repo name `MT-Manager-VIP` is unchanged.
- `compileSdk` 36 (the current AndroidX libraries require it). `targetSdk` 35. `minSdk` 26.
- Dependency injection: Hilt, to be added in M1.
- Recycle bin: **yes** (owner). One bin per storage volume, with a retention setting. Design in ARCHITECTURE.md §11.1. Built in M1.
- In-app manual: **yes** (owner). A Help screen inside the app, with the manual kept as one source in `docs/USER_MANUAL.md`. Design in ARCHITECTURE.md §11.2. Viewer built in M1.
- Distribution: **direct APK download on GitHub Releases** (chosen by the assistant at the owner's request; reversible). ARCHITECTURE.md §11.4.
- Milestones: **M1 split into M1a and M1b** (ARCHITECTURE.md §9). The owner delegated this choice.
- Parked M1 code: saved to branch `wip/m1-core-parked` (untested, do not merge; not part of any PR).
- Approved by the owner: the architecture and the recommended defaults for the open questions in ARCHITECTURE.md §10.
- **Still open:** distribution channel (Play Store vs sideload/F-Droid). This affects the all-files and install-package permissions. It must be decided before M1 is finished.
- Commit trailers use `Co-Authored-By: Claude <noreply@anthropic.com>` with no model name.

## Versions (verified against Maven metadata on 2026-10-12)

- AGP 8.13.2, Kotlin 2.2.21, Gradle wrapper 8.14.3
- Compose BOM 2025.12.01, activity-compose 1.12.4, kotlinx-coroutines 1.10.2
- JDK 21 is installed in the sandbox. The project targets Java 17 bytecode.

## Build

```
./gradlew --no-parallel lintDebug testDebugUnitTest assembleDebug
```

The Android SDK is **not** in the repo and must be installed in each new sandbox:

```
curl -sS -O https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip -q commandlinetools-linux-11076708_latest.zip -d /tmp/cmdtools
mkdir -p /opt/android-sdk/cmdline-tools
mv /tmp/cmdtools/cmdline-tools /opt/android-sdk/cmdline-tools/latest
yes | /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --licenses
/opt/android-sdk/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-36" "platforms;android-35" "build-tools;35.0.0"
echo "sdk.dir=/opt/android-sdk" > local.properties   # gitignored; recreate each time
export ANDROID_HOME=/opt/android-sdk
```

Note: `sdkmanager --licenses` accepts the Android SDK licence terms non-interactively. Read them first if that matters to you.

Known sandbox limit: Maven Central returns HTTP 429 (rate limit) during dependency resolution. Retry the same command after a short wait. Gradle keeps what it downloaded, so later attempts go further.

Last verified result (M0 state, after the rename): BUILD SUCCESSFUL. One unit test (`FsEntryTest`) passed. Debug APK package `app.victoriousmanager`, label `Victorious-Manager`. Not run on a device. GitHub Actions has not run on this PR as far as I have verified.

## User manual

`docs/USER_MANUAL.md` is a skeleton with a full glossary. Its step-by-step sections are filled in per phase (see process item 5). Its status banner says which sections are still planned.

## Parked M1 work (untested, do not merge as-is)

- Location: branch **`wip/m1-core-parked`** (pushed). The same files are also in the local sandbox stash `stash@{0}`, which may be lost.
- Contents, under the new package `app.victoriousmanager`:
  - `FsEntry.kt` (modified: adds `SortField`, `ListOptions`, `PageRequest`, `Page`, `FsException`, `isHidden`)
  - `FileProvider.kt`, `LocalFileProvider.kt`: listing, paging, sorting, filtering; create, rename, delete
  - `FileOperations.kt`: copy, move, delete with conflict handling
  - `JobQueue.kt`: runs jobs one at a time, with cancel
  - Tests: `LocalFileProviderTest.kt`, `FileOperationsTest.kt`, `JobQueueTest.kt`
- **Status: never compiled, never passed tests.** The last test run was stopped by a time limit, so its result is unknown.
- Known problems to fix before use:
  - `LocalFileProviderTest.pagesAreConsistentAndTotalIsFilteredCount` has a meaningless assertion (`...coerceAtMost(3).let { 3 }`). Replace it with a real check.
  - `JobQueue` cancellation logic (`isActiveScope`, the `CancellationException` branch) is untested and probably needs simplifying.
  - `FileOperations` has not been reviewed for symlink, cross-volume move, and overwrite edge cases beyond the tests written.

## Next steps (start of the M1a thread)

1. The owner merges PR #2 into `main` first. Otherwise the M1a PR would also contain the M0 commits.
2. Continue on `ccr-8fbcfc61-nujyty`. If PR #2 has merged, reset the branch to `origin/main` first, as the session's git rules require.
3. Build **M1a** (ARCHITECTURE.md §9): browsing, storage permission, theme setting, Help viewer.
4. Take M1a's code from `wip/m1-core-parked`: `FsEntry.kt`, `FileProvider.kt`, `LocalFileProvider.kt` and `LocalFileProviderTest.kt`. Fix the known problems above. Leave `FileOperations.kt`, `JobQueue.kt` and their tests for M1b.
5. Build the M1a UI with Hilt. Add the Help screen (ARCHITECTURE.md §11.2) and the manual sections for browsing and storage permission.
6. Run `./gradlew --no-parallel lintDebug testDebugUnitTest assembleDebug`, then commit and push.
7. Open the M1a PR against `main`, update this file, and tell the owner. M1b starts in a new thread.

## Open questions for the owner

1. None block M1a. The distribution choice (direct APK on GitHub Releases) was made by the assistant at the owner's request. Say if you want Google Play or F-Droid instead (ARCHITECTURE.md §11.4).
2. Merge PR #2 once you are happy with M0. M1a starts after that.
