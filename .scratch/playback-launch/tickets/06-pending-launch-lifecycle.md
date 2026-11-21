# 06: Pending launch lifecycle

**What to build:** A viewer's Progress is never recorded against the wrong Media. A stale External player launch whose result never came back cannot capture a later result, and a result delivered the instant the External player returns is not lost.

**Blocked by:** 01 (Movie plays through Playback launch)

**Status:** done

- [x] Every `play()` clears the pending launch first, including forced-internal, Unavailable and fallback outcomes
- [x] The pending launch is set before the adapter is called and cleared when the adapter reports any failure
- [x] Module test: Media A handed off, result lost, Media B falls back / forced internal / Unavailable, then a late result records nothing
- [x] Module test: with a fake adapter that delivers the result from inside `launch`, the Progress is recorded on the launched Media
- [x] Module test: a failed launch leaves no pending launch (a later result records nothing)
- [x] Android adapter tests (or equivalent) cover: detached launcher returns `Failed`; the service is stopped on result, cancel and null position
- [x] `./gradlew :app:testFossDebugUnitTest` passes
