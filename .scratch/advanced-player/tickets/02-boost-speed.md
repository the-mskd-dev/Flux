# 02: Boost speed

Spec: `../specs.md`

**What to build:** The viewer presses and holds anywhere on the video: playback goes to 2x and a "2x" pill appears at the top (Ambient overlay style). On release, the previous Playback speed is restored and the pill disappears.

**Blocked by:** 01 Playback speed (the Boost speed restores the speed kept in state)

**Status:** ready-for-agent

- [x] A long press on the video surface starts the Boost speed at 2x and shows the "2x" pill
- [x] Releasing restores exactly the Playback speed that was active before, whether 1x or another speed
- [x] The Interface still toggles on a tap and the double-tap seek still works
- [x] No Boost speed in Picture-in-Picture
- [x] No Boost speed during a pinch zoom or a volume/brightness gesture
- [x] A cancelled gesture (leaving the app, a call, a change of Media) never leaves the player stuck at 2x
- [x] ViewModel tests: start, release restoring the previous speed, refusal in PiP, cancel
- [x] `./gradlew :app:testFossDebugUnitTest` passes
