# 01: Playback speed

Spec: `../specs.md`

**What to build:** The viewer opens the Settings sheet, picks a Playback speed from 0.25x to 2x (steps of 0.25) and the Media plays at that speed. The active speed is visible in the sheet. Each new Media starts at 1x.

**Blocked by:** None (can start immediately)

**Status:** done

- [ ] The Settings sheet lists 0.25x, 0.5x, 0.75x, 1x, 1.25x, 1.5x, 1.75x and 2x, with the active one marked
- [ ] Picking a speed changes the playback speed of the player, audio included, without restarting playback
- [ ] Loading another Media (including Next Episode) resets the Playback speed to 1x
- [ ] The speed is part of the player state so that Boost speed can restore it
- [ ] The existing Settings sheet pages (tracks, Chapters) still work
- [ ] ViewModel tests: choosing a speed updates state; a new Media resets it to 1x
- [ ] Player manager test: the chosen speed reaches the player
- [ ] `./gradlew :app:testFossDebugUnitTest` passes
