# 03: New files follow the Identification manuelle

**What to build:** During a Synchro légère, a new file whose (title, year) parsed from the file name matches a known Media of a manually identified Artwork is attached to that Artwork without a name search. See `docs/adr/0001-identification-manuelle-par-titre-de-fichier.md`.

**Blocked by:** 01

**Status:** ready-for-agent

- [ ] Identification priority: TMDB ID in the file or folder name, then Identification manuelle, then name search.
- [ ] The new Artwork stays marked as manually identified after the Synchro légère.
- [ ] A new file with a different (title, year) is identified by name search as usual.
- [ ] When all the files of a manually identified Artwork disappear, the choice is forgotten.
- [ ] Synchro behaviour tests (existing harness): a new episode joins the manual Show with no search call, a TMDB ID in the name wins, and a different title is searched.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
