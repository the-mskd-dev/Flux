# Status, History and Private rules

## Problem Statement

Flux remembers where I am in a Media (Progress), whether I watched it (Status), and offers a list of what to resume (the History, the "recently watched" list on the home screen). It also hides Private Artwork from everything outside the Private Folder.

The rules that tie these together are written in several small use cases, each with its own copy, and the copies do not agree:

- "Which Media goes into the History after this Media changed?" is written twice (when I save playback Progress, and when I change a Status by hand). They differ: saving Progress skips Private Artwork and the Unknown Artwork; changing a Status does not. So marking an Episode of a Private Artwork as watched can put it in the History, which the Private Folder promises never to happen.
- "A Private Artwork never appears in the History" is enforced in three places (saving to the History, making an Artwork private, marking NSFW Artwork private), each with its own comment, and not at all in the fourth (changing a Status).
- "Reset to to watch at zero" is written in three use cases.
- The pure rule "WATCHED if the position falls in the last Ending chapter, otherwise compare to the threshold" cannot be tested without faking the file-reading adapter that finds the Chapters.

As the maintainer I cannot test how these rules interact, and as a user I can see a Private Artwork leak into the History or a History entry that disagrees with my Status.

## Solution

One **Progress module** owns the rules for Progress, Status, History membership and Private exclusion. Callers say what happened ("playback reached position P", "the user set this Status", "the user reset this Artwork", "this Artwork became private"); the module updates the Media and the History consistently. The History rule exists once and treats Private Artwork and the Unknown Artwork the same way from every entry point.

For the user: the History, Status and Private Folder always agree, whichever path changed them.

## User Stories

1. As a viewer, I want my playback position saved as the Progress of the Media, so that I can resume where I stopped.
2. As a viewer, I want a Media I stopped before the end to become "in progress", so that it shows as started.
3. As a viewer, I want a Media I watched to the end to become "watched" with its position back at zero, so that it is clearly finished.
4. As a viewer, I want a Media to become watched when I reach the last Ending chapter, so that I do not have to wait for the credits.
5. As a viewer, I want a Media whose last Chapter is not an Ending to stay "in progress" when I am inside it, so that a post-credits scene is not mistaken for the end.
6. As a viewer, I want Media without Chapters to become watched past the usual progress threshold, so that files without Chapter data still work.
7. As a viewer, I want to be able to force a Media as watched when I finish it or choose "mark as watched", so that the Chapters and the threshold do not matter.
8. As a viewer, I want the Media I am watching to appear in the History, so that I can resume from the home screen.
9. As a viewer, I want a Movie I stopped midway to appear in the History, so that I can continue it.
10. As a viewer, I want a Movie I finished to leave the History, so that it is not offered again.
11. As a viewer watching a Show, I want my in-progress Episode to be the History entry of that Show, so that resuming takes me to it.
12. As a viewer, I want a finished Episode to be replaced in the History by the Next Episode, so that the Show offers what to watch next.
13. As a viewer, I want the Show to leave the History when I finish its last Episode, so that a completed Show is not offered.
14. As a viewer, I want only one History entry per Artwork, so that a Show does not appear several times.
15. As a viewer, I want an Episode of the Unknown Artwork never to enter the History as a Show, so that unrecognised files do not pose as a series.
16. As a viewer, I want a Private Artwork never to appear in the History, so that the Private Folder keeps its promise.
17. As a viewer, I want this to hold whether the Progress came from the internal player, an external player, or a manual Status change, so that no path leaks.
18. As a viewer, I want an Artwork I make private to leave the History immediately, so that it disappears everywhere at once.
19. As a viewer, I want the NSFW Artwork marked private (when I enable the Private Folder or the NSFW option) to leave the History, so that they disappear everywhere.
20. As a viewer, I want an Artwork I move out of the Private Folder to keep its Status and Progress, so that nothing is lost, and to appear in the History again only when I next watch it.
21. As a viewer, I want to mark a Movie as watched and have it leave the History, so that it matches its Status.
22. As a viewer, I want to mark an Episode as watched and have the Next Episode become the History entry, so that the Show offers what to watch next.
23. As a viewer, I want to mark the last Episode of a Show as watched and have the Show leave the History, so that a finished Show is not offered.
24. As a viewer, I want marking a Media as watched to put its position back at zero, so that replaying starts from the beginning.
25. As a viewer, I want to mark a Media as "to watch" and have its position reset to zero, so that it is a fresh Media again.
26. As a viewer, I want marking a Media "to watch" not to put another Episode in the History as a side effect, so that an undo does not advance my Show.
27. As a viewer, I want "mark previous Episodes as watched" to mark every earlier, not-yet-watched Episode of the Show, so that I can catch up in one action.
28. As a viewer, I want that action to leave already-watched Episodes untouched, so that nothing is overwritten.
29. As a viewer, I want that action to do nothing when there is nothing to mark, so that no empty write happens.
30. As a viewer, I want to reset the Progress of a Movie, a whole Show, or one Season, so that I can watch again from scratch.
31. As a viewer, I want a reset to leave Episodes that are already "to watch" at zero untouched, so that no unnecessary change happens.
32. As a viewer, I want a reset to remove the Artwork from the History, so that no stale entry remains.
33. As a viewer, I want the Artwork Status (summary of its Media) to follow every change, so that the Catalog is accurate.
34. As a viewer, I want the History to show only Media whose source is available, so that I do not tap something I cannot play.
35. As a viewer, I want the History sorted with the most recent first, so that what I just watched is on top.
36. As a viewer, I want the Next Episode offered in the player only when it is available, so that I am not sent to an Unavailable Episode.
37. As a maintainer, I want one place that decides the History entry for an Artwork, so that a fix applies to every entry point.
38. As a maintainer, I want one place that enforces Private exclusion, so that a new entry point cannot forget it.
39. As a maintainer, I want one definition of "Media reset to to watch at zero", so that three copies cannot drift.
40. As a maintainer, I want to test the Status rule from a position without faking the file-reading adapter inside every test, so that rule tests stay short.
41. As a maintainer, I want one test surface that exercises Progress, Status, History and Private rules together, so that their interplay is covered.
42. As a maintainer, I want the Next Episode lookup in one place, with the "available" filter explicit, so that the player and the History cannot disagree by accident.

