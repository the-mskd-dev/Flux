# 01: Logos appear in the Gallery

**What to build:** The Gallery lists the Logo of an Artwork alongside its Backdrop and Poster, in the same grid sorted by descending rating. Only PNG logos are listed (same rule as the automatic Logo), in the data language, English and without language (the all-languages request is a later ticket). Logo thumbnails reuse the Logo size and are shown on a neutral background. The full-screen viewer opens on a Logo, with no action button for it yet. Parent: GitHub issue #420.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] A Gallery with Logos shows them mixed with Backdrops and Posters, sorted by rating
- [ ] Non-PNG logos are not listed
- [ ] Logo thumbnails are readable on a neutral background and the viewer opens on a Logo
- [ ] Tests: image mapper (PNG only), GetGalleryImagesUseCase, GalleryViewModelTest (viewer on a Logo)
