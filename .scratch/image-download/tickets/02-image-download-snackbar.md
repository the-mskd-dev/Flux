# 02: Snackbar feedback for the Image Download

Spec: `.scratch/image-download/specs/spec.md`

**What to build:** After tapping the download button, the user sees a snackbar: a confirmation when the Image Download succeeded, a single clear failure message when it failed (no distinction between offline and other causes). The snackbar is drawn above the full screen viewer, so the overlay does not hide it. This introduces the first `SnackbarHost` of the project, inside the Gallery.

**Blocked by:** 01: Download the displayed image into Pictures/Flux

**Status:** ready-for-agent

- [ ] A successful Image Download shows a confirmation snackbar while the viewer is open
- [ ] A failed Image Download (including no cache entry and no network) shows one failure message, the same whatever the cause
- [ ] The snackbar is visible above the full screen viewer, not hidden behind it
- [ ] The Gallery screen collects the two Image Download events from the view model and shows the snackbar; no state is added to the view model for this
- [ ] The success and failure messages exist in en, fr, de, es, it, ja, ko
- [ ] The grid, zoom, swipe and back handling of the viewer behave exactly as before
- [ ] Manually checked on a device or emulator: success message after a download, failure message when the image has no cache entry and the device is offline
