# Spec: Manual identification

## Problem Statement

The Sync identifies each Artwork by searching TMDB for the title and year parsed from the file names. When the name is ambiguous (remake, homonym, US/UK version of a show), the wrong Artwork is chosen. Today, the only way to fix it is to rename the files to add a TMDB ID. Moreover, any new episode added later would again be attached to the wrong Artwork.

## Solution

A **"Select data"** option (icon `ic_search`) in the `FluxPopupMenu` of a Show page and of a Movie page opens a `FluxDialog`. It lists the TMDB search results (`searchShow` for a Show, `searchMovie` for a Movie). Choosing one performs a **Manual identification** (see `GLOSSARY.md`): all the Media of the current Artwork are attached to the chosen Artwork, and new files with the same title follow it during Light syncs.

A **"Reset"** option goes back to automatic identification. The Full sync also clears all Manual identifications.

Architecture decision: `docs/adr/0001-manual-identification-by-file-name-title.md`.

## Rules

### Option availability
- Present on the page of a Show and of a Movie, **absent** for the Unknown Artwork.
- **Hidden** without a TMDB token.
- **Hidden** if at least one file of the Artwork carries a TMDB ID in its name or its folder.
- **Disabled** during a Sync.

### Dialog
- Editable search field, prefilled with the title parsed from the name of the first file (not the TMDB title), search with no year filter.
- Each result: Poster (2:3) on the left with a placeholder if it is missing; title and release year on the right.
- The current Artwork is marked "Current" and cannot be selected.
- States: loading, no result, error with "Retry".
- During the Manual identification, the dialog shows a loading state.

### Manual identification
- All the Media of the current Artwork are re-identified to the chosen Artwork. All the Enrichments are replayed: details, seasons and episodes, credits, durations, Logo.
- The resulting Artwork is marked as manually identified: a boolean on `ArtworkEntity`, with no new table.
- **Kept**: the Status and Progress of each Media (by file path) and the history (migrated to the new Artwork, otherwise lost through the cascade of the `history` FK).
- **Private**: if the source Artwork or the target Artwork is Private, the result is Private.
- **Merge**: if the chosen Artwork already exists in the Catalog, the Media are merged into it. For the history, the most recent entry is kept.
- **No atomicity**:
  - a transient error (network, API, token) produces a Failed file, retried at the next Sync;
  - an episode or a season that **does not exist** in the chosen Artwork produces an Unrecognized file, which goes to the Unknown Artwork.
- The old Artwork, now empty, is deleted.
- The page reloads on the new Artwork by replacing the navigation destination: going back does not lead to the old ID.

### Light sync
- Identification order of a file:
  1. TMDB ID in the file or folder name;
  2. Manual identification: a known Media with the same file-name (title, year) belongs to a manually identified Artwork;
  3. name search.
- If all the files of a manually identified Artwork disappear, the choice is forgotten.

### Full sync
- Clears all Manual identifications and runs the name search again. Status and Progress are kept. The history keeps the existing behaviour (out of scope).

### Identification reset
- "Reset" option in the same menu, **only** on a manually identified Artwork, with the same availability conditions. It asks for a confirmation.
- The marker is cleared and the name search is run again for each file. The Failed file and Unrecognized file rules apply; Status, Progress and history are kept.
- The page reloads on the resulting Artwork. If the files are split across several Artwork, the user goes back to the Catalog.

## User Stories

1. As a user, I want to pick the right Movie from TMDB results when Flux identified the wrong one, so that my Catalog shows the correct data.
2. As a user, I want to pick the right Show from TMDB results, so that all my episodes get the correct titles, seasons and images.
3. As a user, I want to edit the search text, so that I can find the Artwork even when my file is badly named.
4. As a user, I want to see each result's Poster, title and year, so that I can tell homonyms and remakes apart.
5. As a user, I want the current Artwork to be marked in the list, so that I know which one is already selected.
6. As a user, I want my statuses, progress and history to survive the change, so that I lose nothing.
7. As a user, I want a Private Artwork to stay private after the change, so that hidden content never appears in the public Catalog.
8. As a user, I want my episodes to be merged into the chosen Show when it is already in my Catalog, so that I do not get duplicates.
9. As a user, I want episodes that do not exist in the chosen Show to go to the Unknown Artwork, so that they stay visible and playable.
10. As a user, I want the page to show the new Artwork right after my choice, so that I see the result immediately.
11. As a user adding new episodes later, I want them to join the Artwork I chose, so that I do not have to fix them again.
12. As a user who wrote a TMDB ID in a file name, I want that ID to stay authoritative, so that the option is not offered and cannot contradict it.
13. As a user, I want to reset a manual choice, so that I can go back to the automatic identification.
14. As a user launching a Full sync, I want the whole Catalog to be identified again from scratch, while keeping my progress.
15. As a user without a TMDB token, I want the option to be hidden, so that I am not offered something that cannot work.
16. As a user, I want the option to be disabled during a Sync, so that the Sync cannot overwrite my choice.
