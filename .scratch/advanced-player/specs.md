# Customizable player interface

Source: GitHub issue #424 (libmpv engine + mpvKt-like interface request), reduced to what Media3/ExoPlayer + nextlib can deliver. See ADR 0002 (subtitles rendered in Compose from Media3 cues).

## Problem Statement

The Player is simple but offers little control. The user cannot change the Playback speed, cannot see the technical details of the file being played, sees Chapters only as a list in a sheet (the progress bar gives no hint of where Chapters start or what they are called), and sees ASS/SSA subtitles stripped of their style: translated signs appear at the bottom, mixed with dialogue, and image subtitles (PGS/VobSub) do not appear at all. Subtitles that are out of sync cannot be corrected.

## Solution

Keep the Media3/ExoPlayer + nextlib engine and the single Material 3 Interface, and enrich it:

- A Playback speed selector (0.25x to 2x) and a Boost speed (2x while pressing and holding the video).
- A Media info screen in the Settings sheet.
- A progress bar split at Chapter boundaries, with the Chapter name shown while scrubbing.
- Subtitles rendered from a platform-independent cue model: ASS/SSA Tracks show their basic style, unstyled Tracks look as today, image Tracks become visible.
- A Subtitle delay control, if a clean technique is found (spike first).

## User Stories

1. As a viewer, I want to choose a Playback speed between 0.25x and 2x, so that I can slow down a fast dialogue or speed through a slow scene.
2. As a viewer, I want the Playback speed to start at 1x for each new Media, so that I am never surprised by a leftover speed.
3. As a viewer, I want the Playback speed choice to be visible in the Settings sheet, so that I know which speed is active.
4. As a viewer, I want to press and hold the video to play at 2x, so that I can skim without opening a menu.
5. As a viewer, I want the previous Playback speed back as soon as I release, so that the Boost speed never sticks.
6. As a viewer, I want a "2x" pill while the Boost speed is active, so that I know why the video is fast.
7. As a viewer, I want the Boost speed to ignore long presses during a pinch zoom or a volume/brightness gesture, so that gestures do not conflict.
8. As a viewer, I want no Boost speed in Picture-in-Picture, so that it cannot trigger by accident there.
9. As a viewer, I want a Media info screen, so that I can check the codec, resolution, frame rate and bitrate when something plays badly.
10. As a viewer, I want to see the audio codec, channels and sampling rate in Media info, so that I can tell why audio is missing or downmixed.
11. As a viewer, I want Media info to show a placeholder for unknown values, so that a missing value never looks like a bug.
12. As a viewer, I want the progress bar split at Chapter boundaries, so that I see the structure of an episode at a glance.
13. As a viewer, I want to see the Chapter name and the time while I scrub, so that I can jump to the right place.
14. As a viewer, I want a plain continuous bar when the Media has fewer than 2 Chapters, so that a single block is not pretended to be structure.
15. As a viewer, I want scrubbing to stay as smooth as today, so that the new bar is not a regression.
16. As an anime viewer, I want ASS/SSA subtitles to keep their color, bold/italic and position, so that translated signs appear where they belong.
17. As a viewer, I want SRT and WebVTT subtitles to look like today, so that nothing changes where there is no style to respect.
18. As a viewer of Blu-ray/DVD rips, I want image subtitles (PGS/VobSub) to be displayed, so that I can read them at all.
19. As a viewer, I want to shift the subtitles by a few hundred milliseconds, so that I can fix an out-of-sync file.
20. As a viewer, I want the Subtitle delay to reset to 0 for each new Media, so that one file's correction never leaks into another.
21. As a viewer, I want the Subtitle delay to work on ASS and plain text Tracks, so that sync is fixable whatever the format.
22. As a viewer, I want nothing to break for the external player, so that the existing setting keeps working.
23. As a maintainer, I want the new models and states in common code and the new UI without Android API in Compose Multiplatform, so that Desktop and iOS can reuse them.
24. As a maintainer, I want a documented decision about libmpv and libass, so that the next person knows why they are postponed.

## Implementation Decisions

