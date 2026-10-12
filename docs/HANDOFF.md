# Handoff: Victorious-Manager

Read this first in a new thread. It is the single source of current state. The design is in [ARCHITECTURE.md](../ARCHITECTURE.md).

Last updated after: **M0 complete**, PR #2 open.

## Process (agreed with the owner)

1. Build one phase at a time, following the milestone table in ARCHITECTURE.md §9.
2. At the end of each phase: open a PR (or update the open one) containing that phase's code, then update this file.
3. Tell the owner the PR link, then the owner closes the thread and starts a new one. Do not carry a long thread into the next phase.
4. Open a PR only when a unit of work is complete. Do not open PRs for in-progress work.

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

## Parked M1 work (untested, do not merge as-is)

- Location: local git stash `stash@{0}` ("M1 WIP (untested)"), based on commit `10a02d7`. It is **local to the sandbox**. If the container is reclaimed, it is lost.
- A copy with the new package name is in the session scratchpad, but that copy is also session-local.
- Contents (old package `app.fileforge`):
  - Modified tracked file: `core-fs/src/main/kotlin/app/fileforge/fs/FsEntry.kt` (adds `SortField`, `ListOptions`, `PageRequest`, `Page`, `FsException`, `isHidden`)
  - Untracked: `FileProvider.kt`, `LocalFileProvider.kt`, `FileOperations.kt`, `JobQueue.kt`
  - Untracked tests: `LocalFileProviderTest.kt`, `FileOperationsTest.kt`, `JobQueueTest.kt`
- **Status: never compiled, never passed tests.** The last test run was stopped by a time limit, so its result is unknown.
- Known problems to fix before it can be a PR:
  - `LocalFileProviderTest.pagesAreConsistentAndTotalIsFilteredCount` has a meaningless assertion (`...coerceAtMost(3).let { 3 }`). Replace it with a real check.
  - `JobQueue` cancellation logic (`isActiveScope`, the `CancellationException` branch) is untested and probably needs simplifying.
  - `FileOperations` has not been reviewed for symlink, cross-volume move, and conflict-overwrite edge cases beyond the tests written.

## Next steps (start of the M1 thread)

1. Restore the parked files under the new package path `core-fs/src/main/kotlin/app/victoriousmanager/fs/` and `core-fs/src/test/kotlin/app/victoriousmanager/fs/`. Use `git show 'stash@{0}^3:<old path>'` for the untracked files and `git diff stash@{0}^1 stash@{0} -- core-fs` for the tracked change. Rename `app.fileforge` to `app.victoriousmanager` in each file. Do not `git stash pop`, because it conflicts with the renamed `FsEntry.kt`.
2. Run the tests. Fix failures and the known problems above.
3. Add the M1 UI on top of `:core-fs` (dual pane, tabs, selection, sort/filter, conflict dialog, storage-permission flow), with Hilt.
4. Run the full build, then commit and push to `ccr-8fbcfc61-nujyty`.
5. Open the M1 PR against `main`, update this file, and tell the owner.

Open a PR only once M1 is complete and verified. Do not open one for the parked code.

## Owner's open questions

1. Distribution channel (Play Store vs sideload/F-Droid). Needed before M1 is finished.
2. Whether the parked M1 work should be saved to a draft branch for durability. That would be a new branch, so it needs the owner's permission.
