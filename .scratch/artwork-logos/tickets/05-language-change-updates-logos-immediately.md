# 05: A language change updates the Logo immediately

**What to build:** When the user changes the data language, the **Logo** of every Artwork is re-chosen in the new language and updated right away, with no Synchro, no manual action and no app restart. The Logo images are then prefetched so they are available offline in the new language.

The use case that re-translates the Catalogue gets a "Logos" block. It runs the lookup from ticket 03 for every Artwork except the Unknown Artwork and overwrites the stored Logo path, including turning "none" into a Logo and the reverse. If the lookup fails for an Artwork, its previous Logo path is kept.

The existing translation blocks save whole Artwork rows from an earlier snapshot, so the Logos block must run after they have finished writing; otherwise the new Logo path could be overwritten by a stale one. The Logo update itself writes only the Logo path.

**Blocked by:** 03: Existing Artwork get their Logo (Étape "Logos"); 04: The Logo is available offline (prefetch)

**Status:** done

- [ ] The language change use case re-chooses the Logo of all Artwork (except the Unknown Artwork) in the new language and overwrites the stored Logo path.
- [ ] "None" can become a Logo and a Logo can become "none" after the change.
- [ ] A lookup failure for an Artwork keeps its previous Logo path.
- [ ] The Logo update only writes the Logo path and runs after the translation blocks, so it is never overwritten by their saves.
- [ ] At the end of the language change, the images prefetch is requested so the new Logo are cached.
- [ ] No Synchro is needed: the Logo seen on the Artwork pages are the new ones right after the language change completes.
- [ ] Use case tests (same fakes as the Synchro tests) cover: all Logo updated without a Synchro; previous Logo kept on failure; not overwritten by the translation saves; prefetch triggered; Unknown Artwork untouched.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
