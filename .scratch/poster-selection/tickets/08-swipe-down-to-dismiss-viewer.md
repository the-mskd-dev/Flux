# 08: Swipe down to dismiss the Gallery viewer

**What to build:** When the current image is not zoomed, the user can drag it down to close the Gallery full-screen viewer, like a bottom sheet. The image follows the finger, shrinks slightly as it goes down and the black background fades out, revealing the Gallery grid behind. Past a threshold (or after a fast downward fling) the viewer closes on release; otherwise the image springs back into place. If the finger goes back up during the drag, the image follows it back up.

**Blocked by:** None (can start immediately). Touches the same composable as 07: doing 07 first avoids merge conflicts.

**Status:** ready-for-agent

- [ ] At 1x, a drag that is mainly vertical and downward moves the image with the finger; a drag that is mainly horizontal still swipes to the next/previous page.
- [ ] The image cannot be dragged above its resting position (offset clamped at 0, like a bottom sheet).
- [ ] While dragging, the image scales down progressively (to about 0.85 at the threshold) and the background alpha decreases with the distance, so the Gallery grid shows through.
- [ ] Moving the finger back up during the drag brings the image, its scale and the background back accordingly.
- [ ] On release, the viewer closes if the drag passed about 25 % of the screen height or ended with a fast downward fling; the image keeps moving down and fades out while closing.
- [ ] Otherwise, the image springs back to its resting position with a spring animation and the background becomes fully opaque again.
- [ ] The controls hide as soon as the drag starts.
- [ ] When the image is zoomed, a vertical drag pans the image as today and never dismisses the viewer.
- [ ] Pinch, double-tap zoom and the single tap that toggles the controls keep working as before.
- [ ] Back still zooms out first, then closes the viewer.
- [ ] The dismiss decision (progress from the offset, clamp at 0, threshold, fling velocity, dismiss vs settle back) lives in a small state holder, covered by a unit test.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
