# 05: Réinitialisation

**What to build:** A "Réinitialiser" option in the Show and Movie page `FluxPopupMenu`, shown only on a manually identified Artwork. After a confirmation, it performs a **Réinitialisation** (see `GLOSSARY.md`): the marker is cleared and each file is identified again by name search.

**Blocked by:** 01, 02

**Status:** ready-for-agent

- [ ] The option is shown only when the Artwork is manually identified, with the same availability rules (token, TMDB ID, Synchro running), and asks for a confirmation.
- [ ] Each file is identified again by name search; the Fichier en échec and Fichier non reconnu rules of 02 apply.
- [ ] Status, Progress, history (migrated) and the private flag are kept.
- [ ] After a merge, each file goes back to the Artwork that its own name search gives (for example, `The Office` files on the US show and `Office` files on the UK show).
- [ ] The page reloads on the resulting Artwork; if the files are split across several Artwork, the user goes back to the Catalogue.
- [ ] ViewModel and use case tests cover: a simple reset, a reset after a merge that splits the files, and history migration.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
