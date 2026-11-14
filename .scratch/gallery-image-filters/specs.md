# Spec: Gallery in all Image languages, with type and language filters

Parent: GitHub issue #420 (Gallery: posters and logos from all languages, choose the logo). Covers the all-languages request and the filters. The Logo choice itself is already delivered by `gallery-logos`; the Season poster is out of scope (already handled elsewhere).

## Problem Statement

The Gallery only lists images in the data language, English and without language, so a user who wants, for example, the Japanese Posters of an anime cannot see them without changing the data language of the whole app. With more images, the grid mixing Backdrops, Posters and Logos also becomes hard to browse.

## Solution

The Gallery lists the images of every Image language. A filter button opens a bottom sheet with two sections: the image type (Backdrops, Posters, Logos) and the Image language (Global plus the languages present in the images). Selections are multiple. Nothing selected in a section means no restriction for it.

## User Stories

1. As a user, I want the Gallery to show images of every language, so that I can find, for instance, the Japanese Poster without changing the data language.
2. As a user, I want to filter the Gallery by type (Backdrop, Poster, Logo), so that I only browse what I am looking for.
3. As a user, I want to filter the Gallery by Image language, so that I can isolate one language.
4. As a user, I want to select several types or several languages at once, so that I can compare them.
5. As a user, I want a "Clear" action in the sheet, so that I can go back to the full grid quickly.
6. As a user, I want the filter button available even when no image matches, so that I can change my selection.
7. As a user, I want a message (the existing empty state) when no image matches my selection.
8. As a user, I want the full-screen viewer to browse only the filtered images, so that swiping stays consistent with the grid.
9. As a user, I want the filter locked while the viewer is open.
10. As a user, I want images without text (most Backdrops) labelled "Global", so that I understand the language filter.
11. As a user, I want the language list to follow the images actually available, so that I never pick a language with nothing in it.
12. As a user, I want the filter forgotten when I leave the Gallery, so that it always opens on the full grid.
13. As a user, I want the new texts in my language.

## Implementation Decisions

- The images request of the Gallery (movie and show) is sent without `language` and `include_image_language` so TMDB returns every language. The detail requests and the Syncs keep their current language filter: the automatic Logo and Poster are unchanged.
- The image DTO's language field is renamed `languageCode` (ISO 639-1, null for an image without text). The Gallery image carries it as `languageCode`. The region (`iso_3166_1`) is not read: only languages are handled; regions can come later.
- Sorting stays by descending rating; no language is pushed first.
- The Gallery UI state holds the selected types and the selected language codes (not persisted). The grid and the viewer index both work on the filtered list. Types and languages combine with AND between the two sections, OR within one section.
- The languages offered are those present in the loaded images (all types, not narrowed by the selected type). Order: data language, English, Global, then others by descending number of images. A selected language with no image for the current type simply yields the empty state; the selection is not auto-cleared.
- Label of a language: project `Language` name when known, else the device language name, else the uppercase code. `null` is labelled "Global".
- The filter UI follows `SearchGenresSheet` (`FluxBottomSheet` + `BottomSheetRadioItem`, title row + "Clear" button). The filter button sits in the top bar and is hidden while the viewer is open.
- New strings are added to the 7 locales of composeResources.
- PNG-only rule for Logos is unchanged.

## Testing Decisions

- `TMDBServiceTest`: the Gallery images requests carry no `language` / `include_image_language`; the detail requests still do.
- `ImagesApiMapperTest`: `languageCode` is kept in the Gallery image; `logoPath` still works with the renamed field.
- `GalleryViewModelTest`: type filter, language filter, AND/OR combination, clear, viewer on the filtered list, filter empty state, language ordering.

## Out of Scope

- Region handling (pt-BR vs pt-PT).
- Season poster choice.
- Putting a language first in the grid.
- Persisting the filter.

## Documentation

- `GLOSSARY.md`: Gallery updated, Image language added (done). No ADR: the decisions are easy to reverse.
