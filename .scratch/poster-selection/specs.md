# Spec: Custom image

## Problem Statement

At the top of an Artwork page, Flux displays the **Artwork image**: the Backdrop that TMDB considers the main one, or its Poster when the blurred header is enabled (Movie only). The user has no control over this choice. The TMDB image is sometimes not very representative, of poor quality, contains text in another language, or the user simply does not like it. Yet the **Gallery** shows them dozens of other Backdrop and Poster of the same Artwork, but they can only look at them or download them (Image Download), not use them in Flux.

## Solution

From the full screen viewer of the Gallery, the user can choose the displayed image as the Artwork image. This image becomes the **Custom image** of the Artwork.

For the user:

- A text button centred at the top of the viewer, **"Use as poster"** on a Poster and **"Use as backdrop"** on a Backdrop, makes the displayed image the custom Poster or the custom Backdrop of the Artwork.
- On the image that is already the shown Poster or Backdrop (custom or TMDB default), the button gives way to a disabled **"Current poster"** or **"Current backdrop"** label.
- The button is offered on every image, Backdrop and Poster, for a Movie and for a Show, whatever the blurred header setting.
- An Artwork can have at most one custom Backdrop and one custom Poster. Changing the blurred header setting loses nothing: each mode finds its own Custom image again.
- The custom Backdrop replaces the Backdrop of the Artwork image. The custom Poster replaces the Poster of the Artwork image (blurred header) and the thumbnail of the Artwork in the Catalog, the search and the Private Folder. The thumbnails of the Person page keep the TMDB Poster.
- The Custom image survives the **Light sync**. It is cleared by the **Full sync** and the **Identification reset**: the default TMDB image is then brought back. The **Manual identification** does not transfer the Custom image of the source Artwork; if the target Artwork was already in the Catalog, it keeps its own.
- There is no "go back to the default image" action: you choose another image, or a Full sync resets everything.
- The button is hidden during a Sync.
- The Custom image is prefetched like the other images, so it is visible offline.

## Revision (2026-11-02)

The button no longer depends on the header mode: both types can always be chosen, with a label per type ("Use as poster" / "Use as backdrop"), and the custom Poster also replaces the Catalog, search and Private Folder thumbnails (`CatalogArtworkItem`, `SearchContentGrid`, `PrivateContentGrid`). This supersedes user stories 5, 12, 13 and 18, the "Gallery image type that can be chosen follows from the same mode" rule, the header mode dependency of the Gallery view model, the "Use this image" and "Current image" strings and the corresponding button-state tests below.

## User Stories

