# Playback launch

## Problem Statement

When I tap play on a Media, Flux either opens it in its internal player or hands it to the external player I chose in the settings. This works, but the behaviour is written four times (Movie page, Season page, Unknown Artwork page and Catalog), each with its own copy of the decision, the fallback to the internal player when the external player cannot start, and the recording of the Progress the external player reports back. As a user I can get inconsistent behaviour from one page to another (a fix in one page does not reach the others). As the maintainer, I cannot test the real behaviour end to end: the Android part that builds the external player request and reads its result has no test at all, and the screens' choice between the two players has no test either.

## Solution

One **Playback launch** module owns the whole journey from "the user asked to play this Media" to "the Progress reported by the external player is remembered". Pages only say "play this Media" and render the outcome (open the internal player, show that the Media is Unavailable, or nothing to do). The Android-specific work (building the request for the external player, starting and stopping its foreground service, parsing the position it returns) sits behind a seam as an adapter. A fake adapter lets the same behaviour be tested without a device.

For the user nothing changes visually: the same Media plays in the same player, with the same fallback and the same Progress handling, on every page.

## User Stories

1. As a viewer, I want to tap play on a Movie and have it open in my chosen player, so that I watch it the way I configured.
2. As a viewer, I want to tap play on an Episode of a Show and have it open in my chosen player, so that I get the same behaviour as for a Movie.
3. As a viewer, I want to tap play on a Media of the Unknown Artwork and have it open in my chosen player, so that Unrecognized files are not a special case.
4. As a viewer, I want to tap play on a Media from the Catalog and have it open in my chosen player, so that I can start without opening an Artwork page.
5. As a viewer who enabled the external player, I want Media to open in that external player, so that I use the player I prefer.
6. As a viewer who did not enable the external player, I want Media to open in the internal player, so that Playback speed, Chapter buttons and Track selection are available.
7. As a viewer, I want a Media that is Unavailable not to start any player, so that I do not land on an error screen.
8. As a viewer, I want to be told clearly that a Media is Unavailable when I try to play it, so that I understand why nothing happens.
9. As a viewer whose external player is not installed, I want Flux to fall back to the internal player, so that playback still starts.
10. As a viewer whose external player refuses the file (permission denied), I want Flux to fall back to the internal player, so that I am not blocked.
11. As a viewer whose external player fails for another reason, I want the same fallback, so that no failure leaves me with nothing.
12. As a viewer, I want the fallback to play the same Media I asked for, so that I do not have to find it again.
13. As a viewer, I want the fallback to resume at my Progress, so that I do not lose my place.
14. As a viewer, I want the internal player fallback not to try the external player again, so that I never loop between the two.
15. As a viewer, I want the external player to start at my saved Progress, so that I resume where I stopped.
16. As a viewer with no saved Progress, I want the external player to start from the beginning, so that a new Media plays from its start.
17. As a viewer, I want the Media title passed to the external player, so that it shows the right name.
18. As a viewer, I want the external player to be allowed to read my UserFile, so that the file opens without me granting anything manually.
19. As a viewer, I want the position reported by the external player when I close it to be saved as the Progress, so that I can resume later.
20. As a viewer, I want that position saved on the Media I launched, not on another one, so that my Progress stays correct even if the page changed meanwhile.
21. As a viewer, I want the Status of the Media (to watch, in progress, watched) updated from the reported position, so that my Catalog stays accurate.
22. As a viewer, I want the Artwork Status summary to follow, so that a Show shows the right overall state.
23. As a viewer, I want the Next Episode logic and History to behave as for the internal player when the external player reports a position, so that both paths feel identical.
24. As a viewer, I want nothing to be saved when the external player returns no position, so that my Progress is not overwritten by zero.
25. As a viewer, I want the external player's various position formats (long or int, different keys) to be understood, so that the common external players work.
26. As a viewer, I want the Android foreground service that keeps Flux alive during external playback to start before the launch and stop after the result or failure, so that the system does not kill Flux while I watch.
27. As a viewer, I want that service stopped even when the launch fails, so that no notification stays stuck.
28. As a viewer, I want a launch to start only once per tap, so that I do not get two players.
29. As a viewer, I want the Season page to keep the selected Episode while the external player is open, so that I come back to the same place.
30. As a viewer, I want the Season page's Spoiler masking to be unaffected by launching playback, so that revealing state is not lost.
31. As a Private Folder user, I want playback from a Private Artwork to follow the same rules, so that privacy does not change how play works.
32. As a maintainer, I want one place that decides internal vs external, so that I fix a bug once.
33. As a maintainer, I want one place that handles the fallback, so that every page gets it.
34. As a maintainer, I want one place that records the external result Progress, so that the four pages cannot drift apart.
35. As a maintainer, I want the Android request building and result parsing behind an adapter, so that I can test the rest without a device.
36. As a maintainer, I want a fake adapter, so that tests can simulate "no player installed", "permission denied" and "returns position X".
37. As a maintainer, I want pages to only render an outcome, so that adding a new page that plays Media needs no player logic.
38. As a maintainer, I want the per-page play Event and external-result Intent duplicates removed, so that the contracts shrink.
39. As a maintainer, I want the Playback launch interface to be KMP-clean (no `android.*`), so that the Isolation Rule holds.
40. As a maintainer, I want the existing view model tests to keep passing with simpler setup, so that the refactor is safe.

