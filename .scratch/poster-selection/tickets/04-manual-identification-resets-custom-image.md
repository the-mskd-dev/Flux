# 04: Manual identification and the Custom image

**What to build:** A **Manual identification** never carries the **Custom image** of the source Artwork: a new target Artwork shows its TMDB images. When the target Artwork already existed in the Catalog with its own Custom image, it keeps it.

The **Identification reset** is specified but not implemented yet; when it is, it must clear the Custom image too. Not part of this ticket.

See `.scratch/poster-selection/specs.md`.

**Blocked by:** 01: Choose a custom Backdrop from the Gallery

**Status:** done

- [x] The save of an identified folder keeps the custom Backdrop and custom Poster of a target Artwork already in the Catalog (the regular save keeps them, see ticket 03), and the source's do not follow.
- [x] `SyncHarness` behaviour tests cover: identification to a new Artwork → no Custom image; identification into an Artwork already in the Catalog with a Custom image → kept.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
