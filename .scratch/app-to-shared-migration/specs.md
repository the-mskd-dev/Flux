# Finish the app/ to shared/ migration for the Artwork pages and navigation

## Problem Statement

Flux is a Kotlin Multiplatform app (Android only for now) whose screens are being moved into the shared module. The move stopped halfway, and the Artwork pages sit on both sides of it:

- The Show page, the Person, Gallery, Settings and other pages are in `shared/`. The Movie page and the Season page, which are siblings of the Show page and share its base view model, are still in `app/`, along with the Catalog, Unknown Artwork, Search and Sources pages.
- Navigation is split in two route tables: one in the Android activity for 7 routes, one in the shared navigation for 12. To know where a route leads, I have to look in both.
- Each of the app/ pages is a thin wrapper that repeats the same wiring: get the view model with parameters, collect its state with lifecycle, handle its Events, and switch between loading, error and content with an animation.
- The reason these pages stayed in `app/` is small and concrete: the Movie, Season, Unknown and Catalog pages use the Android external-player launcher (the Playback launch spec removes that); the Search page uses an Android platform string for its "Go" button; the Sources page uses Android's folder names and the Android storage permission. The Player page stays Android (Media3).

As the maintainer I cannot reason about the Artwork page family as one thing, and each new page repeats the same wrapper code.

## Solution

Move every page that has no real Android dependency into `shared/`, keep only the platform-specific pages and adapters in `app/`, and give navigation one route table in `shared/` with a small platform hook for the pages that must stay Android. Pages share one frame for loading, error and content instead of each copying it.

For the user nothing changes: the same pages, in the same places, with the same animations.

## User Stories

1. As a viewer, I want the Movie page to look and behave as today, so that the migration changes nothing for me.
2. As a viewer, I want the Season page to look and behave as today.
3. As a viewer, I want the Show page to look and behave as today.
4. As a viewer, I want the Catalog page to look and behave as today.
5. As a viewer, I want the Unknown Artwork page to look and behave as today.
6. As a viewer, I want the Search page to look and behave as today, including its "Go" keyboard action label.
7. As a viewer, I want the Sources page to look and behave as today, including the system folder names (Movies, Downloads) I can add.
8. As a viewer, I want the Player to keep working exactly as today.
9. As a viewer, I want every back, forward and replace navigation to behave as today, including identification leading to the page that matches the new Artwork's type.
10. As a viewer, I want the loading, error and content states of a page to animate as today, so that transitions feel the same.
11. As a viewer, I want the colour scheme of an Artwork page (customisation or Artwork colour) to apply as today.
12. As a viewer, I want the storage permission flow of the Sources page to work as today.
13. As a viewer, I want the external player (via Playback launch) to start from the Movie, Season, Unknown and Catalog pages as today.
14. As a viewer, I want the Season page to keep its selected Episode and Spoiler masking state across rotation, so that nothing is lost.
15. As a viewer, I want the Catalog to keep its scroll and view mode across navigation, so that I return to where I was.
16. As a maintainer, I want one route table in shared, so that I find where any route leads in one place.
17. As a maintainer, I want the activity to register only the routes that are really Android (the Player, and any page needing Android-only APIs), so that the platform hook is small.
18. As a maintainer, I want the Movie, Season and Show pages in the same module as their base view model, so that the Artwork page family is read as one thing.
19. As a maintainer, I want one page frame that wires the view model, state collection, Event handling and the loading/error/content switch, so that a new page does not copy it.
20. As a maintainer, I want pages not to import Android lifecycle or platform string resources, so that they compile for any platform.
21. As a maintainer, I want the Search page to use its own string resource for "Go" instead of an Android platform string, so that it is shared-clean.
22. As a maintainer, I want the Sources page to ask a platform adapter for the system folder names, so that the page does not name Android directories.
23. As a maintainer, I want the Sources page to use a shared permission abstraction, so that the storage permission is requested through an adapter.
24. As a maintainer, I want the Compose Multiplatform isolation rule to hold for every moved page, so that no `androidx` Compose-for-Android only API is used in shared UI.
25. As a maintainer, I want the nullable "record playback result" hook on the Artwork base view model removed once Playback launch owns it, so that the base class matches its pages.
26. As a maintainer, I want the activity to shrink to window, theme, permissions and platform entries, so that it is easy to read.
27. As a maintainer, I want a test that every route has a page, so that adding a Route without its page fails in CI.
28. As a maintainer, I want the move done page by page, so that each step ships and can be reverted alone.

