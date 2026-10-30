# 01: A new Artwork gets its Logo during the Synchro

**What to build:** When the Synchro identifies a new Artwork, it also records the Artwork's **Logo** (see `GLOSSARY.md`), at no extra TMDB cost. The Logo is chosen in the data language of the Catalogue, then English, then language-less; PNG files only; best rating within a language. An Artwork carries an optional Logo path with three states: never searched (null), searched with no Logo available (empty), or the chosen path. A Synchro complète refreshes the Logo like any other TMDB data. The Unknown Artwork is never searched. Nothing is displayed yet.

This ticket introduces the shared piece that chooses a Logo from a list of candidates and a language; later tickets reuse it.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] The Artwork has a nullable Logo path stored on the existing Artwork (no new table); the database version is incremented with an automatic migration and existing Artwork start at null, with their progress, statuses and private flags untouched.
- [ ] The TMDB images DTO also reads the logos; the Gallery behaviour is unchanged (it still lists only Backdrop and Poster).
- [ ] The details calls for movies and shows also ask for the images, with the same language list as the Gallery, so a new Artwork gets its Logo with no additional TMDB request.
- [ ] The Logo is chosen with the priority data language, English, language-less; within a language the best `vote_average` wins; SVG candidates are ignored; no candidate stores an empty Logo path.
- [ ] The details Enrichissement saves the chosen Logo path with the Artwork; the Unknown Artwork keeps a null Logo path.
- [ ] A Synchro complète overwrites the stored Logo path with the newly chosen one.
- [ ] Synchro behaviour tests (existing harness, `MockEngine` counting calls per endpoint) cover: Logo stored for a new Artwork with no `images` call; language priority; best rating; SVG-only Artwork ends with "none"; Synchro complète refresh; Unknown Artwork never searched.
- [ ] A migration test shows existing Artwork survive with a null Logo path.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
