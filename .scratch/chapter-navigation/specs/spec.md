# Spec: Chapters list in the Settings sheet, and media keys

Source: #370 (scope narrowed during grilling: Chapters list only, no ⏮/⏭ buttons, no swipe). Related: #324 (Next episode button appears too early, separate), #378 (behaviour when an Episode ends, separate).

## Problem Statement

Some releases, notably anime from groups like VCB-Studio, ship UserFiles with Chapters ("Prologue", "Part A", "Part B", "Preview", or just "Chapter 01", "Chapter 02"…). Flux reads them, but only uses them to offer the Chapter button ("Skip opening", "Skip ending", "Next episode"). When the Chapters are ordinary Chapters, I have no way to see them or jump to one: I have to scrub the seek bar by hand to find "Part B" or the "Preview".

Separately, my keyboard, Bluetooth remote or headset has Next / Previous media keys, and they do nothing in the player.

Two existing inconsistencies make moving to the Next Episode unreliable:

- When the last Chapter of an Episode is not an Ending (e.g. a "Preview" after the Ending), the Chapter button shows "Next episode" in the last 15 seconds, but tapping it saves the current Episode as "en cours" instead of WATCHED.
- When the Next Episode is Unavailable (its UserFolder is disconnected), the Chapter button still offers it.

## Solution

- The general Settings sheet gets a third row, **"Chapters"**, under "Audio" and "Subtitles". Its value is the title of the current Chapter. It only appears when the Media has at least 2 Chapters.
- Tapping it opens a dedicated Chapters sheet, built like the Track sheet: one item per Chapter, showing its title (or "Chapter N" when the UserFile has none) and its start time, with a check mark on the current Chapter.
- Tapping a Chapter seeks exactly to its start, closes the Settings sheet entirely, and keeps playback playing or paused as it was.
- The **MediaNext** key launches the Next Episode at any time, if there is one and it is not Unavailable; otherwise it does nothing.
- The **MediaPrevious** key restarts the current Media from the beginning.
- Whenever the Chapter button is showing "Next episode", moving to the Next Episode (by tapping the Chapter button or pressing MediaNext) marks the current Episode WATCHED.
- An Unavailable Next Episode is never offered nor launched, neither by the Chapter button nor by MediaNext.

## User Stories

### Chapters row

1. As a viewer, I want a "Chapters" row in the general Settings sheet when my Media has Chapters, so that I can discover that the file is split into Chapters.
2. As a viewer, I want the "Chapters" row to sit below "Audio" and "Subtitles" and look like them, so that the Settings sheet stays consistent.
3. As a viewer, I want the "Chapters" row to show the title of the Chapter I am currently in, so that I know where I am without opening the list.
4. As a viewer, I want the "Chapters" row to show "Chapter N" when the current Chapter has no title, so that the value is never empty while I am inside a Chapter.
5. As a viewer, I want the "Chapters" row to show no value when playback is before the first Chapter starts, so that Flux never claims I am in a Chapter I haven't reached.
6. As a viewer, I want no "Chapters" row when my Media has no Chapters, so that the Settings sheet isn't cluttered with a useless option.
7. As a viewer, I want no "Chapters" row when my Media has a single Chapter, so that I am not offered a list whose only entry is "the whole file".
8. As a viewer, I want the "Chapters" row value to update as playback moves into the next Chapter, so that it is accurate whenever I open the Settings sheet.

### Chapters sheet

9. As a viewer, I want tapping the "Chapters" row to open a list of all Chapters, so that I can pick the one I want.
10. As a viewer, I want the Chapters listed in playback order, so that the list matches the structure of the Media.
11. As a viewer, I want each Chapter to show its title as written in the UserFile, so that I recognise "Part A", "Preview", "オープニング"…
12. As a viewer, I want an untitled Chapter to show "Chapter N" (N being its 1-based position), so that every item is identifiable.
13. As a viewer, I want each Chapter to show its start time (e.g. `12:34`), so that I can find my way even when titles are generic ("Chapter 01", "Chapter 02"…).
14. As a viewer, I want the current Chapter to carry a check mark, like the selected Track does, so that I see where I am in the list.
15. As a viewer, I want no check mark when playback is before the first Chapter, so that the list stays truthful.
16. As a viewer, I want an Opening or an Ending to appear in the list like any other Chapter, with its own title and no extra badge, so that the list stays simple.
17. As a viewer, I want a long list of Chapters to scroll, so that files with many Chapters remain usable.
18. As a viewer, I want a long Chapter title to be truncated rather than breaking the layout, so that the start time stays visible.
19. As a viewer, I want to dismiss the Chapters sheet without choosing anything, so that I can back out with no effect on playback.

### Jumping to a Chapter

