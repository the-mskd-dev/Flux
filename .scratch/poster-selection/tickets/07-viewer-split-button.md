# 07: Gallery viewer actions in a bottom split button, no Close button

**What to build:** In the Gallery full-screen viewer, the Use as Poster/Backdrop button and the Download button are merged into a single Material 3 Expressive split button, centred at the bottom of the screen (above the navigation bar). The leading part is the Use as Poster/Backdrop action (or the disabled Current Poster/Backdrop label), the trailing part is the Download icon. The Close button is removed: the viewer is closed with the back gesture (and, from ticket 08, with a swipe down).

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] The Close button is gone; nothing is left at the top of the viewer.
- [ ] The controls are a single split button centred at the bottom, kept clear of the navigation bar.
- [ ] Leading part: "Use as poster" / "Use as backdrop" when available, disabled "Current poster" / "Current backdrop" when the image is already the shown one.
- [ ] Trailing part: the Download icon, replaced by a loading indicator and disabled while an Image download is running.
- [ ] When Use as Poster/Backdrop is hidden (a Sync is running), only a standalone Download button is shown, at the same place.
- [ ] Nothing is shown until the current image is loaded.
- [ ] The visibility rules of ticket 06 are kept: hidden on open, fade in/out on a single tap, auto-hide after 3 s, delay restarted by a tap on either part, no auto-hide while an Image download is running.
- [ ] Both parts keep an accessible label (the Download part keeps its content description).
- [ ] Back still zooms out first, then closes the viewer.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
