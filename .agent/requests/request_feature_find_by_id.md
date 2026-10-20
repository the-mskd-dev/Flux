# 1. Feature Goal & User Journey
- **Goal**: Retrieve metadata from TMDB ID, in alternative of the name
- **UI Behaviors**: 
  - The user names a file with a TMDB ID within it
  - The TMDB ID can also be in the folder name, not only in the file name
  - `UserFile.nameProperties` retrieve the TDMB ID , thanks to `FileProperties.extractFromName` or `FileProperties.extractFromPath`, and fill `FileProperties` with a new field `tmdbId`
  - In `SyncCatalogUseCase`, Artwork (and associated Media) are fetched in priority by `tmdbId`, and then by name if there is no `tmdbId`

# 2. Data & Sources (TMDB / Local Storage)
- **TMDB API**: API Call
- **Data Strategy**:
    - New optional fields (e.g., `tmdbId`) in `FileProperties`
    - The TMDB ID can be in the folder name or in the file name
    - The TMDB ID, in a file name or in a folder name, must always be written `{tmdb-NUMERO_ID}` (e.g. `Dune {tmdb-438631} (2021).mkv`, `Game of Thrones {tmdb-1399}/S01E02.mkv`) to avoid to get a false ID
    - Any other number (bare number, year, season folder...) is never a TMDB ID, e.g. in `show name/1/02.mkv` the season number `1` must not be used as a TMDB ID

# 4. Acceptance Criteria (Definition of Done)
- **Scenarios to Test with Turbine / Kotest**:
    - "Verify that `SyncCatalogUseCase` prioritizes the TMDB ID if it exists, before the file name"
    - "Verify that a season number in a folder name is not used as a TMDB ID"
- **KMP Isolation**: Confirm that data and domain logic remain 100% inside `commonMain` with zero `android.*` imports.