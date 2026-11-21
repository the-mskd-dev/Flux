# 07: External player positions are saved in the right unit

**What to build:** A viewer who closes an External player at 90 s resumes at 90 s, whichever player and whichever format (Long or Int, any of the reported keys) it used. A position is never saved raw in the wrong unit, so Status never flips to watched or to watch by mistake.

**Blocked by:** None (can start immediately)

**Status:** done

- [ ] First confirm the unit of `Media.currentTime` / Progress, and the unit each supported player reports under each key (`position`, `extra_position`, `playback_position`, `end_by`); record both in `specs.md` (Implementation Decisions)
- [ ] The spec states the conversion applied to the reported position for each key and for Long versus Int
- [ ] Parsing converts to the Progress unit; the `-1` "no position" sentinel still records nothing
- [ ] JVM test case for each key, for Long and for Int, asserting the converted value
- [ ] `./gradlew :app:testFossDebugUnitTest` passes
