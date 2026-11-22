# Render subtitles in Compose Multiplatform instead of Media3's SubtitleView

The Internal player renders native libass/FFmpeg bitmap cues (ASS/SSA, PGS, VobSub, DVB) and styled text cues (SRT, WebVTT, TTML) through Flux's own Compose Multiplatform `PlayerSubtitles` composable in `shared/commonMain`, rather than embedding Media3's Android `SubtitleView`. Because libass and FFmpeg already rasterize every advanced ASS/SSA effect (karaoke, vector drawings, animations, embedded fonts, positioning) and bitmap Track into RGBA bitmaps with normalized bounds, passing a zero-copy platform-backed image (`expect`/`actual`) into `PlayerSubtitles` produces pixel-identical native output at up to 30 fps while preserving Flux's Material 3 pill styling for plain text Tracks and keeping subtitle rendering reusable across future KMP/CMP targets.

## Considered Options

- **Media3 `SubtitleView` via `AndroidView`**: battle-tested out-of-the-box support for both bitmap cues and rare text features (WebVTT/TTML ruby, vertical CJK text, CEA-608/708 broadcast grids) with no cue mapper to maintain, but Android-only and replaces Flux's rounded Material 3 pill look on SRT/WebVTT with `CaptionStyleCompat` boxes or outlines.
- **PNG-encoded `ByteArray` in `SubtitleCue`** (previous approach): pure Kotlin in `commonMain`, but compresses and decompresses every bitmap cue on every frame, causing frame drops and lag during 30 fps libass karaoke and animation sequences.

## Consequences

- Bitmap cues must travel through an `expect`/`actual` platform image handle (wrapping Android's `Bitmap` directly with `asImageBitmap()` and O(1) reference equality) — never PNG-encoded in `CueMapper`.
- `PlayerSubtitles` renders three layers: bitmap cues at their normalized bounds in the fitted picture box, explicitly positioned text cues, and a bottom stacked `Column` for unpositioned text cues (both plain and inline-styled) so simultaneous lines never overlap.
- No layer follows the pinch-to-fill zoom. libass renders ordinary ASS dialogue as bitmaps too, so zooming the bitmap layer pushed dialogue off-screen; subtitles keep their size and position instead, and authored ASS signs and drawings drift from the zoomed picture in fill mode.
- Rare non-ASS text layout features such as WebVTT/TTML ruby annotations, vertical text, and CEA-608/708 broadcast grids are intentionally unsupported.
