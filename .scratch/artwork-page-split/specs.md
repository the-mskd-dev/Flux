# Split the Artwork page into Movie, Season and Show pages

## Problem Statement

The Artwork page is shared by movies and by the seasons of a series, and its code branches on the type of the Artwork throughout: the people section only shows for a movie, the blurred header only for a movie, identification leads to a replacement that ignores the type of the new Artwork, and the season-specific logic (episode list, season overview, spoiler rules for a season) lives next to movie logic in the same ViewModel and screen. The Show page (`ShowScreen`) duplicates many of the same behaviours (header, overview, spoiler masking, identification and reset-progress dialogs) with its own ViewModel.

As a result, every change to the Artwork page must reason about two or three kinds of content at once, and the code is harder to read and to change than it needs to be. Shared behaviour is also duplicated between the Artwork page and the Show page.

## Solution

Give each kind of page its own screen and its own route, and its own ViewModel:

- a **Movie page** for films,
- a **Season page** for one season of a series, reached from the Show page,
- the existing **Show page** for series.

The three screens share their common behaviour through an open base ViewModel and shared UI pieces, so each screen only contains what is specific to its kind of content. The user-visible behaviour does not change, except for the identification flow, which now leads to the page matching the type of the new Artwork, and the blurred header, which is the same for a movie and a series page.

The vocabulary is updated: "Season page" is added to the glossary, and "Artwork page" stays the generic term.

## User Stories

