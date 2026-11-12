# 05: A season opens its own Season page

**What to build:** Opening a season from the Show page leads to a dedicated Season page with its own route (Artwork id, season number, colour), its own ViewModel and its own screen, built on the shared base ViewModel and shared composables. It shows that season's episodes only, the season overview with its season spoiler rule, season selection and the episode actions. It always shows the backdrop header and has no people section. The Season page supports the gallery (images of the Show), reset progress, and the external and internal player like the other pages.

**Blocked by:** 01 (shared base ViewModel), 02 (shared page composables)

**Status:** done

- [x] The Show page navigates to the season route for each season; the Artwork page is no longer opened for seasons.
- [x] The Season page lists only the episodes of its season, masks unseen episode descriptions per the spoiler setting, and lets the user change season. (The season is changed from the Show page: no in-page season picker, as decided with the user.)
- [x] No people section and no blurred header on the Season page; a custom image chosen in the gallery shows on the page. (in code; not checked on a device)
- [x] Reset progress behaves as on the other pages. The Manual identification is not offered on the Season page: it is done from the Show page.
- [x] ViewModel tests (Kotest FunSpec, MockK, Turbine) cover loading, reset progress, external player result, gallery navigation and spoiler masking.
- [x] `GLOSSARY.md` defines "Season page" (page of one Season of a Show, reached from the Show page, listing its Episode); "Artwork page" stays the generic term.
- [x] `./gradlew :app:testFossDebugUnitTest` passes.
