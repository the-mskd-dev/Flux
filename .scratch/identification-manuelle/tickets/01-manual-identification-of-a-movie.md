# 01: Manual identification of a Movie

**What to build:** On the Movie page, a "Sélectionner les données" option (icon `ic_search`) in the `FluxPopupMenu` opens a `FluxDialog` listing `searchMovie` results. Picking one performs an **Identification manuelle** (see `GLOSSARY.md`, `docs/adr/0001-identification-manuelle-par-titre-de-fichier.md`): the Movie is re-identified to the chosen Artwork, which is marked as manually identified, and the page reloads on it. Spec: `../specs.md`.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] `ArtworkEntity` gets a manual-identification boolean (default false, no new table); the database version is incremented with an automatic migration, and existing data is untouched.
- [ ] The option is hidden without a TMDB token, hidden when a file of the Artwork has a TMDB ID in its name or folder, and disabled while a Synchro is running.
- [ ] Dialog: an editable search field prefilled with the title parsed from the file name, a search with no year filter, and items showing the Poster (with a placeholder), the title and the year. The current Artwork is marked "actuel" and disabled. Loading, empty and error (with "Réessayer") states are handled.
- [ ] Picking a result replays the Enrichissements (details, credits, durations, Logo) for the file of the Artwork under the chosen TMDB ID, saves it marked as manually identified, and deletes the old empty Artwork.
- [ ] Status and Progress are kept (by path), the `history` row is migrated to the new Artwork (most recent one kept on merge), and the result is Private if the source or the target was Private.
- [ ] Choosing an Artwork already in the Catalogue merges into it.
- [ ] A transient failure leaves the file as a Fichier en échec.
- [ ] The page reloads on the new Artwork, replacing the navigation destination so that back does not lead to the old ID.
- [ ] The MVI contract, ViewModel and `ViewModelTest` (Kotest, MockK, Turbine) for the dialog; use case tests for history migration, private flag, merge and failure.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