## Implementation Decisions

- Introduce a **Progress module** (shared, commonMain, pure Kotlin). Its interface has five operations from the callers' point of view: record a playback position for a Media (with an optional "force watched"), change the Status of a Media, mark the Episodes before an Episode as watched, reset the Progress of an Artwork (whole Artwork or one Season), and set an Artwork private or public. It also exposes the Next Episode of a Media. It returns the updated Media where callers need it (as the position-saving operation does today).
- The module hides: the Status decision from a position (last Ending chapter, last non-Ending chapter, threshold fallback), the "watched means position zero" rule, the single **History rule**, and the Private/Unknown exclusion. Today's use cases (record progress, change status, mark previous, reset, save to history, set privacy, mark NSFW private, get next Episode) are folded into it or kept as thin entry points that delegate; callers (Movie, Season, Show, Unknown, Catalog and Player view models, and the settings/Private Folder flows) do not change in what they ask for.
- **History rule (unified on the current "save to History" rules).** For any entry point:
  1. An Artwork that is Private, or the Unknown Artwork, never gets a History entry. Making an Artwork private, or marking NSFW Artwork private, removes its entry (as today).
  2. A Media that is in progress becomes the History entry of its Artwork (Movie or Episode).
  3. A watched Episode makes the Next Episode the History entry; with no Next Episode, the Artwork has no entry. A watched Movie has no entry.
  4. One entry per Artwork.
