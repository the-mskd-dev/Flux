# 04: Catalog plays through Playback launch

**What to build:** Tapping play on the Catalog page goes through the Playback launch module, with the same behaviour as the Movie page. The page only says "play this Media" and renders the outcome; its own current-Media bookkeeping for the external result is removed.

**Blocked by:** 01 (Movie plays through Playback launch)

**Status:** done

- [x] Play delegates to the module and emits one `OpenInternalPlayer(media)` Event only for that outcome
- [x] The page's external flag Event, external-result Intent, per-page launcher and result bookkeeping are removed
- [x] Progress reported by the External player is recorded on the launched Media, not the one currently shown
- [x] ViewModel test reduced to delegation and rendering the outcome; existing tests pass
