# Victorious-Manager

An original, clean-room Android file manager and APK/text toolkit (dual-pane browser, archives, editor, APK tools).
It shares no code, assets or branding with any other app.

Status: **M0 complete** (PR #2 open). M1 not started. See [docs/HANDOFF.md](docs/HANDOFF.md) for current state and next steps, and [ARCHITECTURE.md](ARCHITECTURE.md) for the design.

## Build

Requires JDK 17+ and the Android SDK (platform 36, build-tools 35).

```
./gradlew lintDebug testDebugUnitTest assembleDebug
```

## License

Apache-2.0 (see `LICENSE`).
