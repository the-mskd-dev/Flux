# 02: Filter the Gallery by image type

**What to build:** A filter button in the Gallery top bar opens a bottom sheet modelled on `SearchGenresSheet`, with a "Type" section (Backdrops, Posters, Logos), multiple selection and a "Clear" button. Nothing selected shows everything. The grid and the viewer index use the filtered list; the button is hidden while the viewer is open and stays available when the result is empty (existing empty state). The filter is not persisted. Parent: GitHub issue #420.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] Selecting types narrows the grid; Clear restores it
- [ ] Swiping in the viewer only goes through the filtered images
- [ ] Empty result shows the existing empty state with the filter button still there
- [ ] Strings added in the 7 locales
- [ ] Tests: GalleryViewModelTest (type filter, clear, viewer on filtered list, empty state)
