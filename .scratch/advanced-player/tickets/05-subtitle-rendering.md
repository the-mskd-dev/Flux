# 05: Subtitle rendering

Spec: `../specs.md`, `docs/adr/0002-subtitles-rendered-in-compose-from-media3-cues.md`

**What to build:** Subtitles are drawn from a platform-independent cue model. ASS/SSA Tracks show their basic style (color, bold/italic, position), SRT/WebVTT Tracks look as today, and image Tracks (PGS/VobSub), invisible today, are displayed.

**Blocked by:** None (can start immediately). It is the prefactoring for 06.

**Status:** ready-for-agent

- [ ] First step: confirm on a real ASS file what the Media3 cues carry (color, style, position) and what nextlib adds; note the conclusion in this ticket before building
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
