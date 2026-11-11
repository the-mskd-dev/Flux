# 06: Movie page is its own page and route

**What to build:** The current Artwork route and page become the Movie page, for films only. The season logic is removed from its ViewModel and screen. The people section stays on the Movie page, and it uses the blurred header rule. The catalog, search, people page and Private Folder open the movie route for films and the Show route for series. The Unknown Artwork screen is untouched. A navigation state saved before the update is not restored for the movie page, but must not crash the app.

**Blocked by:** 04 (identification destination), 05 (Season page)

**Status:** ready-for-agent

- [ ] The movie route replaces the Artwork route; the ViewModel and screen no longer contain any season handling.
- [ ] Catalog, search, people and Private Folder navigate to the movie route for a film and the Show route for a series; tests cover this mapping.
- [ ] Movie ViewModel tests cover loading, identification, reset progress, external player result, gallery navigation and the people section.
- [ ] Opening the app with a navigation state saved by the previous version does not crash.
- [ ] No database schema change and no data migration.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
