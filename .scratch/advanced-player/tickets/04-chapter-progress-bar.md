# 04: Chapter progress bar

Spec: `../specs.md`

**What to build:** The progress bar is split into one segment per Chapter (proportional to its duration, with a gap at each boundary). While the viewer scrubs, a bubble above the thumb shows the time and the name of the Chapter under it. With fewer than 2 Chapters, the bar stays continuous.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] Segment widths are proportional to the duration of each Chapter and cover the whole Media
- [ ] A boundary gap is visible between segments, in the played and the unplayed part
- [ ] While scrubbing, the bubble shows the time and the Chapter name (or its number when it has no title)
- [ ] The Chapter under the thumb is found with the same boundary rule as elsewhere: a position on a boundary belongs to the Chapter that starts there
- [ ] With 0 or 1 Chapter the bar is continuous and no Chapter name is shown
- [ ] Scrubbing keeps its current behaviour: fast seeks while dragging, exact seek on release, no jump back after release
- [ ] The wavy playing style and the time labels are kept
- [ ] The new bar is written in Compose Multiplatform in the shared module
- [ ] ViewModel/pure-logic tests: Chapter lookup at a position, on a boundary and past the end
- [ ] Manual check on a device with a Media that has Chapters and one that has none
- [ ] `./gradlew :app:testFossDebugUnitTest` passes