1. As a user, I want to pick a Backdrop from the Gallery as the image at the top of a Show page, so that the page looks the way I like.
2. As a user, I want to pick a Backdrop from the Gallery as the image at the top of a Movie page, so that the page looks the way I like.
3. As a user who enabled the blurred header, I want to pick a Poster from the Gallery for the top of a Movie page, so that the blurred header uses the Poster I prefer.
4. As a user, I want the choice to be made from the full screen viewer, so that I see the image in full before deciding.
5. As a user, I want a clear text button "Use this image" centred at the top of the viewer, so that the action is obvious and does not get mixed up with Image Download.
6. As a user, I want the button to appear only once the image is loaded, so that I never choose an image I have not seen.
7. As a user, I want the image currently displayed at the top of the page to be labelled "Current image", so that I know which one is in use while swiping.
8. As a user, I want "Current image" to also mark the TMDB default image when I have not chosen any, so that I can find the starting point.
9. As a user, I want the label to switch to "Current image" right after I tap the button, so that I get immediate confirmation without a popup.
10. As a user, I want "Current image" not to be tappable, so that I cannot trigger a useless action.
11. As a user browsing a Show Gallery, I want no button on Posters, so that I am not offered an image that the Show page cannot display.
12. As a user browsing a Movie Gallery without the blurred header, I want no button on Posters, so that I only choose images that fit the header.
13. As a user browsing a Movie Gallery with the blurred header, I want no button on Backdrops, so that I only choose images that fit the header.
14. As a user, I want the page to show my chosen image when I go back from the Gallery, so that I see the result straight away.
15. As a user on a tablet, I want the chosen image to be used in the large layout too, so that phone and tablet stay consistent.
16. As a user, I want my chosen Backdrop and my chosen Poster to be remembered separately, so that switching the blurred header setting back and forth does not lose either of them.
17. As a user switching the blurred header on, I want the Movie page to show my chosen Poster, or the TMDB Poster if I chose none, so that the header always shows something sensible.
18. As a user, I want the Catalog, search, Private Folder and Person thumbnails to keep their TMDB Poster, so that my choice only affects the Artwork page.
19. As a user, I want my chosen image to survive a Light sync, so that I do not have to choose it again after adding files.
20. As a user, I want my chosen image to survive a Light sync that adds new Episodes to the Show, so that adding an Episode does not reset the page.
21. As a user, I want a Full sync to bring back the TMDB images, so that I have a simple way to reset everything.
22. As a user, I want a Manual identification to a new Artwork to show the TMDB image of the chosen Artwork, so that I never see an image from another Artwork.
23. As a user identifying an Artwork manually into one already in my Catalog, I want that Artwork to keep its Custom image, so that I don't lose a choice I already made.
24. As a user, I want an Identification reset to bring back the TMDB default image, so that resetting really resets.
25. As a user, I want the button to be hidden while a Sync runs, so that my choice is not lost to a concurrent write.
26. As a user offline, I want my chosen image to be displayed, so that the page looks the same without a connection.
27. As a user, I want my chosen image to be prefetched after a Sync, so that it is available offline like the TMDB images.
28. As a user with a Private Artwork, I want to choose its image like any other, so that the Private Folder works the same way.
29. As a user without a TMDB token, I want nothing to change, so that the feature never blocks anything (the Gallery is not reachable anyway).
30. As a user, I want the Unknown Artwork to be unaffected, so that this special group is never given an image by mistake.
31. As a user upgrading from an older version, I want my Catalog, progress, statuses, private flags and manual identifications untouched by the database change, so that I lose nothing.
32. As a user, I want Image Download to keep working next to the new button, so that I can still save images to my device.
33. As a developer, I want a single domain rule deciding which image is the Artwork image, so that the page header and the "Current image" label always agree.
34. As a developer, I want behaviour tests that encode these rules, so that I can refactor without regressions.

## Implementation Decisions

**Domain**
- New term **Custom image** in `GLOSSARY.md` (done), and **Artwork image** made more precise.
- The Artwork model gets two optional fields: a custom Backdrop path and a custom Poster path, both null by default. The TMDB Backdrop and Poster paths stay untouched: the TMDB data and the user choice are stored side by side.
- A single domain rule gives the Artwork image path from an Artwork and the header mode:
  - normal header: custom Backdrop, otherwise TMDB Backdrop;
  - blurred header (Movie only): custom Poster, otherwise TMDB Poster.
  The page header (normal and blurred, compact and large layouts, Movie and Show) and the Gallery use this rule. On the blurred Movie header, it replaces the direct read of the TMDB Poster.
- The Gallery image type that can be chosen follows from the same mode: Backdrop for a Show or a Movie with the normal header, Poster for a Movie with the blurred header.

**Database**
- The Artwork table gets two nullable text columns. Schema version 14 → 15 with an AutoMigration: every existing Artwork starts without a Custom image.
- A new targeted write sets the custom Backdrop or custom Poster of one Artwork, without rewriting the rest of the row, in the same style as the existing private flag and Logo updates.
- The Catalog save used by the Sync keeps the existing custom paths of each Artwork, the same way it already keeps the private flag and the manual-identification marker. A Light sync therefore never loses them, even when it rewrites the Artwork.
- Clearing all Custom images is one bulk update, in the same style as clearing the Manual identifications.
- The list of images to prefetch includes the custom Backdrop and Poster paths.

