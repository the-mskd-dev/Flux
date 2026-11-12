# 06: Movie page is its own page and route

**What to build:** The current Artwork route and page become the Movie page, for films only. The season logic is removed from its ViewModel and screen. The people section stays on the Movie page, and it uses the blurred header rule. The catalog, search, people page and Private Folder open the movie route for films and the Show route for series. The Unknown Artwork screen is untouched. A navigation state saved before the update is not restored for the movie page, but must not crash the app.

**Blocked by:** 04 (identification destination), 05 (Season page)

**Status:** done

- [x] The movie route replaces the Artwork route; the ViewModel and screen no longer contain any season handling.
- [x] Catalog, search, people and Private Folder navigate to the movie route for a film and the Show route for a series; tests cover this mapping. (One pure rule, `artworkPage`, used by the four screens and by the identification destination.)
- [x] Movie ViewModel tests cover loading, identification, reset progress, external player result, gallery navigation and the people section.
- [x] Opening the app with a navigation state saved by the previous version does not crash. (An unknown saved route is decoded as an Unrestorable route, which leaves the back stack as soon as it is shown; tested at the serializers module, not on a device.)
- [x] No database schema change and no data migration.
- [x] `./gradlew :app:testFossDebugUnitTest` passes.
