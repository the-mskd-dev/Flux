# Spec: Better Opening / Ending detection from Chapter titles

Source: #388 (scope narrowed during triage). Related: #370 (chapter navigation, separate).

## Problem Statement

When I watch an anime episode or a movie in Flux, the Chapter button only offers "Skip opening" / "Skip ending" when the Chapter titles in my UserFile match a narrow English vocabulary that must sit at the start of the title ("OP", "Opening", "ED", "Ending", "Credits"…). Many releases name their Chapters differently: "Song Name OP", "Credits Start", Japanese titles like オープニング / エンディング, or full-width ＯＰ / ＥＤ. For those files, no button appears and I have to scrub through the generic by hand.

The reverse also happens. Flux sometimes offers to skip something I want to watch. A Chapter titled "Post-Credits" or "Credits Scene" is treated as an Ending, so the Chapter button offers to skip a Marvel post-credit scene. A marker Chapter like "OP End" is treated as an Opening.

## Solution

Flux keeps classifying each Chapter from its own title only, with no guessing from position or duration. The vocabulary becomes broader and stricter:

- More real-world Opening and Ending titles are recognised, based on what other skip tools use (mpv chapterskip / SmartSkip, Jellyfin Intro Skipper, AniSkip segment types).
- End markers and post-credit scenes are no longer treated as Opening or Ending.

Files whose Chapters carry generic names ("Chapter 01", "Chapter 02"…, as most VCB-Studio releases likely do) still get no Opening/Ending. Nothing in a generic name tells us what the Chapter contains, and no other open-source player we checked classifies them either. Navigating between those Chapters is covered by #370.

## User Stories

1. As a viewer, I want a Chapter titled "OP" or "Opening" to keep being recognised as an Opening, so that existing files keep working.
2. As a viewer, I want numbered openings like "OP1" or "OP2" to be recognised as an Opening, so that multi-opening series work.
3. As a viewer, I want "NCOP", "Intro" and "Introduction" to keep being recognised as an Opening, so that existing files keep working.
4. As a viewer, I want a Chapter whose title ends with "OP" (e.g. "Song Name OP") to be recognised as an Opening, so that releases naming the song first are handled.
5. As a viewer, I want "Mixed OP" to be recognised as an Opening, so that files using AniSkip-style segment names are handled.
6. As a viewer of Japanese releases, I want a Chapter titled オープニング to be recognised as an Opening, so that I can skip it.
7. As a viewer of Japanese releases, I want a Chapter titled ＯＰ (full-width) to be recognised as an Opening, so that I can skip it.
8. As a viewer, I want "ED", "ED1", "Ending", "NCED", "Outro", "Credits", "End Credits" and "End" to keep being recognised as an Ending, so that existing files keep working.
9. As a viewer, I want a Chapter whose title ends with "ED" (e.g. "Song Name ED") to be recognised as an Ending, so that releases naming the song first are handled.
10. As a viewer, I want "Credits Start" to be recognised as an Ending, so that files using start-marker naming are handled.
11. As a viewer of Japanese releases, I want a Chapter titled エンディング or ＥＤ to be recognised as an Ending, so that I can skip it.
12. As a viewer, I want title matching to ignore case, so that "opening", "OPENING" and "Opening" behave the same.
13. As a viewer, I want "OP End", "Opening End" or "Ending: End" marker Chapters not to be treated as Opening/Ending, so that I am not offered a wrong skip.
14. As a viewer, I want "Post-Credits", "Post Credit Scene" and "Credits Scene" Chapters not to be treated as an Ending, so that I am never offered to skip a post-credit scene.
15. As a movie viewer, I want each credits Chapter in a "credits → post-credit scene → credits → post-credit scene → credits" sequence to be recognised as an Ending on its own, so that I can skip each credits block and still watch every post-credit scene.
16. As a viewer, I want an Opening placed in the middle of an episode (after a long cold open) to be recognised, so that detection doesn't depend on where the Opening sits.
17. As a viewer, I want an episode with no Opening, or no Ending, to still get the other one recognised, so that Opening and Ending detection are independent.
18. As a viewer, I want Chapters like "Prologue", "Avant", "Part A", "Part B", "Preview", "PV" or "Recap" to stay ordinary Chapters, so that Flux never offers to skip story content.
19. As a viewer, I want words that merely start like a keyword ("Opera", "Edward", "Introspection", "Endgame", "Credit Union Heist") not to be recognised as Opening/Ending, so that I am not offered wrong skips.
20. As a viewer, I want generic Chapter names ("Chapter 01", "Chapter 1", digits only, empty title) to stay ordinary Chapters, so that Flux never guesses.
21. As a viewer, I want a Chapter without any title to stay an ordinary Chapter, so that missing metadata never triggers a skip.
22. As a viewer, I want the Chapter button behavior (when it appears, where a skip lands, Next episode on a final Ending) to stay exactly as it is today, so that only the detection quality changes.

