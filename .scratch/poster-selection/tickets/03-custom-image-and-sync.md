# 03: Custom image and the Sync

**What to build:** The **Custom image** survives a **Light sync**, even when the Sync rewrites the Artwork (for instance a new Episode added to the Show). A **Full sync** clears the custom Backdrop and custom Poster of every Artwork, bringing back the TMDB images.

See `.scratch/poster-selection/specs.md`.

**Blocked by:** 01: Choose a custom Backdrop from the Gallery

**Status:** done

- [x] The Catalog save used by the Sync keeps the existing custom paths of each Artwork, like it already keeps the private flag and the manual-identification marker.
- [x] One bulk update clears both custom paths of every Artwork.
- [x] The Full sync runs this clearing before the Catalog step, with the same enablement as the clearing of the Manual identifications (dedicated Sync step or the existing one extended, whichever is simplest); the Light sync never runs it.
- [x] The Sync progress stays truthful and never goes backwards.
- [x] `SyncHarness` behaviour tests cover: custom Backdrop and Poster kept by a Light sync; kept when a new Episode of the Show is added; cleared on every Artwork by a Full sync.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes. (one pre-existing failure, unrelated: ShowManualIdentificationBehaviourTest, also red on 98e7805e)