20. As a viewer, I want tapping a Chapter to seek exactly to its start, so that I land at the very beginning of "Part B" and not a few seconds off.
21. As a viewer, I want the Settings sheet to close entirely after I pick a Chapter, so that I am back to the video immediately.
22. As a viewer, I want playback to stay playing if it was playing when I picked a Chapter, so that the jump doesn't interrupt me.
23. As a viewer, I want playback to stay paused if it was paused when I picked a Chapter, so that I stay in control.
24. As a viewer, I want to be able to pick the Chapter I am already in, so that I can restart it from its beginning.
25. As a viewer, I want to be able to pick an earlier Chapter, so that I can rewatch a part.
26. As a viewer, I want the "Chapters" row and the check mark to reflect the Chapter I jumped to, so that the next time I open the Settings sheet it is correct.
27. As a viewer, I want the position exactly at a Chapter boundary to belong to the Chapter that starts there, so that jumping to a Chapter really puts me "in" that Chapter.
28. As a viewer, I want the Chapter button to keep working after a jump (e.g. "Skip opening" if I jumped to the Opening), so that both features coexist.

### MediaNext

29. As a viewer with a keyboard or remote, I want the MediaNext key to launch the Next Episode, so that I can binge without touching the screen.
30. As a viewer, I want MediaNext to work at any point in the Episode, not only when the Chapter button shows "Next episode", so that the key behaves predictably.
31. As a viewer, I want MediaNext to do nothing while watching a Movie, so that I don't get an unexpected seek or exit.
32. As a viewer, I want MediaNext to do nothing on the last Episode of a Show, so that nothing surprising happens at the end of a Show.
33. As a viewer, I want MediaNext to do nothing on an Episode of the Unknown Artwork, so that Flux doesn't guess an order between unrelated files.
34. As a viewer, I want MediaNext to do nothing when the Next Episode is Unavailable, so that I don't land on a Media that can't be played.
35. As a viewer, I want MediaNext not to skip over an Unavailable Next Episode to a later one, so that I never silently miss an Episode.
36. As a viewer, I want my Progress in the current Episode to be saved as-is when I press MediaNext mid-Episode, so that the Episode stays "en cours" and I can resume it later.
37. As a viewer, I want the current Episode to be marked WATCHED when I press MediaNext while the Chapter button shows "Next episode", so that pressing the key at the end counts as finishing the Episode.

### MediaPrevious

38. As a viewer with a keyboard or remote, I want the MediaPrevious key to restart the current Media from the beginning, so that I can rewatch it in one press.
39. As a viewer, I want MediaPrevious to keep playback playing or paused as it was, so that restarting doesn't change the play state.
40. As a viewer, I want pressing MediaPrevious again to restart again rather than go to a previous Episode, so that the key has one single meaning.
41. As a viewer, I want MediaPrevious to work for Movies and Episodes alike, so that the key is always useful.

### Chapter button and Next Episode consistency

42. As a viewer, I want tapping "Next episode" on the Chapter button to mark the current Episode WATCHED, even when its last Chapter is a "Preview" rather than an Ending, so that finished Episodes don't stay "en cours".
43. As a viewer, I want tapping "Next episode" and pressing MediaNext at the same moment to save the same Status, so that the two ways of moving on are equivalent.
44. As a viewer, I want the Chapter button never to offer an Unavailable Next Episode, so that I am never sent to a Media that can't be played.
45. As a viewer, I want the existing arrow keys and Rewind / FastForward keys to keep seeking backward and forward, so that nothing I already use changes.

## Implementation Decisions

### Chapter model

- The Chapter domain model gains a nullable **title**, taken verbatim from the UserFile's chapter metadata. Blank titles are stored as null. Classification into Opening / Ending / Chapter is unchanged and still derived from the title.
- The "Chapter N" fallback is a presentation concern (localised string resource), not stored in the model. N is the Chapter's 1-based position in the list, not its raw index from the container.
- The **current Chapter** for a position is resolved with half-open ranges `[start, end)`: a position equal to a Chapter's `end` belongs to the next Chapter. The same resolution is used for the "Chapters" row value, the check mark, and the Chapter button logic (which currently uses closed ranges), so all three agree. A position before the first Chapter's start, or after the last Chapter's end, has no current Chapter.

### Player state (shared presentation)

- The Chapters of the loaded Media are exposed in the **session** state (rarely changing), alongside Tracks. A derived flag tells the UI whether the "Chapters" row is shown (at least 2 Chapters), so the rule lives in the shared presentation layer and is testable without Compose.
- The **current Chapter** is exposed in the frequently-changing **overlays** state and is only re-emitted when it actually changes (not on every progress tick).
- `SettingsSheet` gains a `Chapters` variant, alongside `Settings` and `Tracks`.
- New intents:
  - **SelectChapter(chapter)**: seeks to the Chapter's start with exact precision, then closes the Settings sheet (no sheet shown). Does not touch play/pause.
  - **OnMediaNext**: resolves the Next Episode through the same lookup and per-Media cache already used by the Chapter button. If none, does nothing. Otherwise saves the current Progress (forcing WATCHED if the Chapter button is currently showing "Next episode"), clears the Chapter button, and switches to the Next Episode — the same path as tapping the Chapter button's "Next episode". It does not display the Chapter button.
  - **OnMediaPrevious**: seeks to 0 with exact precision. Does not touch play/pause.
