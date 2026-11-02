# 06: A tap on the image shows or hides the Gallery viewer controls

**What to build:** In the Gallery full-screen viewer, the controls (Close, Use as Poster/Backdrop, Download) are hidden when the viewer opens. A single tap on the image fades them in; another single tap fades them out. Once shown, they fade out by themselves after 3 s without interaction. Tapping one of the buttons restarts the 3 s delay. The controls never hide by themselves while an Image download is running.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] When the viewer opens, no control is visible.
- [ ] A single tap on the image shows the controls; a single tap while they are visible hides them.
- [ ] Controls appear and disappear with a fade animation (no abrupt pop).
- [ ] A single tap is recognised 200 ms after release (double-tap timeout lowered from the platform default of about 300 ms, scoped to the viewer only).
- [ ] A double-tap still zooms in/out and does not change the visibility of the controls; pinch and pan do not change it either.
- [ ] Shown controls hide automatically after 3 s with no interaction.
- [ ] Tapping Use as Poster/Backdrop or Download restarts the 3 s delay.
- [ ] While an Image download is running, the controls stay visible. The 3 s delay starts again once the download ends.
- [ ] Swiping to another page keeps the current visibility of the controls and does not restart the delay.
- [ ] Back still closes the viewer (or zooms out first) when the controls are hidden.
- [ ] Existing rules are kept: Use as Poster/Backdrop and Download only show for a loaded image, and Use as Poster/Backdrop follows its state (hidden / current / available).
- [ ] The visibility and auto-hide logic lives in a small state holder, covered by a unit test that uses virtual time (show, hide, auto-hide after 3 s, delay restarted by a button interaction, no auto-hide while a download is running).
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
