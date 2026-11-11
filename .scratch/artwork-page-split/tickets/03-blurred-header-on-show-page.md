# 03: The Show page can use the blurred header

**What to build:** A user who enabled the blurred header sees it on a Show page as well as on a Movie page. The header mode is decided by a single pure rule taking the page type and the existing user setting; the Season page never uses the blurred header and always shows the backdrop. The existing setting is kept.

**Blocked by:** 02 (shared page composables)

**Status:** done

- [ ] One pure header-mode rule takes the page type (movie, show, season) and the setting; the Artwork page and the Show page both use it.
- [ ] Unit tests cover one case per page type and setting value.
- [ ] A Show page shows the blurred header when the setting is on and the backdrop when it is off.
- [ ] `GLOSSARY.md` "Artwork image" says the blurred header is available for a Movie page and a Show page, not for a Season page.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
