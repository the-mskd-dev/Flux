# 09: Overlapping External player launches are an explicit, documented limit

**What to build:** A double tap on two different Media must not silently contradict the spec. The result Intent carries no launch id, so results cannot be told apart; the spec states that the newest launch owns the next result, and no longer claims Progress is never recorded on the wrong Media in that case.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] Story 20 and the story 28 note in `specs.md` are rewritten to state the limit: with two External players open, the first result is recorded on the newest launch's Media
- [ ] Decide and record whether to also reduce the risk cheaply (for example ignore a second external `play()` within a short window); implement only if trivial
- [ ] Module test documents the behaviour: Media A then Media B launched, one result, recorded on B, recorded at most once
- [ ] `./gradlew :app:testFossDebugUnitTest` passes
