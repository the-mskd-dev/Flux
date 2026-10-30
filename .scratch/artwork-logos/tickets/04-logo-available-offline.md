# 04: The Logo is available offline (prefetch)

**What to build:** The images prefetched after a Synchro include the **Logo** of every Artwork, so a Logo already prefetched displays without a connection (Artwork pages keep showing it in airplane mode).

Logo are prefetched in a single small size only (TMDB `w500`), never in the HD variant, whatever the HD prefetch option. The URL prefetched is exactly the URL displayed (ticket 02), so both share one image loader cache entry and offline display needs no second download. Null and empty Logo paths request nothing.

The prefetch input must tell Logo paths apart from the other image paths, since the other images get a standard and an optional HD variant while Logo do not.

Note from ticket 02: the display request (`ArtworkTitle`) uses `String.tmdbLogo` with `Size.ORIGINAL`. Coil's memory cache key includes the size, so the prefetch must also request `Size.ORIGINAL` to share the memory cache entry (the disk cache is keyed by URL only).

**Blocked by:** 01: A new Artwork gets its Logo during the Synchro; 02: Display the Logo instead of the title text

**Status:** ready-for-agent

- [ ] The prefetch input distinguishes Logo paths from the other image paths.
- [ ] Every non-empty Logo path is prefetched in `w500`; no HD variant is requested for a Logo, even with the HD option enabled.
- [ ] Null and empty Logo paths produce no request.
- [ ] The prefetched Logo URL is the one built by the display code, not a second copy of the logic.
- [ ] The existing prefetch behaviour for Artwork images, Media images and Season images, including the HD option, is unchanged.
- [ ] The prefetch manager test (fake image loader) covers: Logo requested in `w500`; not in HD with the option enabled; nothing for null/empty; other images unchanged.
- [ ] Manual check: after a Synchro and its prefetch, with the device offline, a Logo still shows on its Artwork page; one that was not prefetched falls back to the title text.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
