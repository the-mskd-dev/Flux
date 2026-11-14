# 01: The Gallery lists images of every language

**What to build:** The Gallery images requests no longer restrict the language, so TMDB returns Backdrops, Posters and Logos in every Image language. The image DTO field `language` is renamed `languageCode` and kept on the Gallery image. Detail requests and Syncs are unchanged. Parent: GitHub issue #420.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] The Gallery shows Posters and Logos in languages other than the data language and English (e.g. Japanese)
- [ ] The automatic Logo and Poster of the Artwork pages are unchanged
- [ ] `languageCode` is available on each Gallery image
- [ ] Tests: TMDBServiceTest (no language parameters on Gallery requests, still present on detail requests), ImagesApiMapperTest (languageCode kept, logoPath after rename)
