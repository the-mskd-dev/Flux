# 02: Custom Poster for the blurred header (Movie)

**What to build:** When the blurred header setting is enabled, the Gallery of a Movie offers its Posters instead of its Backdrops, and the chosen Poster becomes the custom Poster shown by the blurred header of the Movie page. Custom Backdrop and custom Poster are independent: toggling the setting back and forth, each mode finds its own **Image personnalisée** again, or the TMDB image when there is none. A Show never offers Posters, whatever the setting (the Show page has no blurred header).

See `.scratch/poster-selection/specs.md`.

**Blocked by:** 01: Choose a custom Backdrop from the Gallery

**Status:** ready-for-agent

- [ ] The domain rule's blurred branch returns custom Poster, otherwise TMDB Poster, for a Movie.
- [ ] The blurred header of the Movie page uses this rule instead of reading the TMDB Poster directly.
- [ ] Button state with the blurred header: Movie + Poster → *available* / *current*; Movie + Backdrop → *hidden*; Show + Poster → still *hidden*, Show + Backdrop → still *available*.
- [ ] Tapping the button on a Poster saves it as custom Poster, leaving the custom Backdrop untouched.
- [ ] `GalleryViewModelTest` covers the blurred cases above, the save of a Poster, and the independence of both choices when the setting changes.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
