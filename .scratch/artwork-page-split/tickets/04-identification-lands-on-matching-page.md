# 04: Identification lands on the page matching the new Artwork

**What to build:** After a successful identification, the user lands on the page matching the type of the new Artwork: the movie page for a film, the Show page for a series. The season is dropped, so identifying a season as a series leads to the Show page, not a season-carrying page. The page ViewModels emit the identified Artwork with its type, and the navigation layer decides the destination with a pure rule.

**Blocked by:** 01 (shared base ViewModel)

**Status:** ready-for-agent

- [ ] A pure rule maps the type of the identified Artwork to the destination; tests cover the movie, series and season-origin cases.
- [ ] Identifying from the Artwork page or the Show page leads to the matching destination; no page guesses the type.
- [ ] The existing tests that checked the old "replace with Artwork route" event are updated to the new behaviour.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
