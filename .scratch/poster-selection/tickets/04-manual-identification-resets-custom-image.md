# 04: Identification manuelle brings back the default image

**What to build:** The Artwork produced by an **Identification manuelle** has no **Image personnalisée**: the page shows the TMDB images of the chosen Artwork. This holds even when the target Artwork already existed in the Catalog with its own Image personnalisée: it is lost, to keep the behaviour simple.

The **Réinitialisation** is specified but not implemented yet; when it is, it must clear the Image personnalisée too. Not part of this ticket.

See `.scratch/poster-selection/specs.md`.

**Blocked by:** 01: Choose a custom Backdrop from the Gallery

**Status:** ready-for-agent

- [ ] The save of an identified folder explicitly clears the custom Backdrop and custom Poster of the target Artwork (the regular save keeps them, see ticket 03).
- [ ] `SyncHarness` behaviour tests cover: identification to a new Artwork → no Image personnalisée; identification into an Artwork already in the Catalog with an Image personnalisée → cleared.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
