# 08: Manual identification is offered for files with a TMDB ID

**What to build:** The user can run a Manual identification on a Movie or Show page even when a file of the Artwork has a TMDB ID in its name. "TMDB ID in the name" covers both places it is read from today: the file name and the folder path. The choice is kept: the identification order becomes **Manual identification, then TMDB ID in the name, then name search**, so a later Light sync does not undo it. A Full sync or an Identification reset clears the Manual identification, and the TMDB ID in the name applies again.

**Blocked by:** None (can start immediately)

**Status:** done

- [x] The Manual identification option is shown, and enabled outside a Sync, on the Movie and Show pages when a file has a TMDB ID in its name. It is still hidden for the Unknown Artwork and when no TMDB token is set.
- [x] Tests of the identification availability rule cover the case of a file with a TMDB ID. (Through the Movie and Show ViewModel tests; the folder path case on the Show page.)
- [x] During a Sync, a file with a TMDB ID in its name whose (title, year) matches a known Media of a manually identified Artwork is attached to that Artwork, without a lookup by TMDB ID. Tests cover this case, the TMDB ID case without a Manual identification, and the name search fallback. (Decided per folder: a folder grouped by TMDB ID whose files have mixed (title, year) follows its TMDB ID, see ADR 0001.)
- [x] After an Identification reset or a Full sync, a file with a TMDB ID in its name gets back the Artwork of that TMDB ID. (Tested through the Full sync: there is no per-Artwork Identification reset action in the code yet.)
- [x] ADR 0001 and `GLOSSARY.md` "Manual identification" state the new priority order and no longer say that a TMDB ID in a file name prevents a Manual identification. The comments in the code that state the old order are updated.
- [x] No database schema change and no data migration.
- [x] `./gradlew :app:testFossDebugUnitTest` passes.