**Full sync**
- The Custom image clearing is done by the Full sync only, in the same place, and with the same enablement, as the clearing of the Manual identifications (a dedicated Sync step, or the existing one extended; whichever stays the simplest). It runs before the Catalog step.

**Manual identification / Identification reset**
- The Custom image of the source Artwork does not follow its Media: it is deleted with the source Artwork. The target Artwork keeps its own Custom image when it already existed in the Catalog, since the regular save keeps the existing custom paths; a new target has none.
- The Identification reset is specified but not implemented yet. When it is, it must clear the Custom image of every Artwork it produces. This is a requirement on that future work, not a task of this spec.

**Gallery**
- The Gallery view model also needs: the current Artwork (its TMDB and custom paths, already partly loaded for the title, now observed), the header mode (blurred header setting from the customization store), and the Sync state (as the Show and Movie pages already observe it for the identification menu).
- The Gallery content state exposes, for the image shown in the viewer, one of three states for the new button: *hidden*, *available* ("Use this image") or *current* ("Current image"). It is hidden when the image type does not match the header mode, or a Sync is running. It is *current* when the image path equals the Artwork image path given by the domain rule.
- A new intent, sent when the button is tapped, saves the image as the custom Backdrop or custom Poster according to its type. Since the Artwork is observed, the state switches to *current* by itself. A failed save is traced and the state is left unchanged, with no event.
- The viewer shows the button at the top centre, between the close button and the Image Download button, only once the image is loaded (same rule as Image Download). The *current* state shows a disabled "Current image" text button.
- No snackbar, no badge in the grid.

**Strings**
- Two new strings: "Use this image" and "Current image", added to every supported language, like the existing Gallery strings.

## Testing Decisions

A good test checks external behaviour through the highest existing seam: what the Catalog contains after a scenario, or what the Gallery state shows after an intent. It does not check which DAO method was called or how the rule is written.

**Seam 1: `SyncHarness` (real Room Catalog, fake TMDB)**
Prior art: `FullSyncManualIdentificationBehaviourTest`, `ManualIdentificationBehaviourTest`, `SyncLogoBehaviourTest`.
- Setting a custom Backdrop / Poster is read back from the Catalog.
- A Light sync keeps the Custom image, including when a new Episode of the Show is added (Artwork rewritten).
- A Full sync clears both custom paths of every Artwork.
- A Manual identification to a new Artwork produces an Artwork without a Custom image; into an Artwork already in the Catalog, that Artwork keeps its own.
- The images to prefetch include the custom paths.

**Seam 2: `GalleryViewModel`**
Prior art: `GalleryViewModelTest`.
- Button state per case: Show + Backdrop → available; Show + Poster → hidden; Movie normal + Backdrop → available, + Poster → hidden; Movie blurred + Poster → available, + Backdrop → hidden.
- The TMDB default image is *current* when there is no custom image; the custom image is *current* when there is one, and the TMDB default then becomes *available*.
- Hidden while a Sync is running.
- Tapping the button saves the image with the right type and the state becomes *current*.
- A failed save leaves the state unchanged.

**Seam 3: migration**
Prior art: `Migration13To14Test`.
- Migration 14 → 15 keeps existing rows and their data, with both new columns null.

The header display itself has no dedicated test: it uses the same domain rule as the *current* state, which seam 2 covers.

## Out of Scope

- Changing the Poster of the Catalog, search, Private Folder or Person thumbnails.
- Choosing an image for a Season or an Episode.
- Choosing an image that is not in the Gallery (device image, URL).
- An action to go back to the TMDB default image without a Full sync.
- A badge on the chosen image in the Gallery grid.
- Implementing the Identification reset itself (see `.scratch/manual-identification/specs.md`).
- The Logo: not part of the Gallery, not selectable.

## Further Notes

- The Gallery is only reachable with a TMDB token and never for the Unknown Artwork, so neither case needs extra rules.
- Custom paths are TMDB paths, so they go through the same image sizes and caches as the TMDB images.
- No ADR: the choice of two fields over one is easy to reverse and is recorded here.
