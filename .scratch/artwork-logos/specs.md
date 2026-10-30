# Spec : Logo des Artwork

## Problem Statement

Sur la page d'un Artwork (film ou série), Flux affiche le titre en texte simple. TMDB fournit pourtant pour la plupart des Artwork un **Logo** : le titre stylisé, tel qu'il apparaît sur les affiches et sur les plateformes. Le titre texte est fonctionnel mais terne, et il donne à la page un rendu moins soigné que les applications concurrentes. De plus, les Logo ne sont aujourd'hui ni récupérés, ni stockés, ni utilisables hors connexion.

## Solution

Lorsqu'un Artwork a un **Logo**, la page de l'Artwork l'affiche à la place du titre texte, au même emplacement. Sinon, le titre texte actuel est conservé.

Pour l'utilisateur :

- Le Logo est récupéré pendant la **Synchro** pour les nouveaux Artwork, sans appel réseau supplémentaire.
- Les Artwork déjà présents dans le **Catalogue** reçoivent leur Logo à la prochaine Synchro, y compris une Synchro légère, sans Synchro complète forcée.
- Le Logo est choisi dans la langue des données du Catalogue, sinon en anglais, sinon sans langue. Quand l'utilisateur change cette langue, les Logo sont mis à jour immédiatement, sans Synchro ni relance.
- Le Logo est préchargé avec les autres images : il s'affiche hors connexion dès qu'il a été préchargé. Tant qu'il n'est pas disponible, le titre texte reste affiché.

## User Stories

1. As a user, I want to see the Logo of a movie instead of its title text, so that the page looks closer to the film's own identity.
2. As a user, I want to see the Logo of a show instead of its title text on the Show page, so that the page looks closer to the show's own identity.
3. As a user, I want the title text to stay when an Artwork has no Logo, so that I always see the name of what I am looking at.
4. As a user, I want the Logo in the same place as the title was, so that the page layout does not change under my eyes.
5. As a user, I want the Logo to keep its proportions, so that it is never stretched or cropped.
6. As a user on a tablet, I want a larger Logo than on a phone, so that it stays readable on a big screen.
7. As a user, I want the Logo not to take more than 70 % of the width, so that it does not touch the screen edges.
8. As a user, I want the title text to be shown while the Logo is loading, so that the page is never left without a name.
9. As a user, I want the title text to stay if the Logo fails to load, so that a network problem does not leave a blank space or an error image.
10. As a user, I want a smooth transition from the title text to the Logo once it is loaded, so that the swap does not look like a glitch.
11. As a screen reader user, I want the Logo to be described by the Artwork title, so that the page stays accessible.
12. As a user, I want the top bar of the Show page to keep showing the title text, so that it stays readable when small.
13. As a user on a large screen opening a Show, I want to keep the title of the selected Season next to the Show, so that I know which Season I am looking at.
14. As a user on a large screen opening a movie, I want to see the Logo, so that movies and shows are consistent with the compact layout.
15. As a user with an existing Catalogue, I want my old Artwork to get their Logo too, so that the feature does not only apply to new files.
16. As a user, I want old Artwork to get their Logo during a normal (light) Synchro, so that I do not have to launch a Synchro complète.
17. As a user, I want new Artwork to get their Logo during the same Synchro that identifies them, so that it costs no extra wait or data.
18. As a user, I want an Artwork that TMDB has no Logo for to be searched once and not retried at every Synchro, so that Synchros stay fast and use little data.
19. As a user, I want an Artwork whose Logo search failed (network, API, missing token) to be retried at the next Synchro, so that a temporary outage does not deprive it of its Logo forever.
20. As a user, I want a Logo search failure not to stop the Synchro or mark any file as a Fichier en échec, so that the rest of my Catalogue is still updated.
21. As a user, I want a Logo found during a Synchro to be kept even if the Synchro is interrupted afterwards, so that the next Synchro has less to do.
22. As a user, I want the Logo to be in my language when TMDB has one, so that it matches my titles and descriptions.
23. As a user, I want the English Logo when none exists in my language, so that I still get a graphic title.
24. As a user, I want the language-less Logo as a last resort, so that textless or universal Logo are still used.
25. As a user, I want the best-rated Logo of the chosen language, so that I get the most relevant one.
26. As a user, I want Logo files that Flux can display and resize, so that I never get a broken image because of an unsupported file type.
27. As a user, I want my Logo to be updated right away when I change the data language, so that I do not wait for a manual Synchro or an app restart.
28. As a user changing the data language, I want an Artwork whose Logo lookup fails to keep its previous Logo, so that a temporary failure does not make it disappear.
29. As a user changing the data language, I want the Logo images to be prefetched afterwards, so that they are available offline in the new language.
30. As a user, I want the Logo to be available offline, so that my Catalogue looks the same without a connection.
31. As a user, I want Logo to be prefetched together with the other images after a Synchro, so that browsing is smooth.
32. As a user with limited data, I want Logo to be prefetched in a single small size only, so that prefetching does not download heavy images.
33. As a user, I want the displayed Logo and the prefetched Logo to be the very same image, so that offline display does not depend on a second download.
34. As a user, I want Private Artwork to get their Logo like any other, so that the private folder looks the same as the Catalogue.
35. As a user, I want the Unknown Artwork to keep its title text, so that this special group is never given a Logo by mistake.
36. As a user without a TMDB token, I want the app to keep working with title texts, so that the feature never blocks anything.
37. As a user, I want the Logo search to share the existing TMDB call limits, so that I am not rate-limited when many Artwork need a Logo.
38. As a user with a large Catalogue, I want the Logo of thousands of old Artwork to be fetched without crashing or stalling the Synchro, so that large libraries are supported.
39. As a user, I want the Progression de synchro to account for the Logo work and never go backwards, so that the percentage stays truthful.
40. As a user upgrading from an older version, I want my Catalogue, progress, statuses and private flags to be untouched by the database change, so that I lose nothing.
41. As a developer, I want the Logo choice to be one shared piece used by both the Synchro and the language change, so that both always choose the same Logo.
42. As a developer, I want the Logo to travel with the existing details call, so that no extra TMDB request is added for new Artwork.
43. As a developer, I want behaviour tests that encode these rules, so that I can refactor without regressions.

