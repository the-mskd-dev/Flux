# Spec: Image Download from the Gallery viewer

Related: `docs/specs/gallery.md` (this spec lifts its "save" Out of Scope item; "share" and "set as poster/banner" stay out).

## Problem Statement

In the Gallery viewer I can look at a Backdrop or a Poster in its original resolution and zoom into it, but I cannot keep it. If I want to use an image as a wallpaper, send it to someone or keep it, I have to find it again outside Flux, on TMDB's website.

## Solution

The full screen viewer of the Gallery gets a download button at the top right, facing the close button. A tap performs an **Image Download** of the image on screen: the original resolution file is copied into the device's image storage (visible in the system gallery). The button only appears once the image is loaded, shows a loading indicator while the Image Download runs, and a snackbar confirms success or reports a failure. An image already displayed stays downloadable without a connection. The design isolates the only platform specific step, so iOS and Desktop need nothing more than a new implementation of it.

## User Stories

1. As a user, I want a download button at the top right of the full screen viewer, so that I can keep the image I am looking at.
2. As a user, I want the download button to use the existing download icon, so that its meaning is obvious.
3. As a user, I want the button placed opposite the close button, so that the two actions are not confused.
4. As a user, I want the button to download the image of the current page, so that swiping to another image and tapping downloads that one.
5. As a user, I want the file to be in the original resolution, so that nothing is lost compared to what I see in the viewer.
6. As a user, I want the file to be exactly the one TMDB provides, without recompression, so that the quality is the best available.
7. As a user, I want the downloaded image to appear in my system gallery, so that I can use it from any other app.
8. As a user, I want the images grouped in a "Flux" album, so that I can find them easily.
9. As a user, I want no storage permission prompt, so that the download is one tap.
10. As a user, I want the file name to start with the Artwork's title, so that I recognise it among my pictures.
11. As a user, I want the file name to stay unique per image, so that two images of the same Artwork never collide.
12. As a user, I want file names free of characters that a file system rejects, so that an Artwork title like "Face/Off" does not make the download fail.
13. As a user, I want downloading the same image twice to keep both copies rather than overwrite or fail, so that I never lose a file by accident.
14. As a user, I want the button hidden while the image is loading, so that I never download something I cannot see yet.
15. As a user, I want the button hidden if the image failed to load, so that I am not offered an action that cannot succeed.
16. As a user, I want the button to reappear when I come back to an already loaded image, so that swiping around does not hide it for good.
17. As a user, I want the button to stay visible while I zoom, so that I can download a detail view's image without zooming out first.
18. As a user, I want a loading indicator in place of the icon while the Image Download runs, so that I know it is in progress.
19. As a user, I want the button disabled while an Image Download runs, so that I cannot start two by double tapping.
20. As a user, I want a snackbar confirming the Image Download, so that I know it worked.
21. As a user, I want a snackbar when the Image Download fails, so that I know to retry.
22. As a user, I want a single clear failure message, so that I am not confronted with technical causes.
23. As a user without internet, I want to download an image that is already displayed, so that losing my connection does not block me.
24. As a user without internet, I want a failure message if the image is not available locally, so that I understand nothing was saved.
25. As a user, I want the snackbar to be visible above the full screen viewer, so that I do not miss it.
26. As a user, I want the Image Download to continue if I swipe to another image meanwhile, so that it is not cancelled by a gesture.
27. As a user, I want the confirmation and failure messages translated in my language, so that they are understandable (English, French, German, Spanish, Italian, Japanese, Korean).
28. As a user relying on a screen reader, I want the button to have a spoken description, so that I can find it.
29. As a user, I want the Gallery grid and the rest of the viewer to behave exactly as before, so that nothing regresses (zoom, swipe, back handling).
30. As a developer, I want the only platform specific code to be a small "save these bytes as an image" interface, so that iOS and Desktop need one implementation each.
31. As a developer, I want the network and cache logic to live in common code, so that it is written and tested once.
32. As a developer, I want the word "Download" kept free for a future download of remote server files, so that the two features do not share a name.
33. As a developer, I want nothing persisted in the database, so that there is no schema to migrate.

## Implementation Decisions

- **Vocabulary**: the feature is an **Image Download** (defined in `GLOSSARY.md`). The bare word "Download" is reserved for a future feature that downloads files from a remote server, so it is not used for names in this feature.
- **Where the button lives**: only in the full screen viewer. Nothing is added to the grid.
- **Visibility rule**: the button of a page shows only once the image of that page has finished loading. It is hidden while loading and when the load failed. This is a purely visual state, held by the viewer composable: the per image loading state, today local to each page, is reported upward so the viewer knows whether the current page is loaded. It is not part of the view model state.
- **Which image**: the image at the current viewer index.
- **Pure Kotlin use case** `ImageDownloadUseCase` in the Gallery domain layer, with no Android or AndroidX import (KMP isolation rule). Steps: get the raw bytes of the image's original URL, build the file name, hand both to the platform saver.
  - **Bytes source, cache first**: read the entry of Coil's disk cache for the original URL. The entry holds the raw HTTP response bytes, i.e. TMDB's `original` file untouched, so cached and network bytes are the same quality. If the entry is absent (image evicted from the cache between display and tap), fetch the same URL with the existing Ktor image client. No connectivity check beforehand: the use case fails only when neither source has the image.
  - **File name**: `<Artwork title>_<basename of the TMDB path>.<extension>`. The extension and the MIME type come from the TMDB path extension. Characters forbidden by file systems are replaced or removed.
