Status: ready-for-agent

## Problem Statement

Flux is now available on the Play Store, but users of the FOSS flavor don't know it. The author wants to thank the testers and contributors, and give users who want to support the development a direct way to reach the Play Store listing. Today, nothing in the app mentions it.

## Solution

On the FOSS flavor only:

- The Settings page gets a "Play Store" item, first in the app info section, that opens the Play Store listing.
- The Catalog shows a one-time announcement dialog, with a thank-you message and a button to the Play Store listing.

The Play Store flavor shows neither. The listing URL is a plausible placeholder, to be replaced once the real link exists.

## User Stories

1. As a FOSS user, I want to see an announcement that Flux is on the Play Store, so that I know I can support the development there.
2. As a FOSS user, I want the announcement to appear only once, so that it doesn't bother me on every launch.
3. As a FOSS user, I want a button in the announcement that opens the Play Store listing, so that I can go there in one tap.
4. As a FOSS user, I want the Play Store button to close the announcement after opening the link, so that it is gone when I come back.
5. As a FOSS user, I want to close the announcement without following the link, so that I can ignore it.
6. As a FOSS user, I want the announcement not to cover the Catalog while it is loading or in error, so that it doesn't collide with the first Sync or a failure screen.
7. As a new FOSS user, I want to see the announcement too, so that I'm aware of the Play Store version from the start.
8. As a FOSS user, I want a "Play Store" item at the top of the app info section in Settings, so that I can find the link again after dismissing the announcement.
9. As a FOSS user, I want the Play Store item to show the colored Play Store logo on a white background, so that it is recognizable.
10. As a FOSS user, I want the Play Store item to have a subtitle explaining it supports the development, so that I understand why it is there.
11. As a Play Store user, I want to see neither the announcement nor the Settings item, so that I'm not pointed to where I already am.
12. As a user of any supported language (en, fr, de, es, it, ja, ko), I want the announcement and the Settings item translated, so that I can read them.
13. As the author, I want the Play Store URL in one constant, so that I can swap in the real link in one place.
14. As the author, I want a TODO next to the placeholder URL, so that I don't forget to replace it.
15. As a user, I want the announcement not to come back after the app is killed while it was open, so that "once" really means once.

## Implementation Decisions

- **Flavor gating**: both features depend on the app's flavor information already exposed to the shared module; they are active only for the FOSS flavor.
- **Constant**: a Play Store URL constant is added to the contact group of the shared constants, built from the base application id plus the `.playstore` suffix, with a TODO comment to replace it.
- **Settings item**: first item of the app info section, hidden outside FOSS. It uses the existing open-URL intent. The icon is multicolored, so the item applies no tint and forces a white icon background. Title "Play Store", subtitle "Support the development" (translated).
- **Catalog dialog**:
  - Shown when the flavor is FOSS, the Catalog state is Content, and the "promo seen" flag is false.
  - The flag is persisted and set to true as soon as the dialog is shown (it doesn't wait for dismissal or click).
  - The dialog body holds the announcement paragraphs, with a Play Store button between the support paragraph and the closing sentence. The button opens the URL and closes the dialog.
  - The standard dialog button only closes the dialog; no validate button.
- **Presentation logic**: the Catalog view model owns the decision. A dismiss intent and a `showPlayStorePromo`-style field in the Content state drive the UI. Opening the URL reuses the existing open-URL navigation mechanism.
- **Persistence**: one boolean flag in the existing preferences DataStore closest to the Catalog, following `ARCHITECTURE.md`; a new store only if none fits.
- **Debug override**: in a debug build the dialog is always shown, ignoring the flag, so the author can test it repeatedly. A TODO marks it for removal before going to production.
- **Temporary feature**: the whole announcement is to be removed by March 2027, marked by a TODO on its code.
- **Translations**: new strings in all 7 locales (default English, fr, de, es, it, ja, ko), warm tone, single message.
- **Glossary / ADR**: none. No new domain term; the decision is easy to reverse.

## Testing Decisions

- A good test exercises external behavior only: the state and effects the Catalog view model produces for given flavor, state and flag inputs. It does not inspect DataStore internals or composables.
- **Single seam**: the Catalog view model (state in, intents in, state and navigation effects out), with the flag store faked in memory.
- Cases: shown for FOSS in Content with flag false; not shown for Play Store; not shown in Loading or Error; not shown again once the flag is set (including after a new view model instance); dismiss hides it; Play Store click emits the open-URL effect and hides it.
- Prior art: the existing Catalog and Settings view model tests, and the in-memory preferences DataStore and fake snackbar DataStore helpers in the unit test sources.
- Command: `./gradlew :app:testFossDebugUnitTest`.
- The Settings item is visual only and is checked manually on a FOSS build.

## Out of Scope

- The real Play Store URL (placeholder for now).
- Any promo on the Play Store flavor.
- Re-showing the announcement, a "don't show again" option, or remote configuration.
- Changes to the Play Store listing, store assets or release process.
- Bumping the version or the changelog.

## Further Notes

Source message (French, to be translated):

> Bonjour à toutes et à tous, j'ai l'immense plaisir de vous annoncer que Flux est maintenant disponible sur le Play Store !
> Je souhaite remercier toutes les personnes qui ont participé à la phase de test, votre aide m'a été extrêmement précieuse, et je remercie également toutes les personnes qui participent à l'amélioration de l'application !
> Pour celles et ceux qui veulent soutenir le développement de l'application, voici le lien vers le Play Store
> [Play Store button]
> Profitez bien de Flux !

The `ic_playstore` drawable is already staged in the shared resources.
