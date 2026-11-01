# 03: Image personnalisée and the Synchro

**What to build:** The **Image personnalisée** survives a **Synchro légère**, even when the Synchro rewrites the Artwork (for instance a new Episode added to the Show). A **Synchro complète** clears the custom Backdrop and custom Poster of every Artwork, bringing back the TMDB images.

See `.scratch/poster-selection/specs.md`.

**Blocked by:** 01: Choose a custom Backdrop from the Gallery

**Status:** ready-for-agent

- [ ] The Catalog save used by the Synchro keeps the existing custom paths of each Artwork, like it already keeps the private flag and the manual-identification marker.
- [ ] One bulk update clears both custom paths of every Artwork.
- [ ] The Synchro complète runs this clearing before the catalogue step, with the same enablement as the clearing of the Identifications manuelles (dedicated Étape de synchro or the existing one extended, whichever is simplest); the Synchro légère never runs it.
- [ ] The Progression de synchro stays truthful and never goes backwards.
- [ ] `SyncHarness` behaviour tests cover: custom Backdrop and Poster kept by a Synchro légère; kept when a new Episode of the Show is added; cleared on every Artwork by a Synchro complète.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
