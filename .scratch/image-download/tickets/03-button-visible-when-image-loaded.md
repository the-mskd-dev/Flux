# 03: Download button only once the image is loaded

Spec: `.scratch/image-download/specs/spec.md`

**What to build:** The download button of the viewer is only shown when the image of the current page has finished loading. It is hidden while the image is loading and when its loading failed (error image shown), so the user is never offered an Image Download of something they cannot see. Swiping back to an already loaded image shows the button again, and zooming does not hide it. This is a purely visual state: the per image loading state, today local to each page, is reported up to the viewer, and the view model is untouched.

**Blocked by:** 01: Download the displayed image into Pictures/Flux

**Status:** ready-for-agent

- [ ] While the current image is loading, the download button is not displayed
- [ ] If the current image failed to load, the download button is not displayed
- [ ] Once the current image is loaded, the download button appears
- [ ] Swiping to another page shows the button according to that page's own loading state
- [ ] Swiping back to a page already loaded shows the button again immediately
- [ ] Zooming in or out does not change the button's visibility
- [ ] The loading state is not added to the Gallery UI state or the view model
- [ ] The grid, zoom, swipe and back handling of the viewer behave exactly as before
- [ ] Manually checked on a device or emulator, including a throttled connection so the loading phase is visible