## Implementation Decisions

- **Single module touched:** the Chapter type classification in the player domain (`VideoChapterType.fromString(value: String?): VideoChapterType`). The signature and the three types (OPENING, ENDING, CHAPTER) stay the same.
- **Per-title classification only:** each Chapter is classified independently from its title. No position or duration heuristic, no look at neighbouring Chapters, no fallback for files with generic or missing titles. The maintainer chose this to avoid false positives.
- **Only data from the file:** no external source (AniSkip) and no audio fingerprinting.
- **Precedence:** exclusions are checked first (end markers, post-credit scenes → CHAPTER), then OPENING, then ENDING, else CHAPTER.
- **OPENING vocabulary (case-insensitive):**
  - starts with `op` (optionally followed by digits), `opening`, `ncop`, `intro`, `introduction`, `mixed op`;
  - or ends with ` OP`;
  - or contains オープニング;
  - or is / starts with ＯＰ.
- **ENDING vocabulary (case-insensitive):**
  - starts with `ed` (optionally followed by digits), `ending`, `nced`, `outro`, `credit(s)`, `end credits`, `credits start`;
  - or is exactly `end`;
  - or ends with ` ED`;
  - or contains エンディング;
  - or is / starts with ＥＤ.
- **Exclusions:**
  - keyword followed by an "End" marker (`OP End`, `Opening End`, `Ending: End`), following Jellyfin Intro Skipper's `(?![\s:]+End)` lookahead;
  - titles containing `post` + `credit` or `credits scene`.
- **Word boundaries:** a keyword only matches when followed by a digit, a non-letter, or the end of the title. Matching must not depend on Java's ASCII-only `\W` to delimit Japanese / full-width text: those terms are matched literally.
- **Unchanged:** `VideoChapter`, `GetChaptersUseCase` / `AndroidGetChaptersUseCase` (NextLib reading), `PlayerViewModel.listenProgress` and the Chapter button. They already handle any number of Endings and Openings at any position.
- **Glossary:** Chapter, Opening and Ending are defined in `GLOSSARY.md` (Lecteur section). Opening/Ending are types of Chapter, and a Media can have several Endings.

## Testing Decisions

- **One seam:** `VideoChapterType.fromString`. It is a pure domain function, so tests need no Android, NextLib or ViewModel mocks.
- **Good tests check external behavior only:** title in → type out. They must not assert on regex internals or on how the vocabulary is stored.
- **Style:** a Kotest `FunSpec` with data-driven cases via `withData` (title → expected type), grouped by expected type. Prior art: `AndroidGetChaptersUseCaseTest` and `SaveProgressUseCaseTest` (`FunSpec`, `fluxExtensions()`, `withData`).
- **Cases to include:** every title named in the user stories above. That covers the regression list of currently recognised titles, the new positives, the exclusions, look-alike words, generic names, `null` and empty.
- **No new tests elsewhere:** `AndroidGetChaptersUseCaseTest` already covers the NextLib → `VideoChapter` mapping. The existing `PlayerViewModelTest` covers Chapter button behavior and must keep passing.
- **Verification command:** `./gradlew :app:testFossDebugUnitTest`.

## Out of Scope

- Any heuristic based on Chapter position or duration, and any detection for files with generic or missing Chapter titles.
- External sources (AniSkip, online databases) and audio fingerprinting.
- Auto-skip, and a Settings option to enable/disable or configure the Chapter button (planned v2).
- Previous/next Chapter navigation (⏮️/⏭️), swipe navigation, and Chapter markers on the seek bar (#370).
- New Chapter types (Preview, Recap, Prologue) and any change to Chapter button behavior.
- Manual metadata matching (requested in a #388 comment; separate topic).

## Further Notes

- Research summary: mpvKt, mpvEx, DreamPlayer and Next Player only list or navigate Chapters. Aniyomi and Dantotsu rely on AniSkip. SVP Player is closed source. Only the desktop mpv scripts (po5/chapterskip, Eisa01/SmartSkip) and Jellyfin Intro Skipper classify Chapters by title, and the vocabulary above is the union of theirs. None of them classifies generic names.
- VCB-Studio releases very likely use mkvmerge's default `Chapter <NUM:2>` names when encoding Blu-rays. This is unconfirmed: the reporter's actual Chapter titles were not collected.
- No reply was posted on #388 (maintainer's choice). The issue carries `feature` + `ready-for-agent`.
