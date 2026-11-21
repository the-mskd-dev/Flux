# 11: External player result is recorded even if the Activity is destroyed

**What to build:** A viewer who returns from the External player just as Flux is recreated (rotation, low-memory kill) still gets their Progress saved. Today the pending launch is cleared before the Media lookup and Progress save run, so if the Activity-scoped coroutine is cancelled in that window the watched position is lost permanently.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [x] Handling an External result cannot be cancelled between consuming the pending launch and the Progress being saved: either it runs in a scope that outlives the Activity, or the pending launch is cleared only after the save (or the Internal player fallback hand-off) succeeds
- [x] A module test cancels the caller mid-handling and checks that the Progress is still recorded, or that the pending launch is still available for retry
- [x] The "recorded at most once" rule from ticket 10 still holds, so a retry cannot record the same result twice
- [x] `./gradlew :app:testFossDebugUnitTest` passes
