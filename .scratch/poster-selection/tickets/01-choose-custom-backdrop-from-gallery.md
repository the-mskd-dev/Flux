# 01: Choose a custom Backdrop from the Gallery

**What to build:** From the full screen viewer of the **Gallery**, the user can make the displayed Backdrop the **Artwork image** of the page (see `GLOSSARY.md`: **Custom image**). A text button "Use this image", centred at the top of the viewer, saves it as the custom Backdrop of the Artwork. On the image already used as Artwork image (custom, or TMDB default when there is none), the button is replaced by a disabled "Current image". Going back to the page shows the chosen Backdrop in the header, on Movie and Show pages, compact and large layouts (normal header only; the blurred header is ticket 02).

This ticket lays the whole path: both custom columns (Backdrop and Poster), the targeted write, and the single domain rule giving the Artwork image path. Later tickets reuse them.

See `.scratch/poster-selection/specs.md`.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] The Artwork model and table get two nullable fields, custom Backdrop path and custom Poster path; the database version goes 14 → 15 with an AutoMigration; existing Artwork start with both null and keep progress, statuses, private flags and manual identifications.
- [ ] A targeted write sets the custom Backdrop (or Poster) of one Artwork without rewriting the rest of the row.
- [ ] A single domain rule gives the Artwork image path from an Artwork and the header mode: normal header → custom Backdrop, otherwise TMDB Backdrop (the blurred branch is wired in ticket 02).
- [ ] The normal header of the Movie and Show pages (compact and large layouts) displays the path given by this rule.
- [ ] The Gallery view model observes the Artwork, the blurred header setting and the Sync state, and exposes for the viewed image a button state: *hidden*, *available* or *current*.
- [ ] Button state: Backdrop → *available*, or *current* if it is the Artwork image; Poster → *hidden* (with the normal header); any image → *hidden* while a Sync runs.
- [ ] Tapping "Use this image" saves the image as custom Backdrop; the state becomes *current* by itself (Artwork observed). A failed save is traced and leaves the state unchanged, with no event.
- [ ] The viewer shows the button at the top centre only once the image is loaded (same rule as Image Download); *current* shows a disabled "Current image"; Image Download still works.
- [ ] Strings "Use this image" and "Current image" are added to every supported language.
- [ ] `GalleryViewModelTest` covers: Show + Backdrop available / Poster hidden; Movie normal + Backdrop available / Poster hidden; TMDB default *current* without custom image; custom image *current* and TMDB default *available* with one; hidden during a Sync; tap saves and becomes *current*; failed save leaves state unchanged.
- [ ] A `SyncHarness` behaviour test shows a custom Backdrop set on an Artwork is read back from the Catalog.
- [ ] A migration test (like `Migration13To14Test`) shows rows survive 14 → 15 with both new columns null.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
