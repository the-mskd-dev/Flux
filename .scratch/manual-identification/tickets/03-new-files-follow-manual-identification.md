# 03: New files follow the Manual identification

**What to build:** During a Light sync, a new file whose (title, year) parsed from the file name matches a known Media of a manually identified Artwork is attached to that Artwork without a name search. See `docs/adr/0001-manual-identification-by-file-name-title.md`.

**Blocked by:** 01

**Status:** ready-for-agent

- [ ] Identification priority: TMDB ID in the file or folder name, then Manual identification, then name search.
- [ ] The new Artwork stays marked as manually identified after the Light sync.
- [ ] A new file with a different (title, year) is identified by name search as usual.
- [ ] When all the files of a manually identified Artwork disappear, the choice is forgotten.
- [ ] Sync behaviour tests (existing harness): a new episode joins the manual Show with no search call, a TMDB ID in the name wins, and a different title is searched.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
