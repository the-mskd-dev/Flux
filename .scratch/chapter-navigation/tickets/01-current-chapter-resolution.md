# 01: One "current Chapter" rule, with half-open ranges

Spec: `.scratch/chapter-navigation/specs/spec.md`

**What to build:** Flux decides which Chapter is "current" for a given position in exactly one place, so the Chapter button today (and the Chapters list in ticket 02) always agree. A Chapter covers `[start, end)`: a position exactly on a boundary belongs to the Chapter that starts there, a position before the first Chapter's start or after the last Chapter's end has no current Chapter. The Chapter button switches to this rule. Visible effect: when playback sits exactly on the boundary between two Chapters (e.g. right after tapping "Skip opening", which seeks to the Opening's end), the Chapter button reflects the Chapter that starts there instead of still offering to skip the previous one.

This is a prefactor for ticket 02; it does not add any new UI.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] Current Chapter resolution lives on the Chapter domain side (not inline in the ViewModel) and is reusable by the presentation layer
- [ ] A position equal to a Chapter's `end` resolves to the next Chapter
- [ ] A position before the first Chapter's start resolves to no Chapter
- [ ] A position at or after the last Chapter's end resolves to no Chapter
- [ ] An empty Chapter list resolves to no Chapter
- [ ] The Chapter button logic (Opening skip, Ending skip, Next episode on a final Ending) uses this resolution
- [ ] After a "Skip opening" tap lands exactly on the Opening's end, the Chapter button no longer offers "Skip opening"
- [ ] Existing "chapter button from progress" ViewModel tests still pass; only boundary expectations that relied on closed ranges are adjusted, and a test named after the boundary behaviour is added
- [ ] `./gradlew :app:testFossDebugUnitTest` passes
