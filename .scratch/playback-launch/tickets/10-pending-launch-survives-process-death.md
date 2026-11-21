# 10: The External player result survives Flux being killed

**What to build:** A viewer who watches for an hour in an External player while Android kills Flux still gets their Progress saved when the player closes.

**Blocked by:** 09 (Overlapping External player launches), both change the pending-launch rules

**Status:** ready-for-agent

- [x] The pending launch (Media id) is persisted when set and cleared with the same lifecycle rules as the in-memory one (cleared by every `play()`, on failure, and once consumed)
- [x] After process death, a result with a persisted pending launch records Progress on that Media; a result with none records nothing; each result is recorded at most once
- [x] A persisted Media that no longer exists records nothing and clears the entry
- [x] The spec's "lost on process death" statement is removed
- [x] Module tests simulate a restart with a fresh module instance over the same persisted store
- [x] `./gradlew :app:testFossDebugUnitTest` passes
