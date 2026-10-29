# 01: Download the displayed image into Pictures/Flux

Spec: `.scratch/image-download/specs/spec.md`

**What to build:** In the Gallery's full screen viewer, a download button at the top right (opposite the close button, using the existing `ic_download` icon) performs an **Image Download** of the image on screen. The original resolution file is copied into the device's image storage, in a "Flux" album visible in the system gallery, without any permission prompt. The bytes are read from Coil's disk cache first (the raw TMDB `original` file, untouched), and fetched from the network only when the cache has no entry. The file is named after the Artwork's title and the TMDB path, cleaned of forbidden characters; downloading the same image twice keeps both copies. While the Image Download runs, the icon is replaced by a loading indicator and the button is disabled, so a double tap cannot start two. The success and failure outcomes are emitted as one-off events (displayed by ticket 02). In this ticket the button is always visible (ticket 03 refines that), and its spoken description is translated in the 7 existing languages.

The only platform specific step is a small "save these bytes as an image" interface in common code, with the Android implementation bound in the Android Koin module. No iOS or Desktop stub.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] Tapping the button in the viewer downloads the image of the current page (after a swipe, the new current image), and the file shows up in `Pictures/Flux` in the system gallery, in original resolution and without recompression
- [ ] No storage permission is requested
- [ ] The use case and the saver interface contain no `android.*` / `androidx.*` import; only the Android implementation of the saver uses `MediaStore`
- [ ] With the image in Coil's disk cache, the Image Download works in airplane mode and does not hit the network
- [ ] With no cache entry, the image is fetched from the network; with neither source available, the Image Download fails
- [ ] File name is `<Artwork title>_<basename of the TMDB path>.<ext>`, forbidden characters are cleaned (an Artwork titled "Face/Off" does not fail), extension and MIME type come from the TMDB path
- [ ] Downloading the same image twice yields two files (no overwrite, no failure)
- [ ] While running, the icon is replaced by a loading indicator and the button is disabled; a second tap is ignored by the view model
- [ ] A tap with the viewer closed does nothing; the rest of the Gallery state (images, viewer index, failure) is untouched
- [ ] The view model emits an Image Download succeeded event on success and an Image Download failed event on any failure (cancellation is rethrown, not reported)
- [ ] The Image Download is not cancelled when the user swipes to another page meanwhile
- [ ] The button's content description exists in en, fr, de, es, it, ja, ko
- [ ] Nothing is persisted in the database
- [ ] Gallery view model tests cover: downloads the image at the viewer index, running flag true then false, success event, failure event, second tap ignored, viewer closed
- [ ] Use case tests (JVM, faked cache, network source and saver) cover: cache hit does not call the network, cache miss uses the network, neither source fails and the saver is not called, saver failure is propagated, file name building and cleaning
- [ ] `docs/specs/gallery.md`: "save" is removed from Out of Scope and a link to the Image Download spec is added ("share" and "set as poster/banner" stay out)
- [ ] Manually checked on a device or emulator: download online, download in airplane mode on an already displayed image, download twice
