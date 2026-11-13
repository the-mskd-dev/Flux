# 06: Subtitle delay (spike, then feature)

Spec: `../specs.md`

**What to build:** The viewer shifts the subtitles of the current Media by ± milliseconds from the Settings sheet to fix an out-of-sync file. The delay applies to every text Track, ASS included, and starts at 0 for each new Media.

**Blocked by:** 05 Subtitle rendering (changes the subtitle contract this builds on)

**Status:** ready-for-agent

- [ ] Spike, time-boxed: find out whether cues can be shifted on the Flux side, a positive delay by holding cues back and a negative delay by looking ahead; write the conclusion in this ticket before any UI work
- [ ] If no clean technique exists, stop: drop the Subtitle delay from the scope and report it; no fragile workaround and no custom subtitle decoder
- [ ] If it works: a ± control in the Settings sheet (steps of 100 ms) shows the current delay and can reset it to 0
- [ ] The delay applies to ASS and plain text Tracks, and keeps working after a seek and after a pause
- [ ] Loading another Media resets the Subtitle delay to 0; it is never persisted
- [ ] ViewModel tests: intent changes state; a new Media resets it
- [ ] Player manager test of the shifting mechanism
- [ ] `./gradlew :app:testFossDebugUnitTest` passes
