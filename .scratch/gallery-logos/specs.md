# Spec: Logos in the Gallery and custom Logo

Parent: GitHub issue #420 (Gallery: posters and logos from all languages, choose the logo). Only the logo part is covered here.

## Problem Statement

The Logo shown on an Artwork page is picked automatically from the data language, then English, then without language. The user cannot see the other Logos TMDB has for an Artwork and cannot choose another one, whereas they can already choose the Backdrop and the Poster in the Gallery.

## Solution

The Gallery lists the Logos of the Artwork in the same grid as the Backdrops and Posters. In the full-screen viewer, a Logo offers "Use as logo", which makes it the custom Logo of the Artwork, shown on the Artwork page in place of the automatic one. The custom Logo behaves like the custom Backdrop and the custom Poster.

## User Stories

1. As a user, I want to see the Logos of an Artwork in the Gallery, so that I know which ones exist.
2. As a user, I want Logos mixed in the same grid as Backdrops and Posters, sorted by rating, so that the best images come first.
3. As a user, I want only usable Logos (PNG) listed, so that I never open a Logo that cannot be displayed.
4. As a user, I want Logo thumbnails readable on a neutral background, so that transparent Logos stay visible.
5. As a user, I want to open a Logo full screen and zoom it, so that I can judge it before choosing.
6. As a user, I want a "Use as logo" button on a Logo in the viewer, so that I can choose it.
7. As a user, I want the Artwork page to show my chosen Logo right after I choose it, so that I see the result immediately.
8. As a user, I want the button to read "Current logo" on the Logo already shown (custom or automatic), so that I know what is in use.
9. As a user, I want the button hidden during a Sync, so that my choice is not lost while the Catalog changes.
10. As a user, I want my chosen Logo kept by a Light sync, so that routine syncs do not undo my choice.
11. As a user, I want my chosen Logo kept when I change the data language, so that the language setting does not undo my choice.
12. As a user, I want a Full sync or an Identification reset to clear my chosen Logo, as for my chosen Poster and Backdrop, so that these operations behave consistently.
13. As a user, I want a Manual identification not to carry my chosen Logo over to the target Artwork, so that the target keeps its own.
14. As a user, I want the labels "Use as logo" and "Current logo" in my language, so that the interface stays consistent.
15. As a user with existing data, I want the app update to keep my Catalog and my existing Custom images, so that nothing is lost.
16. As a user, I want the chosen Logo displayed without delay on the Artwork page, so that it does not flash the title text first.

## Implementation Decisions

- The Gallery image type gains a Logo type next to Backdrop and Poster. Logos come from the images already fetched for the Gallery; they are filtered to PNG (same rule as the automatic Logo) and sorted with the others by descending rating.
- The Gallery request keeps its current language filter (data language, English, without language). Fetching all languages is a later ticket.
- Logo thumbnails reuse the Logo size already used on the Artwork page, so the shown Logo comes from the cache; the viewer keeps using the original image like the other types.
- The Artwork has a new optional custom Logo path, stored apart from the automatic Logo path. The automatic Logo path keeps its meaning (null: never searched, empty: none) and can still be rewritten by the Logos step and the language change.
- The shown Logo is the custom Logo if present, otherwise the automatic one. The Artwork page and the "Current logo" comparison both use this rule.
- The custom Logo follows the Custom image lifecycle: kept by the Light sync, cleared together with the other Custom images by the Full sync and the Identification reset, not carried over by a Manual identification.
- Database version goes from 15 to 16 with a migration adding the column; existing rows get no custom Logo.
- Image prefetching includes the custom Logo in addition to the automatic one; the Artwork images projection carries it.
- The viewer button states (hidden, available, current) extend to Logos with the same rules as Poster and Backdrop; there is no way back to the automatic Logo.
- New strings "Use as logo" and "Current logo" are added in en, de, es, fr, it, ja, ko, reviewed in the PR.
- The glossary is already updated (Logo, Custom image, Gallery).

## Testing Decisions

- A good test checks external behaviour only (what the Gallery shows, what the Artwork shows, what survives a Sync), never internal calls.
- Seam 1, GalleryViewModelTest with the existing fakes: Logos in the grid, PNG filtering, rating sort, viewer on a Logo, "Use as logo", "Current logo", hidden during a Sync, saving the custom Logo. No separate mapper or use case test.
- Seam 2, the Sync harness with the existing Sync tests: custom Logo kept by a Light sync and by a language change, cleared by a Full sync and an Identification reset, not carried over by a Manual identification.
- Seam 3, a migration test 15 to 16 modelled on the previous migration tests.
- Prefetch: one added assertion in the existing prefetch test that the custom Logo path is included.

## Out of Scope

- Fetching images in all languages, language chips and ordering by language.
- Choosing the Poster per Season.
- Going back to the automatic Logo.
- A filter or a dedicated Logos section in the Gallery.

## Further Notes

Implementation is split in three tickets in `.scratch/gallery-logos/tickets/`. Tickets 01 and 03 mention separate mapper and use case tests; the spec's testing decisions prevail and those tests go through the Gallery view model seam instead.
