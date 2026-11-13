# 06: Subtitle delay (spike, then feature)

Spec: `../specs.md`

**What to build:** The viewer shifts the subtitles of the current Media by ± milliseconds from the Settings sheet to fix an out-of-sync file. The delay applies to every text Track, ASS included, and starts at 0 for each new Media.

**Blocked by:** 05 Subtitle rendering (changes the subtitle contract this builds on)

**Status:** ready-for-agent

- [x] Spike, time-boxed: find out whether cues can be shifted on the Flux side, a positive delay by holding cues back and a negative delay by looking ahead; write the conclusion in this ticket before any UI work

  **Spike conclusion:** a clean technique exists in nextlib 1.11.1-0.18.0, read from its sources. `OffsetRenderer` exposes `syncOffsetMilliseconds`, and both text renderers implement it: `NextTextRenderer` (Media3 path, where the embedded ASS of the spike went) and `FfmpegTextRenderer` (libass/PGS/VobSub bitmaps). The offset is applied in `render()` as `position - offset`, so a positive value holds cues back and a negative value shows them early. It is a renderer property, so it survives seeks and pauses. It is set through `ExoPlayer.subtitleDelayMilliseconds` (nextlib, `NextTextRenderer.kt`). Flux side: the value is set on the service's ExoPlayer through a custom session command, as the seek precision already is. Not yet verified on a device.
- [ ] If no clean technique exists, stop: drop the Subtitle delay from the scope and report it; no fragile workaround and no custom subtitle decoder
- [x] If it works: a ± control in the Settings sheet (steps of 100 ms) shows the current delay and can reset it to 0
- [ ] The delay applies to ASS and plain text Tracks, and keeps working after a seek and after a pause
- [x] Loading another Media resets the Subtitle delay to 0; it is never persisted
- [x] ViewModel tests: intent changes state; a new Media resets it
- [x] Player manager test of the shifting mechanism
- [x] `./gradlew :app:testFossDebugUnitTest` passes (810 tests, 0 failures)
