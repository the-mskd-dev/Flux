# Project Directives & Architecture (Android / KMP)

## 1. Context

- **Description** : KMP (Android Only) application. This app allows users to retrieve their video files information through the API TMDB, and then play their files within the application.
- **Goals** : Retrieve information, organize files in a modern library, play files and keep a progress history.
- **Architecture** : Read before creating a file, adding an entity, or proposing a stack change. See `ARCHITECTURE.md`.

## 2. Agent skills

### Global rules

In the whole code, specs, tickets, GLOSSARY.md, you should use English only. Other languages are authorized only in strings.xml files.

### Issues, specs and tickets
- GitHub Issues (`the-mskd-dev/Flux`) are **read-only** for agents: read them with `gh issue view <number> --json number,title,body,labels,comments`. Never create, edit, label, close or comment on issues.
- All specs go in `.scratch/<REQUEST>/specs.md`, all tickets in `.scratch/<REQUEST>/tickets/`.
- `.scratch/` is the only place where agents write planning documents. See `docs/agents/issue-tracker.md`.

### Triage labels

Default five-role vocabulary (needs-triage, needs-info, ready-for-agent, ready-for-human, wontfix), applied as GitHub labels. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: root `GLOSSARY.md` + `docs/adr/`. See `docs/agents/domain.md`.

## 3. ANDROID CLI & TERMINAL TOOLING

You are allowed to execute terminal commands to interact with the Android environment, run builds, and inspect tests.

### Available Tooling & Commands:

1. **Unit Testing & Verification**:

   - Command: `./gradlew :app:testFossDebugUnitTest`

   - Purpose: Validate ViewModel tests, Koin module configurations, and logic.

2. **Android CLI / ADB Automation**:

   - Command: `adb devices` (to check connected emulators or physical devices)

   - Command: `adb logcat -d | grep "com.mskd.flux"` (to inspect app logs and crashes)

   - Command: `./gradlew installFossDebug` (to build and install the debug APK)

### Execution Rules for CLI:

- Always check that a device/emulator is connected via `adb devices` before running installation or UI inspection commands.

- Never run long-polling logcat commands (`adb logcat` without `-d`); always use `-d` to dump and exit so the terminal process does not hang.

### Notes for future agents

- Write a note only for what the code cannot tell: a non-obvious constraint, a trap hit during the task, or a decision with trade-offs (→ ADR).
- Never describe what the code already says.
- Before adding, search existing notes: update or delete rather than append.
- If you find a note contradicting the code, fix or remove it.
- Keep each note under ~5 lines.