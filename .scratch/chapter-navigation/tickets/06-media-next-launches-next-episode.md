# 06: MediaNext launches the Next Episode

Spec: `.scratch/chapter-navigation/specs/spec.md` (user stories 29–37, 43)

**What to build:** Pressing the MediaNext key on a keyboard, Bluetooth remote or headset launches the Next Episode at any point of the current Episode, without showing the Chapter button. If there is no Next Episode (Movie, last Episode, Unknown Artwork, or Unavailable Next Episode), the key does nothing at all. Mid-Episode, the current Progress is saved as-is (the Episode stays "en cours"). While the Chapter button is showing "Next episode", pressing MediaNext marks the current Episode WATCHED, exactly like tapping the button.

**Blocked by:** 05 ("Next episode" on the Chapter button marks the current Episode WATCHED)

**Status:** ready-for-agent

- [ ] A new player intent for MediaNext resolves the Next Episode with the same lookup and per-Media cache as the Chapter button
- [ ] With no Next Episode, nothing is saved, the Media does not change and nothing is shown
- [ ] With a Next Episode and the Chapter button not showing "Next episode", Progress is saved not forced, then the Media switches
- [ ] With the Chapter button showing "Next episode", Progress is saved with WATCHED forced, then the Media switches
- [ ] It reuses the Next Episode routine introduced in ticket 05
- [ ] The Chapter button is not displayed as a side effect of pressing MediaNext
- [ ] The player screen's key handler maps the MediaNext key to the intent; other key mappings unchanged
- [ ] ViewModel tests cover the three cases (no Next Episode, mid-Episode, Chapter button showing "Next episode")
- [ ] `./gradlew :app:testFossDebugUnitTest` passes
