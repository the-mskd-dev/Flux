# Language update

## Problem Statement

When I change the data language in the settings, Flux re-fetches the titles and descriptions of my Catalog (Movies, Shows, Seasons, Episodes), refreshes the Genres and looks for the Logo of each Artwork again in the new language. This works, but it is built as a second, separate process next to the Sync, not as a Sync. As a result:

- I see no Sync progress while it runs: the percentage never moves, because this process never reports it.
- The behaviour of its Logo and Genre handling can drift from the Sync's own (it re-implements them), so a fix made for the Sync (for example a missing TMDB token) does not reach it.
- As the maintainer, I have two places to change when TMDB data is fetched: the Sync pipeline, where GLOSSARY.md says an Enrichment belongs, and a 160-line use case with four near-identical translate-and-save blocks.

## Solution

A language change becomes one more kind of **Sync**, run by the same Sync pipeline module as the Light sync and the Full sync. It reuses the existing Sync steps for Genres and Logos, adds a translation step in place of the four hand-written blocks, skips everything that only makes sense when walking the UserFolder (scan, removal of Missing files, fetching new files), and reports Sync progress like any other Sync.

For the user, the visible result is the same (the Catalog is shown in the new language, Logos for the new language) plus a progress indicator that moves, and one consistent behaviour across Sync and language change.

## User Stories

1. As a viewer, I want to change the data language in the settings and see my Catalog titles and descriptions in that language, so that I read the Catalog in my language.
2. As a viewer, I want Movie titles and descriptions translated, so that Movies match my language.
3. As a viewer, I want Show titles and descriptions translated, so that Shows match my language.
4. As a viewer, I want Season titles and descriptions translated, so that Season pages match my language.
5. As a viewer, I want Episode titles and descriptions translated, so that Episode lists match my language.
6. As a viewer, I want a title or description that TMDB has no translation for to keep its previous text, so that nothing becomes blank.
7. As a viewer, I want an empty translated field to keep the previous value, so that partial translations never erase data.
8. As a viewer, I want the Genres refreshed in the new language, so that filters and labels are consistent.
9. As a viewer, I want each Artwork's Logo searched again for the new language, so that the stylised title matches.
10. As a viewer, I want an Artwork for which the Logo lookup fails to keep its previous Logo, so that a network hiccup does not remove it.
11. As a viewer with a Custom image (Backdrop, Poster or Logo), I want my choices kept when I change the language, so that I do not lose my customisation (as for a Light sync).
12. As a viewer, I want my Progress, Status and History untouched by a language change, so that I keep my place.
13. As a viewer, I want my Private Artwork translated like the others, so that the Private Folder is consistent with the Catalog.
14. As a viewer, I want the Unknown Artwork left untouched (no TMDB data to translate, no Logo), so that it does not trigger useless calls.
15. As a viewer, I want a Media that is Unavailable (Unavailable source) to be translated too, so that the Catalog is consistent when the source returns.
16. As a viewer, I want the language change not to delete or re-match any file (no Missing file removal, no identification), so that changing the language is safe.
17. As a viewer, I want a Manual identification to be preserved by a language change, so that my corrections are not lost.
18. As a viewer, I want to see Sync progress while the language change runs, so that I know it is working.
19. As a viewer, I want the Sync progress never to go backwards and to reach 100 % only at the end, so that the indicator is trustworthy.
20. As a viewer, I want the same "sync in progress" state shown for a language change as for other Syncs, so that I do not have to learn a new one.
21. As a viewer, I want to be able to keep using the app while the language change runs, so that I am not blocked.
22. As a viewer, I want a language change to cancel a Sync already running (and the reverse, as today for Sync), so that two operations do not overwrite each other.
23. As a viewer, I want a language change started while a Full sync is running to restart cleanly in the new language, so that no old-language data is saved last.
24. As a viewer, I want the Artwork images prefetched after the language change, so that the new Logos are ready.
25. As a viewer, I want the translated title to be saved after the Logo-affecting rows are read, so that a stale Artwork row never overwrites a newly saved Logo path.
26. As a viewer without a TMDB token, I want the language change not to fire lookups that can only fail, so that it ends quickly without errors.
27. As a viewer with no network, I want the language change to end without crashing and keep the existing text, so that the Catalog stays usable.
28. As a viewer, I want a failure on one item (translation or Logo) not to stop the others, so that one bad item does not block the rest.
29. As a viewer, I want changing the language back to the previous one to work the same way, so that I can switch freely.
30. As a viewer, I want choosing the same language again to do no harm, so that a repeated tap is idempotent.
31. As a viewer with a large Catalog, I want requests grouped in batches within the TMDB call limit, so that I am not throttled.
32. As a viewer, I want a Failed file concept not to apply (nothing is a Failed file for a language change), so that failures do not show up as unrecognised files.
33. As a maintainer, I want the language change to go through the Sync pipeline module, so that Sync progress, busy-state and cancellation are written once.
34. As a maintainer, I want one translation step instead of four copies of translate-and-save, so that adding a field to translate is a single change.
35. As a maintainer, I want Genres and Logos to reuse the existing steps, so that a fix to them applies to the language change.
36. As a maintainer, I want the Logos step to support "refresh every Artwork" as well as "only unsearched Artwork", so that both Syncs share the step.
37. As a maintainer, I want each Sync step to say whether it runs in a given kind of Sync, so that the language change skips scan and file steps without special cases in the pipeline.
38. As a maintainer, I want the language-change tests to cross the same Sync harness as the other Sync behaviour tests, so that I learn one way of testing the Catalog.
39. As a maintainer, I want the settings screen to only say "the language changed", so that it does not know how the Catalog gets updated.
40. As a maintainer, I want the KDoc of the pipeline to use the GLOSSARY.md English vocabulary (Sync, Sync step, Sync progress, Enrichment, Failed file), so that the code and glossary agree.

