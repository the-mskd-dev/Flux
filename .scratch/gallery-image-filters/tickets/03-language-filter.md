# 03: Filter the Gallery by Image language

**What to build:** The filter sheet gets a "Language" section: Global (images without language) plus the languages present in the loaded images, ordered data language, English, Global, then others by descending image count. Multiple selection, OR within the section, AND with the type section, included in "Clear". Labels: project `Language` name, else device language name, else uppercase code; "Global" for no language. A selected language with no image for the current type gives the empty state; the selection is not auto-cleared. Parent: GitHub issue #420.

**Blocked by:** 01, 02

**Status:** ready-for-agent

- [ ] Language chips reflect the images actually loaded, in the specified order
- [ ] Language and type filters combine as AND between sections, OR within
- [ ] Clear resets both sections
- [ ] Strings added in the 7 locales
- [ ] Tests: GalleryViewModelTest (language filter, combination, ordering, clear)
