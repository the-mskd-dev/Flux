# 05: Subtitle rendering

Spec: `../specs.md`, `docs/adr/0002-subtitles-rendered-in-compose-from-media3-cues.md`

**What to build:** Subtitles are drawn from a platform-independent cue model. ASS/SSA Tracks show their basic style (color, bold/italic, position), SRT/WebVTT Tracks look as today, and image Tracks (PGS/VobSub), invisible today, are displayed.

**Blocked by:** None (can start immediately). It is the prefactoring for 06.

**Status:** ready-for-agent

- [x] First step: confirm on a real ASS file what the Media3 cues carry (color, style, position) and what nextlib adds; note the conclusion in this ticket before building

  **Spike conclusion (Pixel 9 Pro XL, `Fire Force.S01E15.mkv`, embedded `S_TEXT/ASS`, French track):**
  - The selected track's format is `application/x-media3-cues` with `codecs=text/x-ssa`: Media3's SSA parser produces the cues, so they are **text cues, not libass bitmaps**. nextlib's `FfmpegTextRenderer` (libass) did not take this track because the sample MIME is no longer `TEXT_SSA` at render time.
  - Each dialogue cue carries `ForegroundColorSpan` (color, `ffffffff` for the default style), `StyleSpan` (bold, `style=1`), `line` as a fraction (`lineType=0`, `FRACTION`), `lineAnchor`, `position` with `positionAnchor`, `textAlignment` and `textSize` as a fraction. Bottom dialogue: `line=0.95, lineAnchor=2`. A top sign: `line=0.05, lineAnchor=0`.
  - `size` and `bitmapHeight` are `NO_VALUE`. Italic and colored signs in the sample file were not reached.
  - **Not verified:** PGS/VobSub. The MKV files on the device carry only ASS or UTF-8 (`The Punisher 2026.mkv`); no test file with image subtitles was found.
  - **Sidecar subtitles:** the debug build cannot read a `.ass` placed next to a video in `Movies` (`MediaProvider: Permission ... denied`). That is a separate bug, not in this ticket's scope.
- [ ] A pure Kotlin `SubtitleCue` exists in common code (text, color, bold, italic, position, optional bitmap) with no `androidx` or `android` import
- [ ] The player manager exposes cues as this model instead of a list of strings, and the Android mapping from Media3 cues is the only place that knows Media3 cue types
- [ ] An ASS/SSA Track shows color, bold/italic and position from the file; a translated sign appears where the file puts it
- [ ] A Track without style (SRT, WebVTT) looks exactly as before (black pill, centered, bottom)
- [ ] An image Track displays its bitmap at the position it asks for
- [ ] Animations, karaoke and embedded fonts are not attempted
- [ ] The rendering composable is written in Compose Multiplatform in the shared module
- [ ] Player manager tests: mapping of a styled cue, an unstyled cue and a bitmap cue
- [ ] Manual check on a device with an ASS file, an SRT file and a PGS/VobSub file
- [ ] `./gradlew :app:testFossDebugUnitTest` passes
