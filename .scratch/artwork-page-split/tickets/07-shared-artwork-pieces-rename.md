# 07: Shared Artwork pieces lose the "Page" suffix

**What to build:** The base ViewModel shared by the Movie, Show and Season pages is named `ArtworkViewModel`, and the shared composables follow the same naming (`ArtworkDialogs`, `ArtworkHeader`, and any other common piece still carrying `ArtworkPage`). The page type used by the header rule (`ArtworkPageType`) and the `artworkPage` navigation rule keep their names, because "Artwork page" is a glossary term. This is a rename: the user sees no change.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] The shared base ViewModel is named `ArtworkViewModel`, and the shared composables no longer carry `Page` in their names.
- [ ] `ArtworkPageType` and `artworkPage` are unchanged.
- [ ] No behaviour change: existing tests pass without behavioural edits.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
