# 02: Artwork and Show screens use shared page composables

**What to build:** The header, overview, spoiler-masked episode list, identification dialog and reset-progress dialog become common composables used by both the Artwork screen and the Show screen. Each screen keeps its own page composable and its large and regular layouts. This is a prefactor: the user sees no change.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] Each of the five pieces exists once and is used by both screens.
- [ ] Both pages look and behave as before (header, overview, spoiler masking, both dialogs), checked on a device or emulator in regular and large layouts.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