- Tapping the Chapter button's "Next episode" now forces WATCHED on the current Episode. Both entry points share one "go to Next Episode" routine taking whether to force WATCHED.

### Saving Progress

- `SaveProgressUseCase` gains an explicit input to force the WATCHED Status (default: not forced, preserving today's chapter/threshold rules for every other caller). When forced, the Media is saved WATCHED with its Progress reset to 0, exactly as an organically-watched Media is today, and the history entry is recorded as usual.

### Next Episode lookup

- `GetNextEpisodeUseCase` returns nothing when the Next Episode (next by season then number, in the same Show) is Unavailable. It does not search further for a later available Episode. Movies, the last Episode and Episodes of the Unknown Artwork keep returning nothing. The database query is unchanged; the availability check happens in the use case on the domain model.

### UI (Android app)

- The general Settings sheet renders the "Chapters" row only when the session flag says so, reusing the existing settings row component (label, value, chevron). Value: current Chapter's title, its "Chapter N" fallback, or empty.
- A new Chapters sheet mirrors the Track sheet's structure (dialog, scrolling list, check mark on the selected item). Each item: title (single line, ellipsised) and start time formatted like the seek bar's times.
- The player's key handler maps `Key.MediaNext` to **OnMediaNext** and `Key.MediaPrevious` to **OnMediaPrevious**. Existing mappings (arrows, Rewind, FastForward, Space) are unchanged.
- New localised strings: "Chapters" (row label) and the "Chapter %d" fallback.

### Glossary

- `GLOSSARY.md` is already updated: the Settings sheet can be dedicated to choosing a Chapter, the **Next Episode** term is defined, and the Chapter button refers to it. Use these terms in code comments and strings.

## Testing Decisions

- Good tests here assert **external behaviour**: intents in, emitted state and calls to collaborators (player manager seeks, Progress saves, media switch) out. They must not assert private fields, caching internals or Compose structure.
- **`PlayerViewModel`** is the primary seam and covers most stories. Prior art: the existing `PlayerViewModelTest` (Kotest `FunSpec` + mockk), especially its "chapter button tap", "chapter button from progress", "show settings" and "seek to" sections. Add:
  - Session exposes the Media's Chapters; the "Chapters" row flag is false for 0 and 1 Chapter, true for 2+.
  - Current Chapter follows progress ticks, uses half-open ranges at boundaries, is null before the first Chapter, and is not re-emitted while unchanged.
  - SelectChapter seeks to the Chapter's start with exact precision and closes the Settings sheet; play state untouched.
  - OnMediaPrevious seeks to 0 with exact precision.
  - OnMediaNext with a Next Episode saves Progress unforced and switches Media; while the Chapter button shows "Next episode", saves with WATCHED forced; with no Next Episode, saves nothing and stays on the Media.
  - Chapter button "Next episode" tap saves with WATCHED forced.
  - Existing Chapter button-from-progress cases still pass with half-open ranges (adjust only boundary expectations if any rely on closed ranges).
- **`SaveProgressUseCase`**: forced WATCHED saves WATCHED with Progress 0 regardless of chapters/threshold; unforced behaviour is unchanged. Prior art: `SaveProgressUseCaseTest` ("save progress", "save progress with chapters").
- **`GetNextEpisodeUseCase`**: returns nothing when the next Episode is Unavailable; returns it when available. Prior art: `GetNextEpisodeUseCaseTest`.
- **`AndroidGetChaptersUseCase`**: maps the container's chapter title onto the model; blank → null. Prior art: `AndroidGetChaptersUseCaseTest`.
- The Compose layer (row visibility rendering, list item layout, key-to-intent mapping) is not unit tested; it stays a thin mapping over the tested state and intents.
- Run with `./gradlew :app:testFossDebugUnitTest`.

## Out of Scope

- ⏮ / ⏭ chapter buttons in the Interface.
- Swipe left/right gestures to change Chapter.
- Chapter markers on the seek bar.
- A transient overlay showing the Chapter title after a jump.
- Picture-in-Picture and media notification next/previous actions.
- Going to a previous Episode.
- When the Chapter button's "Next episode" appears (#324) and what happens when an Episode ends (#378).
- Skipping to a later available Episode when the Next Episode is Unavailable.
- Changing Opening / Ending classification rules.
- Cleaning the duplicated "Language" section of `GLOSSARY.md`.

## Further Notes

- Chapters are read once per Media when it loads; the Chapters list does not need to react to file changes during playback.
- `SaveProgressUseCase` re-reads Chapters from the UserFile on every save. Not addressed here, but worth keeping in mind if the Chapter reading becomes heavier.
- MediaNext and the Chapter button share the per-Media Next Episode cache, so pressing MediaNext after the Chapter button has appeared costs no extra database query.
