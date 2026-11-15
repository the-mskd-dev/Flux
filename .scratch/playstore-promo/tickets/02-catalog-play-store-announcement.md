# 02: One-time Play Store announcement on the Catalog

**What to build:** On the FOSS flavor, the Catalog shows a one-time announcement dialog thanking testers and contributors and announcing the Play Store availability. It appears only when the Catalog state is Content and the persisted "promo seen" flag is false; the flag is set to true as soon as the dialog is shown, so it never comes back, even if the app is killed while it is open. Its body holds the announcement paragraphs, with a Play Store button between the support paragraph and the closing sentence "Enjoy Flux!". That button opens the Play Store URL (constant from ticket 01) and closes the dialog. The standard dialog button only closes it; there is no validate button. The text is translated in the 7 locales. The Play Store flavor never shows it, nor do the Loading and Error states.

**Debug override:** in a debug build, the dialog is always shown (the "promo seen" flag is ignored) so it can be tested repeatedly. This override is temporary and must be removed before going to production.

**Removal deadline:** the whole announcement (dialog, flag, view model logic, strings) is temporary and must be removed by March 2027.

**Blocked by:** 01: Play Store link in Settings (FOSS only)

**Status:** ready-for-agent

- [ ] In a debug build the dialog is always shown, ignoring the flag, with a `TODO` comment in the code to remove this override when going to production
- [ ] A `TODO` comment on the announcement code (dialog, flag, view model logic) to remove it by March 2027
- [ ] The flag is persisted in the existing preferences DataStore closest to the Catalog (a new store only if none fits, per `ARCHITECTURE.md`)
- [ ] The Catalog view model exposes the dialog visibility in its Content state and handles a dismiss intent and a Play Store click
- [ ] Unit tests on the Catalog view model, with an in-memory flag store, cover:
  - [ ] shown for FOSS in Content with flag false
  - [ ] not shown for Play Store
  - [ ] not shown in Loading or Error
  - [ ] not shown again once the flag is set, including with a new view model instance
  - [ ] dismiss hides it
  - [ ] Play Store click emits the open-URL effect and hides it
- [ ] The dialog text is translated in all 7 locales (source text in specs.md)
- [ ] `./gradlew :app:testFossDebugUnitTest` passes
- [ ] Checked manually on a FOSS build