## Implementation Decisions

- The language change is a third kind of Sync on the existing **Sync pipeline** module, next to the Light sync and the Full sync. It is started through the same Sync entry (the module that owns busy-state, cancellation of the previous run and Sync progress), not through its own use case. The settings screen asks the Sync entry for a language change after saving the new data language.
- Kinds of Sync are expressed in the Sync context (today a boolean "full" flag): it grows to say which kind of Sync is running (Light, Full, Language change). Each Sync step decides from the context whether it is enabled, as the Genres step does today for the Full sync; no special-casing in the pipeline itself.
- Scan, removal of Missing files, user-choice reset, catalog (identification and fetching of new files), durations, Nsfw private and finalization steps are not enabled for a language change. The pipeline's rule "the scan step runs first" is lifted for a Sync that does not walk the UserFolder.
- New **translation step**: translates, for the whole Catalog (Movies, Shows, Seasons, Episodes, Private Artwork included, Unknown Artwork excluded), title and description in the data language. One generic translate-and-save routine per kind of item replaces the four hand-written blocks. A missing or blank translated field keeps the previous value. A failed lookup for one item leaves it untouched and never stops the Sync (it is not a Failed file). Batches of 25 within a limited parallelism, as today.
- The **Genres step** is enabled for a language change as well as a Full sync, reusing the existing genre sync.
- The **Logos step** gains a mode: for a language change, every Artwork except the Unknown Artwork is searched again (today's language-change behaviour), where a Light sync only searches Artwork never searched. A failed lookup keeps the previous Logo. Like for the Sync, no lookup is made without a TMDB token. This replaces the duplicated Logo search.
- **Ordering constraint** kept from today: Logos run after the translations are saved, because the translated Artwork rows read earlier would overwrite the new Logo paths. The pipeline's step order encodes this.
- Artwork images are prefetched at the end of a language change (today's behaviour); the finalization step is enabled for it or the prefetch is kept as a dedicated last step.
- **Sync progress**: each step declares its units (one per translated item batch or per call, one for Genres, one per Logo lookup) and completes them as today; the language change therefore gets a moving, monotonic percentage with no change to the progress calculation.
- Custom images are kept (same rule as the Light sync) and the Manual identification is untouched; Progress, Status and History are not read or written.
- A language change started while another Sync runs cancels it and takes over; a Light sync requested during a running Full sync is still ignored (existing rule). The new kind of Sync follows the coordinator's existing "latest launch wins" rule.
- `UpdateLanguageUseCase` is deleted (deletion test: it re-implements what the pipeline owns). The Koin wiring moves with it: the translation step is registered with the other steps and added to the language-change step list.
- No ADR is affected (ADR-0001 concerns Manual identification, which stays untouched). Add to GLOSSARY.md when implemented: **Language change** (Sync kind) and **Translation** (Enrichment-like Sync step), and decide whether "Light sync / Full sync / Language change" are the three Sync kinds.

## Testing Decisions

- A good test drives the public Sync entry through the existing Sync harness with the fake TMDB adapter and an in-memory Catalog, then asserts what the user would observe: titles, descriptions, Genres, Logos, Sync state and progress, untouched Progress/Status/History. It never asserts which step ran or how many batches were made.
- The one seam is the existing one (Sync harness + FakeTmdb). No new seam. The second adapter (FakeTmdb vs the real TMDB client) already justifies it.
- Cases: all four kinds of item translated; blank or missing translation keeps the old text; one failing item does not stop the others; Genres refreshed; every Artwork's Logo searched again, failed lookup keeps the previous Logo; no TMDB token means no lookups and no errors; Unknown Artwork untouched; Private Artwork translated; Custom images kept; Manual identification kept; Progress/Status/History unchanged; no Missing file removed; Sync progress moves, is monotonic, ends at completion; Sync state returns to idle; language change cancels a running Light sync; repeated language change is idempotent; Logo is not overwritten by a stale translated Artwork row.
- Settings view model test keeps only: after saving the language, a language change is requested (it uses a mock of the Sync entry as today).
- Prior art: `SyncHarness` and the Sync behaviour tests (catalog, Manual identification, Sync Logo, Custom image), `FakeTmdb`, `LanguageChangeLogoBehaviourTest` (its cases move into the harness style), and `UpdateLanguageUseCaseTest` (its cases are folded in, then the file is removed).
- All existing tests live in the Android module's unit tests, including those of shared code; new tests follow that convention.

## Out of Scope

- Changing which fields are translated (still title and description) or adding the Cast/Crew or Season summaries to the language change.
- The Image language of Gallery images (independent of the data language per GLOSSARY.md).
- UI changes to the settings screen or the sync progress display beyond what existing Syncs already show.
- Changes to the Light sync and Full sync behaviour, other than the Logos step gaining a mode and the pipeline allowing Syncs without a scan.
- Caching translations or a translation table.
- The French-vocabulary KDoc fix is included only for the files this change touches; a global KDoc clean-up is separate.

## Further Notes

- Evidence from the architecture review: the language update re-implements Logo and Genre handling, calls the coordinator directly without Sync progress, and sits outside the pipeline that GLOSSARY.md says owns Enrichments.
- Behaviour difference to preserve on purpose: the language change refreshes Logos for every Artwork, the Light sync only for Artwork never searched; this is why the Logos step needs a mode rather than reuse as is.
- Risk: the Sync context uses mutable state set by earlier steps (scan, known files, files to fetch); steps enabled for a language change must not depend on it.
- Related follow-up candidates from the same review: one Playback launch module (spec in `.scratch/playback-launch/`), Status/History invariants in one place.
