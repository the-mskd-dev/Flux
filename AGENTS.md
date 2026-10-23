# Project Directives &amp; Architecture (Android / KMP)

## 1. Context

- **Description** : KMP (Android Only) application. This app allows users to retrieve their video files information through the API TMDB, and then play their files within the application.
- **Goals** : Retrieve information, organize files in a modern library, play files and keep a progress history.

## 2. Agent skills

### Issue tracker

Issues and specs live in GitHub Issues (`the-mskd-dev/Flux`, via the `gh` CLI). See `docs/agents/issue-tracker.md`.

### Triage labels

Default five-role vocabulary (needs-triage, needs-info, ready-for-agent, ready-for-human, wontfix), applied as GitHub labels. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: root `GLOSSARY.md` + `docs/adr/`. See `docs/agents/domain.md`.

## 3. ANDROID CLI &amp; TERMINAL TOOLING

You are allowed to execute terminal commands to interact with the Android environment, run builds, and inspect tests.

### Available Tooling &amp; Commands:

1. **Unit Testing &amp; Verification**:

   - Command: `./gradlew :app:testFossDebugUnitTest`

   - Purpose: Validate ViewModel tests, Koin module configurations, and logic.

2. **Android CLI / ADB Automation**:

   - Command: `adb devices` (to check connected emulators or physical devices)

   - Command: `adb logcat -d | grep "com.mskd.flux"` (to inspect app logs and crashes)

   - Command: `./gradlew installFossDebug` (to build and install the debug APK)

### Execution Rules for CLI:

- Always check that a device/emulator is connected via `adb devices` before running installation or UI inspection commands.

- Never run long-polling logcat commands (`adb logcat` without `-d`); always use `-d` to dump and exit so the terminal process does not hang.