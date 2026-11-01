# Spec : Image personnalisée

## Problem Statement

En haut de la page d'un Artwork, Flux affiche l'**Artwork image** : le Backdrop que TMDB considère comme principal, ou son Poster quand le header flou est activé (Movie uniquement). L'utilisateur n'a aucun contrôle sur ce choix. L'image de TMDB est parfois peu représentative, de mauvaise qualité, contient du texte dans une autre langue, ou ne plaît simplement pas à l'utilisateur. La **Gallery** lui montre pourtant des dizaines d'autres Backdrop et Poster du même Artwork, mais il ne peut que les regarder ou les télécharger (Image Download), pas les utiliser dans Flux.

## Solution

Depuis le visualiseur plein écran de la Gallery, l'utilisateur peut choisir l'image affichée comme Artwork image. Cette image devient l'**Image personnalisée** de l'Artwork.

Pour l'utilisateur :

- Un bouton texte centré en haut du visualiseur, **« Utiliser cette image »**, remplace l'Artwork image par l'image affichée.
- Sur l'image qui est déjà l'Artwork image (personnalisée ou TMDB par défaut), le bouton laisse place à un libellé désactivé **« Image actuelle »**.
- Seules les images du type affiché par le header sont proposées :
  - sur un **Show**, uniquement les Backdrop (la page Show n'a pas de header flou) ;
  - sur un **Movie**, les Backdrop quand le header flou est désactivé, les Poster quand il est activé.
- Un Artwork peut avoir au plus un Backdrop personnalisé et un Poster personnalisé (Movie uniquement). Changer le réglage du header flou ne perd rien : chaque mode retrouve sa propre Image personnalisée.
- Seule l'Artwork image change. Les vignettes du Catalog, de la recherche, du Private Folder et de la page Person gardent le Poster TMDB.
- L'Image personnalisée survit à la **Synchro légère**. Elle est effacée par la **Synchro complète**, l'**Identification manuelle** et la **Réinitialisation** : on revient alors à l'image TMDB par défaut.
- Il n'y a pas d'action « revenir à l'image par défaut » : on choisit une autre image, ou une Synchro complète remet tout à zéro.
- Le bouton est masqué pendant une Synchro.
- L'Image personnalisée est préchargée comme les autres images, donc visible hors connexion.

## User Stories

1. As a user, I want to pick a Backdrop from the Gallery as the image at the top of a Show page, so that the page looks the way I like.
2. As a user, I want to pick a Backdrop from the Gallery as the image at the top of a Movie page, so that the page looks the way I like.
3. As a user who enabled the blurred header, I want to pick a Poster from the Gallery for the top of a Movie page, so that the blurred header uses the Poster I prefer.
4. As a user, I want the choice to be made from the full screen viewer, so that I see the image in full before deciding.
5. As a user, I want a clear text button "Utiliser cette image" centred at the top of the viewer, so that the action is obvious and does not get mixed up with Image Download.
6. As a user, I want the button to appear only once the image is loaded, so that I never choose an image I have not seen.
7. As a user, I want the image currently displayed at the top of the page to be labelled "Image actuelle", so that I know which one is in use while swiping.
8. As a user, I want "Image actuelle" to also mark the TMDB default image when I have not chosen any, so that I can find the starting point.
9. As a user, I want the label to switch to "Image actuelle" right after I tap the button, so that I get immediate confirmation without a popup.
10. As a user, I want "Image actuelle" not to be tappable, so that I cannot trigger a useless action.
11. As a user browsing a Show Gallery, I want no button on Posters, so that I am not offered an image that the Show page cannot display.
12. As a user browsing a Movie Gallery without the blurred header, I want no button on Posters, so that I only choose images that fit the header.
13. As a user browsing a Movie Gallery with the blurred header, I want no button on Backdrops, so that I only choose images that fit the header.
14. As a user, I want the page to show my chosen image when I go back from the Gallery, so that I see the result straight away.
15. As a user on a tablet, I want the chosen image to be used in the large layout too, so that phone and tablet stay consistent.
16. As a user, I want my chosen Backdrop and my chosen Poster to be remembered separately, so that switching the blurred header setting back and forth does not lose either of them.
17. As a user switching the blurred header on, I want the Movie page to show my chosen Poster, or the TMDB Poster if I chose none, so that the header always shows something sensible.
18. As a user, I want the Catalog, search, Private Folder and Person thumbnails to keep their TMDB Poster, so that my choice only affects the Artwork page.
19. As a user, I want my chosen image to survive a Synchro légère, so that I do not have to choose it again after adding files.
20. As a user, I want my chosen image to survive a Synchro légère that adds new Episodes to the Show, so that adding an Episode does not reset the page.
21. As a user, I want a Synchro complète to bring back the TMDB images, so that I have a simple way to reset everything.
22. As a user, I want an Identification manuelle to bring back the TMDB image of the chosen Artwork, so that I never see an image from another Artwork.
23. As a user identifying an Artwork manually into one already in my Catalog, I want the result to show the TMDB default image, so that the behaviour stays simple and predictable.
24. As a user, I want a Réinitialisation to bring back the TMDB default image, so that resetting really resets.
25. As a user, I want the button to be hidden while a Synchro runs, so that my choice is not lost to a concurrent write.
26. As a user offline, I want my chosen image to be displayed, so that the page looks the same without a connection.
27. As a user, I want my chosen image to be prefetched after a Synchro, so that it is available offline like the TMDB images.
28. As a user with a Private Artwork, I want to choose its image like any other, so that the Private Folder works the same way.
29. As a user without a TMDB token, I want nothing to change, so that the feature never blocks anything (the Gallery is not reachable anyway).
30. As a user, I want the Unknown Artwork to be unaffected, so that this special group is never given an image by mistake.
31. As a user upgrading from an older version, I want my Catalog, progress, statuses, private flags and manual identifications untouched by the database change, so that I lose nothing.
32. As a user, I want Image Download to keep working next to the new button, so that I can still save images to my device.
33. As a developer, I want a single domain rule deciding which image is the Artwork image, so that the page header and the "Image actuelle" label always agree.
34. As a developer, I want behaviour tests that encode these rules, so that I can refactor without regressions.

## Implementation Decisions

**Domain**
- New term **Image personnalisée** in `GLOSSARY.md` (done), and **Artwork image** made more precise.
- The Artwork model gets two optional fields: a custom Backdrop path and a custom Poster path, both null by default. The TMDB Backdrop and Poster paths stay untouched: the TMDB data and the user choice are stored side by side.
- A single domain rule gives the Artwork image path from an Artwork and the header mode:
  - normal header: custom Backdrop, otherwise TMDB Backdrop;
  - blurred header (Movie only): custom Poster, otherwise TMDB Poster.
  The page header (normal and blurred, compact and large layouts, Movie and Show) and the Gallery use this rule. On the blurred Movie header, it replaces the direct read of the TMDB Poster.
- The Gallery image type that can be chosen follows from the same mode: Backdrop for a Show or a Movie with the normal header, Poster for a Movie with the blurred header.

**Database**
- The Artwork table gets two nullable text columns. Schema version 14 → 15 with an AutoMigration: every existing Artwork starts without an Image personnalisée.
- A new targeted write sets the custom Backdrop or custom Poster of one Artwork, without rewriting the rest of the row, in the same style as the existing private flag and Logo updates.
- The Catalog save used by the Synchro keeps the existing custom paths of each Artwork, the same way it already keeps the private flag and the manual-identification marker. A Synchro légère therefore never loses them, even when it rewrites the Artwork.
- Clearing all Images personnalisées is one bulk update, in the same style as clearing the Identifications manuelles.
- The list of images to prefetch includes the custom Backdrop and Poster paths.

**Synchro complète**
- The Image personnalisée clearing is done by the Synchro complète only, in the same place, and with the same enablement, as the clearing of the Identifications manuelles (a dedicated Étape de synchro, or the existing one extended; whichever stays the simplest). It runs before the catalogue step.

**Identification manuelle / Réinitialisation**
- The Artwork saved by an Identification manuelle has no Image personnalisée, even when the target Artwork already existed in the Catalog with one. The save of an identified folder must therefore explicitly clear the custom paths of the target, since the regular save keeps them.
- The Réinitialisation is specified but not implemented yet. When it is, it must clear the Image personnalisée of every Artwork it produces. This is a requirement on that future work, not a task of this spec.

**Gallery**
- The Gallery view model also needs: the current Artwork (its TMDB and custom paths, already partly loaded for the title, now observed), the header mode (blurred header setting from the customization store), and the Synchro state (as the Show and Movie pages already observe it for the identification menu).
- The Gallery content state exposes, for the image shown in the viewer, one of three states for the new button: *hidden*, *available* ("Utiliser cette image") or *current* ("Image actuelle"). It is hidden when the image type does not match the header mode, or a Synchro is running. It is *current* when the image path equals the Artwork image path given by the domain rule.
- A new intent, sent when the button is tapped, saves the image as the custom Backdrop or custom Poster according to its type. Since the Artwork is observed, the state switches to *current* by itself. A failed save is traced and the state is left unchanged, with no event.
- The viewer shows the button at the top centre, between the close button and the Image Download button, only once the image is loaded (same rule as Image Download). The *current* state shows a disabled "Image actuelle" text button.
- No snackbar, no badge in the grid.

**Strings**
- Two new strings: "Utiliser cette image" and "Image actuelle", added to every supported language, like the existing Gallery strings.

## Testing Decisions

A good test checks external behaviour through the highest existing seam: what the Catalog contains after a scenario, or what the Gallery state shows after an intent. It does not check which DAO method was called or how the rule is written.

**Seam 1: `SyncHarness` (real Room Catalog, fake TMDB)**
Prior art: `FullSyncManualIdentificationBehaviourTest`, `ManualIdentificationBehaviourTest`, `SyncLogoBehaviourTest`.
- Setting a custom Backdrop / Poster is read back from the Catalog.
- A Synchro légère keeps the Image personnalisée, including when a new Episode of the Show is added (Artwork rewritten).
- A Synchro complète clears both custom paths of every Artwork.
- An Identification manuelle produces an Artwork without an Image personnalisée, including when the target already existed with one.
- The images to prefetch include the custom paths.

**Seam 2: `GalleryViewModel`**
Prior art: `GalleryViewModelTest`.
- Button state per case: Show + Backdrop → available; Show + Poster → hidden; Movie normal + Backdrop → available, + Poster → hidden; Movie blurred + Poster → available, + Backdrop → hidden.
- The TMDB default image is *current* when there is no custom image; the custom image is *current* when there is one, and the TMDB default then becomes *available*.
- Hidden while a Synchro is running.
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
- An action to go back to the TMDB default image without a Synchro complète.
- A badge on the chosen image in the Gallery grid.
- Implementing the Réinitialisation itself (see `.scratch/identification-manuelle/specs.md`).
- The Logo: not part of the Gallery, not selectable.

## Further Notes

- The Gallery is only reachable with a TMDB token and never for the Unknown Artwork, so neither case needs extra rules.
- Custom paths are TMDB paths, so they go through the same image sizes and caches as the TMDB images.
- No ADR: the choice of two fields over one is easy to reverse and is recorded here.