## Implementation Decisions

**Domain**
- **Logo** is a new term (added to `GLOSSARY.md`, section Modèles): the TMDB image of an Artwork showing its stylised title on a transparent background; an Artwork has at most one Logo.
- An Artwork carries an optional Logo path with three states: *never searched* (null), *searched, none available* (empty), or the path of the chosen Logo. It is stored as a field on the existing Artwork, not in a new table.
- The Unknown Artwork is never searched and never shows a Logo.

**Database**
- The Artwork table gets one nullable text column, null by default. The schema version is incremented with an automatic migration. Every existing Artwork therefore starts as *never searched*, which is what triggers the backfill.
- The Synchro upsert already writes only TMDB-sourced columns; the Logo path is one of them. User state (progress, status, private flag) is untouched.

**TMDB network**
- The image DTO read for the Gallery now also reads `logos`; the Gallery ignores them.
- The details calls (movie and show) append `images` to `append_to_response` and pass the `include_image_language` parameter, using the same language list logic as the Gallery (user language, English, language-less). New Artwork get their Logo in the call they already make.
- A standalone images call (already used by the Gallery) is reused for the backfill and the language change.

**Logo selection (one shared piece)**
- Input: the Artwork's logos and the data language. Output: the chosen Logo path, or "none".
- Candidate files are PNG only (SVG is ignored: the TMDB CDN does not resize it and the image loader does not decode it).
- Priority by language: data language, then English, then language-less. Within a language, the best `vote_average` wins. No candidate means "none".
- A failure of the lookup is distinguished from "none": a failure never changes the stored state.

**Synchro**
- The details Enrichissement records the chosen Logo path on the Artwork it saves (empty when there is none).
- A new Étape de synchro "Logos" runs in every Synchro (light and complete), after the Étape catalogue and before the Finalisation. It processes the Artwork known to the Catalogue whose Logo path is null, except the Unknown Artwork. For each, it does one images call, chooses the Logo and persists it immediately.
- The Étape declares its units from the number of Artwork to process, like the other Étapes, so the Progression de synchro stays correct and never goes backwards.
- A lookup failure leaves the Logo path null (retried at the next Synchro), is not counted as a Fichier en échec and never stops the Synchro. A missing TMDB token makes the Étape do nothing.
- Concurrency relies on the existing single semaphore in the TMDB client; the Étape does not manage its own limit.
- A Synchro complète refreshes the Logo through the details Enrichissement like any other TMDB data.

**Language change**
- The use case that re-translates the Catalogue when the data language changes gets a "Logos" block. It re-runs the shared selection for **all** Artwork (except the Unknown Artwork) in the new language and overwrites the stored Logo path, including turning "none" into a Logo and the reverse. On a lookup failure, the previous value is kept.
- The Logos block runs after the translation blocks have finished writing, because those blocks save whole Artwork rows from an earlier snapshot and could otherwise overwrite the new Logo path. The Logo update itself writes only that column.
- When it ends, it asks the images prefetch manager to prefetch, so the new Logo are cached for offline use.

