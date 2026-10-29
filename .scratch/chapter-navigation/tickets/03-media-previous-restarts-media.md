# 03: MediaPrevious restarts the Media

Spec: `.scratch/chapter-navigation/specs/spec.md` (user stories 38–41, 45)

**What to build:** Pressing the MediaPrevious key on a keyboard, Bluetooth remote or headset restarts the current Media (Movie or Episode) from the beginning. Playback stays playing or paused as it was. Pressing it again restarts again; there is no "previous Episode". Existing keys (arrows, Rewind, FastForward, Space) keep their current behaviour.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] A new player intent for MediaPrevious seeks to 0 with exact precision and does not change play/pause
- [ ] The player screen's key handler maps the MediaPrevious key to that intent
- [ ] Works identically for a Movie and an Episode
- [ ] Arrow keys, Rewind, FastForward and Space mappings are unchanged
- [ ] ViewModel test covers the intent (seek to 0, exact precision)
- [ ] `./gradlew :app:testFossDebugUnitTest` passes
