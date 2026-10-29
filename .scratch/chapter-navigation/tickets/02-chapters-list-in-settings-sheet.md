# 02: Chapters list in the Settings sheet

Spec: `.scratch/chapter-navigation/specs/spec.md` (user stories 1–28)

**What to build:** When the Media has at least 2 Chapters, the general Settings sheet shows a third row, "Chapters", below "Audio" and "Subtitles", whose value is the current Chapter's title. Tapping it opens a Chapters sheet, built like the Track sheet: every Chapter in playback order, each showing its title from the UserFile (or "Chapter N", N being its 1-based position, when it has none) and its start time, with a check mark on the current Chapter. Tapping a Chapter seeks exactly to its start, closes the Settings sheet entirely, and leaves playback playing or paused as it was.

Demo: open a VCB-Studio-style file (Chapters "Prologue", "Part A", "Part B", "Preview"), open the Settings sheet, see "Chapters → Part A" while in Part A, open the list, tap "Preview", land at its first frame with the sheet closed.

**Blocked by:** 01 (One "current Chapter" rule, with half-open ranges)

**Status:** ready-for-agent

- [ ] The Chapter domain model carries the title read from the UserFile; a blank title is stored as absent
- [ ] The Chapters of the loaded Media are exposed in the player's session state, with a flag that is true only for 2 or more Chapters
- [ ] The current Chapter is exposed in the player's overlays state, follows progress, and is not re-emitted while it stays the same
- [ ] The Settings sheet has a Chapters variant alongside the general and Track variants
- [ ] A SelectChapter intent seeks to the Chapter's start with exact precision and closes the Settings sheet; play/pause is untouched
- [ ] The "Chapters" row is absent for 0 or 1 Chapter, present for 2+, and reuses the existing settings row look (label, value, chevron)
- [ ] The row value is the current Chapter's title, its "Chapter N" fallback, or empty before the first Chapter
- [ ] The Chapters sheet lists all Chapters in order with title (single line, ellipsised) and start time formatted like the seek bar; it scrolls; the current Chapter has a check mark, none before the first Chapter
- [ ] Opening and Ending Chapters appear like any other Chapter, with no badge
- [ ] Dismissing the Chapters sheet without choosing has no effect on playback
- [ ] New localised strings for the "Chapters" label and the "Chapter %d" fallback, in every language the app already ships
- [ ] Chapter reading test covers title mapping (present → kept, blank → absent)
- [ ] ViewModel tests cover: row flag for 0 / 1 / 2+ Chapters, current Chapter following progress, SelectChapter seek + sheet closing
- [ ] `./gradlew :app:testFossDebugUnitTest` passes