## Implementation Decisions

- **Order and dependencies.** This spec depends on the Playback launch spec for Movie, Season, Unknown and Catalog: those pages move only once the external-player launcher is behind the Playback launch seam. Search and Sources have no such dependency and can go first. The Player page stays in `app/` (it renders Media3) and is out of the move.
- **One navigation module** in `shared/` owns the route table. The Android activity supplies the platform hook: it registers only the routes that need Android-only APIs (today: the Player; after the move, nothing else), plus window and permission concerns. All other routes, including Movie, Season, Catalog, Unknown, Search and Sources, are registered in the shared table.
- **Page frame module.** A shared frame owns the repeated wiring: obtain the view model with its parameters, collect state with lifecycle, collect Events once, and switch between loading, error and content with the existing animation and content key. Pages provide only their content and an Event handler. The existing per-page `AnimatedContent` copies are removed.
- **Search page.** Replace the Android platform string used for the "Go" action label by a shared string resource (new key in all translated string files, English first as per project rule).
- **Sources page.** The system folder names and the storage permission request become a small adapter interface implemented in the Android module (one real adapter now; an iOS adapter would be the second, so the seam is only a prepared seam). The page itself becomes shared. If this proves out of proportion, keep the Sources page in `app/` and record it as an accepted exception.
- **Artwork base view model.** Once Playback launch lands, the nullable playback-result hook and the per-page external-result Intents are removed from it.
- **Koin and resources**: the view models are already in `shared/`; no DI change except removing platform-only registrations if any become unused.
- **Strings and resources**: pages that move use the shared resource module; any app-module resource they use (strings, drawables) moves with them.
- **Tests**: the existing view model tests do not change. There is no test today for the route-to-page mapping; add one at the navigation module's interface.
- No ADR is affected. The earlier spec `.scratch/artwork-page-split` created the Movie, Season and Show pages; this spec finishes the module placement it left open.
- Vocabulary: Artwork page, Season page, Catalog, Unknown Artwork as in GLOSSARY.md.

## Testing Decisions

- A good test asserts what the user would observe at the navigation seam: given a route, which page is shown; given a back/replace action, which route is on top. It does not assert how a page wires its view model.
- One seam: the navigation module's interface (route in, page out). Tested through a navigation test that, for every route, resolves a page (this is the "every route has a page" test), plus the existing behaviour of replace/clear-and-navigate.
- Page frame: tested through one representative page (loading, error, content, content key change).
- Existing view model tests stay as is and are the safety net for each page's behaviour; the move changes no view model.
- Screens themselves stay untested at the UI level, as today; screenshot tests are out of scope.
- Prior art: `KoinModulesTest` (module wiring check), the navigation tests in `app/src/test/.../navigation`, and the view model tests of Movie, Season, Show, Unknown, Search, Sources, Catalog.
- All existing tests live in the Android module's unit tests, including those of shared code; new tests follow that convention.

## Out of Scope

- The Player page and anything Media3 (it stays Android).
- An iOS app or iOS adapters; the migration prepares for it but does not build it.
- Redesigning any page, changing animations, or changing routes.
- Changing view models, contracts or their tests, except removing the playback-result hook after Playback launch.
- The Playback launch work itself (separate spec).
- Moving the activity's theme, edge-to-edge or permission code.

## Further Notes

- Evidence from the architecture review: the activity registers 7 routes and the shared navigation 12; Show is shared while Movie and Season are not; the app/ pages are thin wrappers (Movie 97 lines, Season 94) and the only Android-specific imports in them are the external-player launcher (Movie, Season, Unknown, Catalog), one Android string (Search), and Android folder names plus the storage permission (Sources).
- Suggested order: Search, Sources (if kept in scope), then after Playback launch: Movie, Season, Unknown, Catalog, and finally the single route table.
- Related specs: `.scratch/playback-launch/` (blocking for four pages), `.scratch/artwork-page-split/`, `.scratch/pip-and-track-preference/`.