- **Open point to settle before implementation (to-watch).** The "save to History" rule only ever sees watched and in-progress Media, so it has no answer for a manual change to "to watch". Today a manual change to "to watch" leaves the History alone, except that the entry is removed when the Episode has no Next Episode. Proposed default: keep exactly that outcome for "to watch" (no new entry is created, so an undo never advances a Show) and apply the unified rule only for watched and in-progress. The implementer confirms or changes this with the owner; it is the only deliberate behaviour decision left.
- **Behaviour changes relative to today (deliberate):** (a) a manual Status change on a Private Artwork or on the Unknown Artwork no longer creates a History entry; (b) the History rule is the same for every entry point. No other user-visible change.
- The Chapters lookup stays an adapter behind a seam (Android reads the file; tests use a fake). The module takes it as a dependency; the Status-from-position rule is tested through the module with a fake that returns Chapters.
- The History repository and the Catalog database remain the persistence seams. The History rule uses the Catalog's Next Episode query; the "available" filter is explicit: the player's Next Episode requires an available Episode, a History entry is hidden at read time when its source is unavailable (existing behaviour).
- Reading the History stays as today (filtered by available sources, newest first) and is not changed.
- Koin: one registration for the Progress module in commonMain; the former per-use-case registrations are removed or point at it. The Chapters adapter registration stays in androidMain.
- GLOSSARY.md: Progress lists "History" under _Avoid_ and there is no term for the "recently watched" list of resumable Media. Choose and add one term when implementing (proposal: **Resume list**) and use it for the concept called History in code, or document that "History" is accepted. Also add **Progress module** only if it names a concept the user sees; otherwise it stays an implementation name.
- No ADR is affected (ADR-0001 concerns Manual identification).

## Testing Decisions

- A good test crosses the Progress module's interface and asserts what the user would observe: the Media's Status and position, the History entries, the Artwork Status summary. It never asserts which repository method was called or in what order.
- One seam: the Progress module interface. Adapters: an in-memory Catalog (database fake), a fake History repository (exists), a fake Chapters adapter (the Chapters use case). The Chapters adapter has two implementations (Android file reader, fake), so the seam is real.
- Cases: position in the last Ending chapter, in a last non-Ending chapter, outside Chapters, no Chapters with threshold below/above; force watched ignoring Chapters; Movie in progress/finished; Episode in progress, watched with a Next Episode, watched as last Episode; one entry per Artwork; Private Artwork never enters the History from any entry (record, Status change, mark previous); Unknown Artwork never enters; making private and NSFW marking remove entries; changing a Status on a Movie/Episode (watched, to watch per the open point); mark previous marks only unwatched earlier Episodes and does nothing when empty; reset for Movie, Show, one Season, leaves untouched Media alone, removes the History entry; Next Episode only when available and not for Movies, Unknown Artwork or the last Episode.
- The three pairs of rules that diverged today get explicit tests proving they now agree (Private via Status change; History entry after a Status change versus after a recorded position).
- Existing tests for the individual use cases are folded into the module tests (ChangeMediaStatus, MarkPreviousAsWatched, ResetProgress, SaveProgress, SaveToHistory) keeping every case; view model tests keep only "asks the Progress module for X".
- Prior art: the progress use case tests with `ProgressUCTestCases` and `FakeHistoryRepository`; the Sync harness style (public entry + fake adapters) for the "one seam" shape.
- All tests live in the Android module's unit tests, including those of shared code; new tests follow that convention.

## Out of Scope

- The player's chapter-button rule, the seek overlay, Boost speed and other Player behaviour.
- Playback launch (see the Playback launch spec) and the external player.
- Reading, sorting or filtering the History for display (the home screen), other than the Next Episode availability rule above.
- Changing the Progress threshold, the rewind setting or the Chapter detection.
- Private Folder PIN handling and the Private Folder screens.
- Database schema changes (the existing one entry per Artwork History table is kept).
- Spoiler masking and Season summaries.

## Further Notes

- Evidence from the architecture review: the "next Episode goes to the History, else delete" rule appears in two use cases that differ; "Private never in the History" appears in three use cases with their own comments and is missing from a fourth; "to watch at zero" is copied in three; the Next Episode lookup is called raw in two use cases and wrapped in a third.
- The Private gap in the Status change is the one behaviour fix in this spec; call it out in the changelog when it ships.
- Related specs from the same review: `.scratch/playback-launch/`, `.scratch/language-update/`.