## Implementation Decisions

- Introduce a **Playback launch** module in the shared module (commonMain). It is a deep module: its interface is "play this Media" plus "an external player reported a position", and it hides the availability check, the external-vs-internal decision, the fallback and the recording of the result Progress.
- The interface returns an outcome that pages render: open the internal player with a Media, Media is Unavailable, or the launch was handed to the external player. It never exposes Intents or Android types.
- The external-vs-internal decision keeps today's rule: an Unavailable Media never plays; otherwise the external player is used when enabled in the settings and the caller did not ask for the internal player. The availability check exists once (today it is checked twice for Movie).
- The fallback is part of the module: when the external adapter reports a failure (no player, permission denied, other), the module produces the internal-player outcome for the same Media, without retrying the external player.
- The result Progress is recorded by the module on the Media that was launched (not on whatever the page currently shows), using the existing progress-saving behaviour so Status, History and Next Episode rules are identical to the internal player. A null position records nothing.
- A **seam** is placed between the module and an **external player adapter** with two adapters: the Android adapter (production) and a fake (tests). One real second adapter makes the seam real.
- The Android adapter takes over today's external-player utility: building the view request (type, read permission, title and start-position extras), starting and stopping the external-playback foreground service around the launch, launching through the activity result, and parsing the returned position (long or int, several keys). It reports failure kinds to the module.
- Pages (Movie, Season, Unknown Artwork, Catalog) stop declaring their own play Event with an external flag and their own external-result Intent. They call the module and render its outcome. The screens keep only the platform wiring that must live in a composable (the activity result launcher), wired once, shared by the four pages.
- ArtworkViewModel's nullable external-result hook and its per-page copies are removed in favour of the module.
- `ResolvePlaybackActionUseCase` and the `PlaybackAction` result type are absorbed by the module (the deletion test: it was a shallow module whose behaviour reappears in four callers).
- Koin: the module is registered in commonMain; the adapter in androidMain.
- No change to Media, Progress, Status or Artwork page behaviour beyond removing duplication. No ADR is affected (ADR-0001 concerns Manual identification).
- Vocabulary: use Media, Progress, Status, Unavailable, Unknown Artwork, Catalog as defined in GLOSSARY.md. "External player" and "Playback launch" are new terms to add to GLOSSARY.md when the module is named.

## Testing Decisions

- A good test crosses the Playback launch interface and asserts observable outcomes (which outcome is returned, what Progress and Status are saved, what the adapter was asked to launch), not which internal steps were called.
- Tested module: Playback launch, through its interface only, with the fake external player adapter and the existing fakes for settings and progress (the Progress/Status/History rules stay covered by their own existing tests).
- Cases: Unavailable Media; internal player when the external player is disabled; external player when enabled; forced internal; each failure kind falls back to internal for the same Media; fallback does not retry external; result Progress recorded on the launched Media; null position records nothing; Status transitions from the reported position; launch twice does not double-record.
- Android adapter: tested separately as a JVM test of Intent extras (start position vs from-start, title keys, read permission flag) and position parsing (long, int, missing, several keys). Only that adapter-specific logic, not the module's behaviour.
- View model tests for Movie, Season, Unknown and Catalog shrink: they assert that playing delegates to the module and renders its outcome; the decision logic is no longer re-tested per page.
- Prior art: `ResolvePlaybackActionUseCaseTest` (decision cases move to the module test), `SaveProgressUseCaseTest` and `ChangeMediaStatusUseCaseTest` (fake-based behaviour tests), and the Sync behaviour tests with a shared harness and fake adapter (`SyncHarness` with `FakeTmdb`) as the model for the harness style.
- Note: all existing tests live in the Android module's unit tests, including those for shared code; new tests follow that convention.

## Out of Scope

- The internal player (PlayerViewModel, PlayerManager, Picture-in-Picture, track preferences).
- Changing which external players are supported or their intent extras.
- New settings for the external player.
- Moving the Movie, Season, Unknown, Catalog screens from the app module to shared.
- Changes to Status, History, Private Folder or Next Episode rules.
- An iOS adapter (Android only for now; the seam allows one later).

## Further Notes

- Evidence from the architecture review: the same play function and screen wiring appear in Movie, Season, Unknown and Catalog; the Android utility has no tests.
- Natural follow-ups from the same review: Status/History invariants as one home, and finishing the app → shared migration of the Artwork pages (which this change unblocks).
- Terms to add to GLOSSARY.md when implemented: **Playback launch**, **External player**.