1. As a user, I want to open a movie and see its movie page, so that I see the information and actions that apply to a film.
2. As a user, I want to open a series and see its show page, so that I see the seasons and the series-level information.
3. As a user, I want to open a season from a series and see its season page, so that I see the episodes of that season and their spoilers.
4. As a user, I want the season page to show the episodes of that season only, so that I do not mix seasons together.
5. As a user, I want the people section (cast and crew) on a movie page, so that I can browse the people behind the film.
6. As a user, I want the people section not to appear on a season page, so that the page only shows what concerns the season.
7. As a user, I want the blurred header to be available on movie pages, so that I can keep the poster style I already chose.
8. As a user, I want the blurred header to be available on show pages, so that a series can use the same header style as a movie.
9. As a user, I want the season page to keep the backdrop header, so that a season is shown with its landscape image.
10. As a user, I want to identify a movie or a series from its page, so that I can correct a wrong match.
11. As a user, after identifying the Artwork of a movie page, I want to land on the movie page of the new Artwork, so that the page matches what I identified.
12. As a user, after identifying the Artwork of a show page as a series, I want to land on the show page of the new series, so that the season is no longer carried over.
13. As a user, after identifying the Artwork of a show page as a movie, I want to land on the movie page of the new film, so that the page matches the new content.
14. As a user, I want the reset-progress action to work the same on the movie page, the season page and the show page, so that the behaviour is consistent.
15. As a user, I want the external player and the internal player to behave the same on each page, so that playback works wherever I start it.
16. As a user, I want the spoiler masking to apply on the season page and the show page, so that unseen episode descriptions stay hidden.
17. As a user, I want the gallery to open from any page, showing the images of the Artwork (Show level for a series, not the season's), so that I can pick a Backdrop or a Poster.
18. As a user, I want the custom image chosen in the gallery to show on the page I am looking at, so that my choice is visible immediately.
19. As a user, I want the Private Folder, the search, the people page and the catalog to open the right page for a movie or a series, so that nothing changes when I browse.
20. As a user, I want a saved navigation state from before the update to be handled without crashing, even if it can no longer be restored for the movie page, so that the app keeps working.
21. As a user, I want the Unknown Artwork screen to keep working as it does today, so that nothing changes for files I could not identify.
22. As a developer, I want each page to have one ViewModel, so that each screen's state and events are easy to follow.
23. As a developer, I want the common behaviour of the three pages to live in one open base ViewModel, so that a fix to a shared behaviour is made once.
24. As a developer, I want the shared UI pieces (header, overview, spoiler-masked episodes, identification and reset-progress dialogs) to live in common composables, so that the three pages look and behave the same where they should.
25. As a developer, I want the header mode to be decided by a single rule that knows the page type, so that the blurred header rule is written in one place.
26. As a developer, I want the navigation destination after identification to be decided from the type of the new Artwork, so that no screen has to guess the type.
27. As a developer, I want the glossary to define "Season page", so that the team uses the same term in code, specs and discussions.

## Implementation Decisions

- **Routes**: the current movie route is renamed to a movie route. A new season route carries the Artwork identifier and the season number, plus the colour that the current route already carries. The Show route is unchanged. The Show page navigates to the season route for each season. The catalog, search, people and private folder navigate to the movie route for films and to the show route for series.
- **Routes serialisation**: the renamed route changes its serialised name. A navigation state saved before the update does not restore the movie page; this is accepted.
- **Identification**: a successful identification no longer replaces the page with an Artwork route without season. The ViewModel of each page emits the identified Artwork with its type, and the navigation layer goes to the movie route for a film and to the show route for a series. The Manual identification is offered on the Movie and Show pages only, not on the Season page.
- **ViewModels**: one ViewModel per screen (movie, season, show). An open base ViewModel holds the behaviour shared by the three pages: observing the Artwork, identification, reset of progression, external player result handling, and the intent dispatching shape. Each screen's ViewModel overrides only how its content is loaded and mapped. The public contract of each ViewModel (the state, the intents and the events) is kept as close as possible to the current one, so that existing tests can be adapted rather than rewritten.
- **Blurred header**: the blurred header is available for movie pages and show pages. It is not available for season pages, which always show the backdrop. The header-mode rule is a single pure rule that takes the page type and the user setting. The existing user setting is kept; only the rule changes.
- **UI**: header, overview, episode lists with spoiler masking, identification dialog, and reset-progress dialog become shared composables. Each screen keeps its own page composable and its own large and regular layouts, which use the shared pieces. The people section is part of the movie page only.
- **Season page content**: the season page shows the episodes of the season, the season overview (with the season spoiler rule), and the episode actions. The season is chosen on the Show page: the Season page has no season picker of its own, as the Artwork page had none. It does not show the people section and does not use the blurred header.
- **Show page content**: unchanged, except that it uses the shared header and the shared dialogs, and that it gets the blurred header option.
- **Glossary**: add "Season page" as the page of one Season of a Show, reached from the Show page, listing its Episode. "Artwork page" remains the generic term for a movie or series page. The "Artwork image" entry is updated: the blurred header is available for a Movie page and a Show page, and not for a Season page.
- **Scope**: the Unknown Artwork screen is out of scope and keeps its current route and ViewModel.
- **Migration**: no database schema change. No data migration.

## Testing Decisions

Good tests check external behaviour: what the user sees and what the ViewModel emits for a given input, not how the private helpers are organised. They should not depend on the structure of the base class.

Seams:

- **ViewModel public interface** (state, intents, events) for the movie page, the season page and the show page. This is the main seam: it is the existing seam for the Artwork and Show pages, so no new seam is introduced for it.
- **Header-mode rule**: a pure rule that takes the page type and the setting and returns the header mode. This is tested as a pure function, with one case per page type and setting value.
- **Identification destination**: the rule that maps the type of the identified Artwork to the destination page. Tested as a pure function, with the movie, the series and the season-origin cases.

Modules to test:

- The three ViewModels, through their public interface, for loading, identification, reset progress, external player result, gallery navigation, and spoiler masking.
- The header-mode rule.
- The identification destination rule.
- The navigation mapping from the catalog, search, people and private folder to the movie route or the show route.

Prior art:

- The existing Artwork ViewModel test and the Show ViewModel test show the setup for the ViewModel tests (Kotest FunSpec, MockK, Turbine). They are the template for the three new ViewModel tests.
- The existing tests for the identification and the reset-progress flows are the template for the shared base behaviour.

Existing tests that check the old event (the Artwork-route replacement) must be updated to check the new destination.

## Out of Scope

- The Unknown Artwork screen and its ViewModel.
- Any change to the TMDB data, the sync, or the database schema.
- Any new user-facing feature beyond the blurred header on the show page.
- Changes to the player, the gallery screen itself, and the person screen.
- Renaming the Show route or the Show page.

## Further Notes

- The glossary is updated in the same change, so the code and the vocabulary stay aligned.
- Seams were chosen as the existing ViewModel interfaces plus two pure rules. No new seam is introduced beyond these.
- This spec is stored under the project's scratch folder, as set in the project's agent instructions, and is not published as a GitHub issue. The `ready-for-agent` label applies to a ticket, not to this file.
