# 04: The Full sync clears the Manual identifications

**What to build:** A Full sync (first install or launched from the settings) clears every manual-identification marker and identifies all the files again by name search. Status and Progress are kept.

**Blocked by:** 03

**Status:** ready-for-agent

- [ ] The marker is cleared for every Artwork and the files are identified by name search (a TMDB ID in the name still wins).
- [ ] Status and Progress are kept by path; history keeps the existing behaviour.
- [ ] Sync behaviour test: a manually identified Show goes back to its name-search Artwork after a Full sync, with its progress kept.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
