# 02: Manual identification of a Show

**What to build:** The same option and dialog as ticket 01, on the Show page, using `searchShow`. All the episodes of the Show are re-identified to the chosen Show, including Seasons and episodes. Never offered on the Unknown Artwork. Spec: `../specs.md`.

**Blocked by:** 01

**Status:** ready-for-agent

- [ ] The option is in the Show page `FluxPopupMenu`, with the same availability rules as 01, and is absent for the Unknown Artwork.
- [ ] All the Media of the Show, whatever their file-name title, are re-identified, with the Seasons and episodes enrichment replayed under the chosen TMDB ID.
- [ ] An episode or Season that does not exist in the chosen Show becomes a Fichier non reconnu (Unknown Artwork); a transient failure gives a Fichier en échec. There is no atomicity.
- [ ] A merge into an existing Show in the Catalogue works: Seasons and episodes are combined, and the most recent history is kept.
- [ ] Status, Progress, history and the private flag are kept as in 01.
- [ ] Tests cover: missing Season goes to Unknown Artwork, network failure gives a Fichier en échec, merge, and history migration.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