- Engine: Media3/ExoPlayer + nextlib stay. No second engine and no alternative interface; the external player boolean is unchanged. libmpv, mpv.conf, input.conf, filters and interpolation are not part of this work (ADR 0002).
- Contract (MVI): the player contract gains intents and state for Playback speed, Boost speed, Media info and Subtitle delay. The player manager interface gains the matching operations and exposes Media info.
- Playback speed: 0.25x to 2x by steps of 0.25, picked in the Settings sheet; applied through the player's playback parameters; reset to 1x when a new Media loads.
- Boost speed: long press anywhere on the video surface; a "2x" pill at the top in the Ambient overlay style; restores the Playback speed on release; refused in PiP, during a zoom and during a volume/brightness gesture. The Playback speed value is kept in state so the Boost speed can restore it.
- Media info: pure Kotlin model in common code with container, video codec, resolution, frame rate, bitrate, audio codec, channels and sampling rate. On Android it is built from the player's track formats, completed by nextlib-mediainfo if needed. Unknown values are optional and rendered with a placeholder. A new page of the Settings sheet shows it, read-only.
- Chapter progress bar: segments proportional to Chapter duration with a gap at each boundary; a bubble with time and Chapter name follows the thumb while scrubbing; continuous track when there are fewer than 2 Chapters; the existing scrubbing behaviour (fast seek while dragging, exact seek on release, settle logic) and the wavy style during playback are kept. Written in Compose Multiplatform in the shared module.
- Subtitle cues: a pure Kotlin `SubtitleCue` in common code (text, color, bold, italic, position, optional bitmap) replaces the list of strings exposed by the player manager. Android maps Media3 cues into it. A Compose Multiplatform composable renders it: styled cues use the file's style, cues without style use the current look, bitmap cues are drawn as images. No Subtitle style setting.
- Subtitle delay: ± milliseconds, all text Tracks, per Media, reset to 0. Media3 has no offset API, so the work starts with a time-boxed spike; if no clean technique exists, the delay is dropped and reported (no fragile workaround, no custom subtitle decoder just for this).
- Architecture: new models and states in `shared/commonMain`; new composables without Android API in the shared module (CMP); existing Player composables in the app module stay in place; platform-specific code is limited to the Media3 manager, gestures and window handling.
- Glossary terms: Playback speed, Boost speed, Media info, Subtitle delay (already added).

## Testing Decisions

- A good test checks external behaviour: state exposed after an intent, values handed to the player, cues produced from a given input. It does not assert on internal calls or private structure.
- Main seam: the Player ViewModel driven through its single intent entry point with a fake player manager. It covers Playback speed (choice, reset on new Media), Boost speed (start, release, refusal in PiP / during a zoom / during volume-brightness gesture), Media info exposure, Chapter data and scrubbed-position Chapter lookup, and Subtitle delay (intent, reset per Media). Prior art: the existing Player ViewModel tests (Kotest FunSpec, MockK, Turbine) and their test-case fakes.
- Second seam: the Android player manager, for the Media3 boundary: Cue to SubtitleCue mapping (styled, plain, bitmap), track format to Media info mapping including unknown values, speed applied to the player, and the delay mechanism if the spike succeeds. Prior art: the existing Android player manager tests.
- Composables are not unit tested; they are verified manually on a device (build, install, logcat dump per project rules).
- Every ticket ends with the unit test suite passing.

## Out of Scope

- libmpv, mpv.conf, input.conf, filters, scaling, interpolation, hardware decoding options.
- A second engine, an "Advanced player" option or an mpvKt-style alternative interface.
- Subtitle style settings (color, background, opacity, outline, size).
- ASS animations, karaoke, embedded fonts (would need libass).
- Persisting the Playback speed or the Subtitle delay.
- Desktop and iOS implementations (only keeping the design ready for them).

## Further Notes

- The ticket "Subtitle rendering" changes the player manager's subtitle contract and is the prefactoring for the Subtitle delay ticket.
- Not verified yet: what nextlib already bundles for subtitles, and what the Media3 SSA parser puts in cues. The Subtitle rendering ticket starts by confirming it.
- Today image subtitle Tracks produce no text and therefore no visible subtitle at all.
