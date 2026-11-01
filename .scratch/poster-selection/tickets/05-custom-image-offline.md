# 05: Image personnalisée available offline

**What to build:** The custom Backdrop and custom Poster are prefetched with the other Catalog images, so the **Artwork image** shows the **Image personnalisée** without a connection once prefetched.

See `.scratch/poster-selection/specs.md`.

**Blocked by:** 01: Choose a custom Backdrop from the Gallery

**Status:** ready-for-agent

- [x] The list of images to prefetch includes the non-null custom Backdrop and custom Poster paths, in the same sizes as the TMDB Backdrop and Poster.
- [x] A behaviour test (`SyncHarness` Catalog, or `ImagesPrefetchManagerTest`) shows a custom path is part of the images to prefetch.
- [x] `./gradlew :app:testFossDebugUnitTest` passes.
