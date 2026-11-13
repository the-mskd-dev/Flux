# 03: Media info

Spec: `../specs.md`

**What to build:** A "Media info" page in the Settings sheet shows, read-only, the technical description of the Media being played: container, video codec, resolution, frame rate, bitrate, audio codec, channels and sampling rate. Unknown values show a placeholder.

**Blocked by:** None (can start immediately)

**Status:** done

- [x] A pure Kotlin Media info model exists in common code with no `androidx` or `android` import
- [x] The Android player manager builds it from the player's track formats (completed by nextlib-mediainfo only if a value cannot be read otherwise)
- [x] A missing value is optional in the model and rendered with a placeholder, never "null" or 0
- [x] The page is reachable from the Settings sheet and goes back to it
- [x] The page is written in Compose Multiplatform if it needs no Android API
- [x] Player manager tests: mapping of a typical video+audio file, and of a file with unknown values
- [x] ViewModel test: Media info is exposed once the player is ready and cleared on a new Media
- [x] `./gradlew :app:testFossDebugUnitTest` passes
