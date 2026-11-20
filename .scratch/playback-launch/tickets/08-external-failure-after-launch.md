# 08: A failure after the External player starts falls back to the Internal player

**What to build:** When the External player resolves but then fails (crashes, or returns a cancelled result carrying an error), the viewer is not left on the page with nothing playing: Flux falls back to the Internal player for the same Media, and the pending launch and foreground service are cleaned up.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] `specs.md` says which failures are synchronous in `launch` (no player, permission denied, other exception) and which arrive asynchronously through the activity result, and how each is cleaned up
- [ ] An asynchronous failure result (cancelled with an error) opens the Internal player for the launched Media without retrying the External player; a plain user cancel does not
- [ ] The pending launch is cleared and the foreground service stopped on every asynchronous failure
- [ ] Module test with the fake adapter and adapter test covering the asynchronous failure
- [ ] `./gradlew :app:testFossDebugUnitTest` passes