**Offline and prefetch**
- Logo paths that are neither empty nor null are included in the images prefetched after a Synchro and after a language change.
- Logo are prefetched in standard size only (TMDB `w500`), never in the HD variant, regardless of the HD prefetch option. The same URL builder is used for display and prefetch, so both share one image loader cache entry.
- The prefetch input must therefore tell Logo paths apart from the other image paths.

**UI**
- A single composable shows the title of an Artwork: the Logo when the Artwork has one, the existing main title text otherwise. It replaces the main title in: the movie header of the Artwork screen, the movie title of the Artwork screen on large screens, and both the regular and large Show screens.
- While the Logo loads or if it fails, the title text is shown (no placeholder, no error image). When the Logo is loaded it replaces the text with a crossfade.
- Layout: same position as the title today (centred, straddling the bottom of the Artwork image), content scale "fit", maximum height 72 dp on phone and 96 dp on large screens, maximum width 70 % of the available width.
- The content description of the Logo is the Artwork title.
- The Show top bar (animated title) keeps the title text.
- On large screens, a Show keeps displaying the title of the selected Season as text; only movies show the Logo there.
- No light-theme specific handling: the Logo is displayed as is in both themes.

## Testing Decisions

- **A good test** checks observable behaviour: the Catalogue content in the database (stored Logo path per Artwork), the successive values of the Synchro state, the number of TMDB calls per endpoint, and the URLs handed to the image loader. It never checks which Étape or helper was called or in what internal order.
- **Seams** (existing ones, no new seam introduced):
  1. The Synchro use case, through the existing Synchro test harness (in-memory sources, Ktor `MockEngine` counting calls per endpoint, in-memory Room or fake database repository, fake coordinator). Covers the whole Synchro behaviour.
  2. The language change use case, with the same fakes. Covers the immediate Logo update.
  3. The images prefetch manager, through its existing test with a fake image loader. Covers offline availability (which URLs are requested, no HD Logo).
- **Cases**:
  - A new Artwork gets its Logo from the details call with no extra images call.
  - An old Artwork whose Logo path is null gets its Logo in a light Synchro.
  - "No Logo" is stored as empty and is not searched again by the next Synchro.
  - A network or API failure and a missing token leave null and the Artwork is retried by the next Synchro; no Fichier en échec is reported and the other Artwork are updated.
  - Language priority (user language, then English, then language-less), best rating within a language, and PNG only (an SVG-only Artwork ends up with "none").
  - The Unknown Artwork is never searched.
  - Progression de synchro still increases and reaches 100 % only at the end with the Logos Étape.
  - A Synchro complète refreshes the Logo through the details call.
  - Changing the data language updates the Logo of all Artwork without any Synchro, keeps the previous Logo on failure, is not overwritten by the translation saves, and triggers a prefetch.
  - The prefetch requests the Logo in `w500` only, even with the HD option enabled, and requests nothing for null or empty Logo paths.
  - Private Artwork are treated like the others.
  - The database migration keeps existing Artwork and starts them at null.
- **UI**: no automated UI test. The title composable gets previews (with Logo, without Logo, loading/failed) like the other composables.
- **Prior art**: the behaviour tests of the Synchro (Synchro harness, Kotest `FunSpec`, Turbine for the state, `MockEngine`), the prefetch manager test with its fake, and the existing language change tests.

## Out of Scope

- Logo in the **Gallery** (it only lists Backdrop and Poster).
- Logo for Season, Episode and the Unknown Artwork.
- Logo in the top bar, the Catalogue lists, the search and the history.
- Light-theme legibility: many TMDB logos are white. They are shown as is; a scrim, tint or fallback to text in light theme is a possible later improvement.
- SVG Logo and HD Logo.
- Backoff or retry limits for failed Logo lookups (a failure is retried at every Synchro, like a Fichier en échec).
- Choosing or changing the Logo manually.

## Further Notes

- The "never searched / none / path" distinction is what avoids both a forced Synchro complète for old Artwork and an endless retry of Artwork without any Logo on TMDB. If the three states ever become awkward, an alternative is a separate "Logo searched" flag.
- The seams above were proposed as Q15 of the grilling session and not explicitly answered; the user then asked for this spec, so they are taken as accepted.
- Delivery suggestion, each step leaving the app working: (1) Logo path field, migration and details-call capture, (2) Logos Étape for old Artwork, (3) prefetch of Logo, (4) language change, (5) title composable and screens.
- Per the repository rules, this spec lives in `.scratch/artwork-logos/specs.md` and tickets go in `.scratch/artwork-logos/tickets/`; nothing is published to GitHub Issues, so no triage label is applied.