- **Platform seam** `ImageSaver`, an interface in the common `system` package next to `UrlLauncher`, `EmailLauncher` and `FilesLauncher`: it takes the bytes, the file name and the MIME type and returns a `Result<Unit>`, never throwing.
  - **Android implementation** writes through `MediaStore.Images` into `Pictures/Flux/`. The project's minSdk is 29, so no storage permission is requested. The file is flagged pending while written, then published. A name collision is resolved by the system with a numeric suffix, the code does not deduplicate.
  - It is bound in the Android system Koin module.
  - **No iOS or Desktop stub** is added. A future platform only has to provide its own `ImageSaver` (Photos library on iOS, Images folder on Desktop) and register it.
- **Presentation** (existing Gallery MVI feature):
  - new intent for the tap on the viewer's download button;
  - the content state gains a boolean for "an Image Download is running". The intent is ignored while it is true;
  - two new one-off events: Image Download succeeded, Image Download failed. A single failure event covers every cause (offline, no source, write error);
  - the view model resolves the image from the current viewer index, runs the use case in its own scope (so a page swipe does not cancel it) and emits the events. Errors are caught, and cancellation is rethrown, as already done for the images loading.
- **UI**:
  - the button sits at the top end, aligned with the close button and with the same status bar padding. It uses the existing `ic_download` drawable, white tint, and shows the loading indicator instead of the icon while the Image Download runs;
  - the Gallery screen collects the two events and shows a snackbar. No snackbar host is used elsewhere in the project, so one is introduced in the Gallery content and drawn above the viewer overlay, so that the full screen viewer does not hide it;
  - new strings (button description, success message, failure message) in all 7 existing languages (default English, `fr`, `de`, `es`, `it`, `ja`, `ko`).
- **Koin**: the use case is registered in the Gallery module and receives the UI image loader (for its disk cache), the image HTTP client and the saver.
- **Persistence**: none. No table, no field, no setting.

## Testing Decisions

- A good test checks external behaviour only (state exposed, events emitted, bytes handed to the saver), never the internal calls or the private structure.
- **Main seam: the Gallery view model** (existing `GalleryViewModelTest`, Kotest FunSpec, MockK, Turbine). With a faked use case, assert the exposed state and events:
  - a tap on the download intent downloads the image at the current viewer index;
  - the running flag is true during the Image Download and false after;
  - success emits the success event, a failure emits the failure event;
  - a second tap while running is ignored;
  - a tap with the viewer closed does nothing;
  - the rest of the state (images, viewer index, failure) is untouched.
- **Secondary seam: `ImageDownloadUseCase`** (JVM test, faked cache, faked network source, faked saver):
  - cache hit: the saver receives the cached bytes and the network source is not called;
  - cache miss: the saver receives the network bytes;
  - neither source: failure, the saver is not called;
  - saver failure is propagated;
  - the file name: Artwork title and path basename, forbidden characters cleaned, extension and MIME type from the TMDB path.
- **Not covered by automated tests**: the Android `MediaStore` implementation, the visibility of the button as the image loads, the loading indicator and the snackbar. They are checked by hand on a device or an emulator (download online, download in airplane mode on an already displayed image, download twice, check `Pictures/Flux` in the system gallery) and with previews.
- **Prior art**: the existing `GalleryViewModelTest`, the use case tests of the Sources and Player features (JVM, faked dependencies).

## Out of Scope

- Sharing an image, setting it as the Artwork's poster or banner.
- Downloading from the grid, multi-selection, "download all".
- Choosing the destination folder or the file name.
- Re-encoding or converting the format.
- A cancel button, a progress percentage, a system notification.
- Different messages for offline and other failures.
- Downloading remote server files: the future feature that will own the bare word "Download".
- iOS and Desktop implementations of the saver.
- Any change to the grid, the zoom, the swipe, or the back handling of the viewer.

## Further Notes

- `docs/specs/gallery.md` lists "Actions in the viewer: share, save, set as the Artwork's poster or banner" as out of scope. When this feature lands, only "save" is removed from that list, and a link to this spec is added.
- No ADR: the cache-then-network source and the platform seam are real trade-offs and slightly surprising, but both are cheap to undo, so the reasoning lives here.
- `Gallery` in `GLOSSARY.md` says nothing is stored locally. That sentence is about Flux's own catalog and database, and still holds: the file produced by an Image Download is outside Flux (see **Image Download**).
