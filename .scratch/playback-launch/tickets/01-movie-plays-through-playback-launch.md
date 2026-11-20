# 01: Movie plays through Playback launch

**What to build:** The Playback launch module owns the whole journey for a Movie. Tapping play on a Movie opens the Internal player or the External player according to the settings (an Unavailable Media does nothing), falls back to the Internal player for the same Media when the External player cannot start, and remembers the Progress the External player reports on the Media that was launched. The Movie page only says "play this Media" and renders the outcome. Includes the external player adapter seam with a fake and the Android adapter, and a single activity result launcher registered once for the whole app. See `.scratch/playback-launch/specs.md` (Implementation Decisions).

**Blocked by:** None (can start immediately)

**Status:** done

- [x] `play(media, forceInternal)` returns `OpenInternalPlayer(media)`, `HandedToExternalPlayer` or `Unavailable`; the availability check exists once
- [x] Any adapter failure (no player, permission denied, other) yields `OpenInternalPlayer` for the same Media without retrying the External player; failure kind is logged
- [x] `onExternalResult(position)` records Progress on the launched Media through the existing progress-saving behaviour; null position or no pending launch records nothing; a new launch replaces the pending one; each result is recorded at most once
- [x] Module has no `android.*` dependency; registered in Koin commonMain
- [x] Android adapter (app module) builds the view request (start position vs from-start, title, read permission), starts/stops the foreground service around the launch (also on failure), parses the returned position (long/int, several keys)
- [x] The activity result launcher is registered once in `MainActivity` and feeds `onExternalResult`; the Movie screen no longer has its own launcher
- [x] Movie page drops its external flag Event and external-result Intent; emits one `OpenInternalPlayer(media)` Event only for that outcome
- [x] Module test with harness and fake adapter covers the cases in the spec, including result recorded while the page shows another Media; Android adapter JVM tests for extras and parsing; Movie ViewModel test reduced to delegation
- [ ] Verified on a device with `adb`: internal, external, and fallback with no player installed
