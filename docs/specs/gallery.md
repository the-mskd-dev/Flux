# Spec : Gallery

## Problem Statement

Dans Flux, l'**Artwork image** en haut de la page d'un Artwork (film ou série) n'est qu'une image décorative. L'utilisateur ne peut pas voir les autres backdrops et posters que TMDB propose pour cet Artwork, ni les regarder en grand ou les zoomer.

## Solution

Un tap sur l'Artwork image ouvre une nouvelle page **Gallery**. Elle récupère auprès de TMDB toutes les images de l'Artwork (backdrops et posters) et les affiche dans une seule grille décalée, triée par note décroissante. Un tap sur une image ouvre un visualiseur plein écran avec zoom et swipe latéral entre les images. Sans connexion, la Gallery affiche un message « Indisponible, vérifiez votre connexion » avec un bouton « Réessayer ». Sans token TMDB, l'Artwork image n'est pas cliquable.

## User Stories

1. As a user, I want to tap the Artwork image of a movie, so that I can see all the images TMDB has for it.
2. As a user, I want to tap the Artwork image of a show, so that I can see all the images TMDB has for the show.
3. As a user, I want the Gallery of a show to contain only the show's images, so that I am not flooded by season and episode images.
4. As a user, I want the Gallery to open from the blurred Artwork image variant too, so that the behaviour is the same whichever header style I chose.
5. As a user, I want the Gallery to open even when the banner displays an episode image, so that I always reach the show's Gallery.
6. As a user, I want to see backdrops and posters together in one grid, so that I can browse everything at once.
7. As a user, I want the grid to respect each image's real ratio (16:9 backdrops, 2:3 posters), so that no image is cropped.
8. As a user, I want the best-rated images first, so that I see the most relevant ones without scrolling.
9. As a user, I want 2 columns on a phone and 3 on a tablet, so that the grid uses the screen well.
10. As a user, I want to see every image whatever its language, so that I am not hiding the textless or foreign ones.
11. As a user, I want a loading indicator while the list loads, so that I know something is happening.
12. As a user, I want the grid to load lightweight thumbnails, so that it scrolls smoothly and uses little data.
13. As a user, I want to tap an image of the grid, so that it opens full screen.
14. As a user, I want the full screen viewer to show the image in its original resolution, so that I can see every detail.
15. As a user, I want to pinch to zoom in the viewer, so that I can inspect details.
16. As a user, I want to double-tap to switch between 1x and 2x, so that I can zoom quickly with one hand.
17. As a user, I want to swipe sideways to move to the next or previous image, so that I can browse without going back to the grid.
18. As a user, I want swiping to be disabled while the image is zoomed, so that I can pan the image without changing it by accident.
19. As a user, I want the viewer to follow the grid order, so that next and previous are predictable.
20. As a user, I want the viewer to show the whole image without cropping, so that I see it as TMDB provides it.
21. As a user, I want the back button to close the viewer first, so that I return to the grid where I was.
22. As a user, I want the back button to close the Gallery when the viewer is closed, so that I return to the Artwork page.
23. As a user, I want the grid to keep its scroll position after closing the viewer, so that I do not lose my place.
24. As a user without internet, I want to still open the Gallery and read « Indisponible, vérifiez votre connexion », so that I understand why nothing shows.
25. As a user without internet, I want a « Réessayer » button, so that I can reload once I am connected again.
26. As a user whose connection drops during loading, I want an error state with « Réessayer » without the page closing, so that I can retry in place.
27. As a user whose connection drops while browsing, I want already displayed images to stay visible, so that I can keep looking at them.
28. As a user without a TMDB token, I want the Artwork image to be inactive, so that I am not sent to a page that cannot work.
29. As a user who later enters a TMDB token, I want the Artwork image to become clickable, so that I can use the Gallery without restarting the app.
30. As a user, I want the Gallery title to show the Artwork's title, so that I know whose images I am browsing.
31. As a user, I want a Gallery with no image to show an empty state, so that a blank page does not look like a bug.
32. As a developer, I want the Gallery to store nothing in the database, so that there is no schema to migrate.

## Implementation Decisions

- **New route**: `Route.Gallery(artworkId, contentType)`, registered in the navigation configuration. The content type is required because TMDB uses different endpoints for movies and shows. The Artwork's title is read from the local database.
- **TMDB service**: new calls for movie images and show images. The response DTOs read `backdrops` and `posters` and ignore `logos`. Each image carries its file path, ratio (or width/height), `vote_average` and language, which is kept but not used for filtering in v1.
- **Repository and domain**: a domain model for a Gallery image (path, type backdrop or poster, ratio, rating) and a mapper from the DTO. The repository returns a single list, merging backdrops and posters and sorted by `vote_average` descending. For a show, only the show-level images are requested.
- **Availability**: no token means the Artwork image is not clickable (checked through the existing token data store). Being offline does not block the click: the Gallery itself shows the unavailable state, using the existing connectivity information.
- **Artwork image**: gets an optional click callback. Both variants (full and blurred) support it. The Show screens (regular and large) and the Artwork screen in Movie mode wire it to navigate to the Gallery.
- **Presentation**: a Gallery view model following the project's existing intent/state pattern. State covers loading, content (list, viewer open or not, current viewer index), unavailable-offline and error with retry. Retry reloads the list.
- **UI**: a single staggered vertical grid (2 columns on compact, 3 on larger widths). The viewer is an overlay inside the Gallery page, not a separate route. A pager shows the images, a zoomable image handles pinch, pan and double-tap (1x and 2x) with fit scaling, and the pager is locked while zoom is above 1x. Back closes the viewer first.
- **Image sizes**: grid uses `w342` for posters and `w780` for backdrops. The viewer uses `original`. Images are loaded through the existing image loading setup.
- **Persistence**: none. No new table, no new field.
- **Domain language**: `GLOSSARY.md` at the repository root defines Artwork, Gallery, Backdrop, Poster and Artwork image.

## Testing Decisions

- A good test checks external behaviour only (state exposed, values returned), never internal calls or private structure.
- **Main seam: the Gallery view model.** Use fakes for the TMDB images source, connectivity and token availability, and assert the exposed state:
  - loading, then the merged list sorted by rating descending (backdrops and posters interleaved);
  - offline gives the unavailable state, and retry reloads once back online;
  - a network error gives the error state, and retry recovers;
  - a show requests only show-level images;
  - opening the viewer on an image sets the right index, closing it returns to the grid;
  - an empty list gives the empty state.
- **Secondary seam: the TMDB service.** Extend the existing service tests to check that the images JSON is read correctly: backdrops and posters are mapped, logos are ignored, and show vs movie endpoints are the right ones.
- Prior art: the Player view model test and the sources use case tests (JVM, faked dependencies), and the existing TMDB service tests.
- Not covered by automated tests: pinch and swipe gestures, the staggered grid layout, the clickable condition on the Artwork image. These are checked with previews and by hand on a device.

## Out of Scope

- Logos, and season or episode images.
- Language filtering or language badges.
- Actions in the viewer: share, set as the Artwork's poster or banner. Saving an image is covered by the [Image Download spec](../../.scratch/image-download/specs/spec.md).
- Offline storage of the image list.
- A dedicated screen for the token request from the Gallery.
- Any change to the blurred or full Artwork image appearance.

## Further Notes

- The offline message is « Indisponible, vérifiez votre connexion », with a « Réessayer » button.
- Setting an image as the Artwork's poster or banner is a likely follow-up, and the read-only viewer keeps that possible.
- If a persistent image cache is ever wanted, add a nullable field on the existing Artwork entity rather than a new table, as the project prefers.
