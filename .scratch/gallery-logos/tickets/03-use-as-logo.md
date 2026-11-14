# 03: "Use as logo" in the viewer

**What to build:** In the Gallery viewer, a Logo shows a "Use as logo" button that sets it as the custom Logo; the Artwork page then shows it. The button reads "Current logo" when the Logo is already the one shown (custom or automatic) and is hidden during a Sync. There is no way back to the automatic Logo, as for Poster and Backdrop. Both texts are translated in all 7 languages (en, de, es, fr, it, ja, ko) for review in the PR. Parent: GitHub issue #420.

**Blocked by:** 01 (Logos appear in the Gallery), 02 (Custom logo exists and is shown)

**Status:** ready-for-agent

- [ ] Tapping "Use as logo" saves the custom Logo and the Artwork page shows it
- [ ] Button becomes "Current logo" for the shown Logo and is hidden during a Sync
- [ ] Strings added in the 7 languages
- [ ] Tests: GalleryViewModelTest (Use as logo, Current logo, hidden during Sync)
