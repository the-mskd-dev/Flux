# 02: Display the Logo instead of the title text

**What to build:** On the page of an Artwork, the **Logo** replaces the title text when the Artwork has one, at the same place as the title today. Without a Logo, the current title text stays. While the Logo loads or if it fails, the title text is shown (no placeholder, no error image), then the Logo replaces it with a crossfade once loaded. A new Artwork synchronised online shows its Logo.

Where it applies: the movie header of the Artwork screen, the movie title of the Artwork screen on large screens, and the Show screen (regular and large). On large screens a Show keeps the title of the selected Season as text; only movies show the Logo there. The top bar of the Show screen keeps the title text. No light-theme specific handling: the Logo is displayed as is in both themes (known limit, see the spec).

This ticket also defines the single URL used to display a Logo (TMDB `w500`), which the prefetch (ticket 04) must reuse.

**Blocked by:** 01: A new Artwork gets its Logo during the Synchro

**Status:** ready-for-agent

- [ ] One composable shows the title of an Artwork: the Logo when its Logo path is neither null nor empty, the existing main title text otherwise.
- [ ] It replaces the main title in the movie header of the Artwork screen, in the movie title of the large Artwork screen, and in both the regular and large Show screens.
- [ ] The large Artwork screen keeps the Season title as text for a Show.
- [ ] The Show top bar still shows the title text.
- [ ] Title text is shown while the Logo is loading and when it fails; once loaded, the Logo replaces it with a crossfade.
- [ ] Layout: centred at the same place as the title (straddling the bottom of the Artwork image), content scale fit, maximum height 72 dp on phone and 96 dp on large screens, maximum width 70 % of the available width.
- [ ] The content description of the Logo is the Artwork title.
- [ ] A single URL builder (TMDB `w500`) is used for display and is reusable by the prefetch.
- [ ] Previews exist for: with Logo, without Logo, loading or failed.
- [ ] Manual check: after a Synchro online, a new Artwork shows its Logo on the movie and Show pages (phone and large layouts); an Artwork without Logo shows the title text.
