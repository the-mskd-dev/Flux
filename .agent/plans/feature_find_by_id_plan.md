# Plan — Feature: Find artwork by TMDB ID

> Source request: `.agent/requests/request_feature_find_by_id.md`
> Branch: `feature-search-by-id`

## 0. Architect notes (read before executing)

### Scope adaptation of the A → D sequence
This feature has **no UI / no ViewModel**: it lives entirely in the Domain + Data layers of the sync pipeline.
The mandatory A → D sequence is kept, mapped as follows:

| Step | Standard meaning (AGENT.md) | Meaning for this feature |
|------|-----------------------------|--------------------------|
| A | MVI Contract | **Domain contracts**: `FileProperties.tmdbId`, `CatalogFolder.tmdbId`, `ApiRepository.getArtworkById` signature (no logic) |
| B | ViewModel tests | **Unit tests** (Kotest FunSpec + MockK): parsing (`UserFileTest`), grouping, `ArtworkMetadataFetcherImplTest`, `SyncCatalogUseCaseTest` |
| C | ViewModel impl | **Implementation** of parsing, grouping, repository/data source and fetcher priority — make Step B green |
| D | Koin declaration | **Koin check**: no new class to declare; verify graph only |

No change to `CatalogContract.kt` / `CatalogViewModel.kt`.

### Current flow (as-is)
```
SyncCatalogUseCase
 └─ newFiles.groupInFolders()               // groups by nameProperties.(title, year)
 └─ CatalogContentFetcher.fetch(folders)
     └─ ArtworkMetadataFetcherImpl.fetch     // api.getArtwork(file = folder.files.first())
         └─ ApiRepositoryImpl.getArtwork     // tmdb.getArtwork(file)?.toDomain() ?: Artwork.UNKNOWN
             └─ TmdbDataSourceImpl.getArtwork // searchShow / searchMovie by title + year
```

### Target flow
```
ArtworkMetadataFetcherImpl.fetch
   folder.tmdbId != null ?
     ├─ yes → api.getArtworkById(id, type)   // details endpoint (getMovie / getShow)
     │         └─ null / UNKNOWN → fallback api.getArtwork(file)   (search by name)
     └─ no  → api.getArtwork(file)           (unchanged)
```

### Design decisions
1. **Where the priority lives**: in `ArtworkMetadataFetcherImpl` (domain, pure Kotlin, already injected with `ApiRepository`). `SyncCatalogUseCase` delegates artwork fetching to `CatalogContentFetcher` → `ArtworkMetadataFetcher`, so this is the only place where "ID first, then name" can be expressed without duplicating the pipeline. `SyncCatalogUseCase` only needs its step counting adapted.
2. **Content type of an ID** (TMDB movie ID ≠ TV ID): inferred from the existing rule — `file.isEpisode` → `ContentType.SHOW`, else `ContentType.MOVIE` (same rule as `TmdbDataSourceImpl.getArtwork`).
3. **No new endpoint**: reuse `TmdbDataSource.getMovie(artworkId)` / `getShow(artworkId)` (they already handle token check, language and translation fallback). Only new mappers `MovieDto.toArtwork()` / `ShowDto.toArtwork()` are needed.
4. **No persistence change**: `Artwork.id` is already the TMDB ID → no Room migration, no new table.
5. **Parsing rules** (request + requester decision of 2026-10-20):
   - **Single notation everywhere**: a TMDB ID is **always** written `{tmdb-<digits>}`, in a file name **and** in a folder name (e.g. `Dune {tmdb-438631} (2021).mkv`, `Game of Thrones {tmdb-1399}/S01E02.mkv`). Regex: `\{tmdb-(\d+)\}` (case-insensitive on `tmdb`).
   - The token may appear **anywhere** in the name; it is removed before title/year/season/episode extraction, so it never pollutes the title.
   - Any other number (bare, `<…>`, year, season folder) is **never** a TMDB ID. This removes the former ambiguities (4-digit folder vs year, 1–2 digit folder vs season, `1399/02.mkv`).
   - **Folder lookup**: the candidate folder is the *title folder*, i.e. the parent folder, **unless** the parent is a season folder (matches `SEASON_ONLY_PATTERNS`, e.g. `1`, `s1`, `season 1`) → then the grandparent.
   - **Priority**: ID in file name > ID in folder name.
   - `nameProperties` must still pick up a folder ID when `extractFromName` succeeds (today `extractFromPath` is skipped in that case).
6. **Grouping**: files sharing a `tmdbId` must end in the same `CatalogFolder` even if their parsed titles differ (e.g. `GoT {tmdb-1399}/…` and `Game of Thrones {tmdb-1399}/…`). Group key becomes `tmdbId` when present, else `(title, year)`.

### KMP isolation
All touched files are under `shared/src/commonMain/` — **zero `android.*` / `androidx.*` imports allowed**. Tests live in `app/src/test/` (existing convention).

---

## Step A — Domain contracts (models & interfaces, no logic)

- [x] **A.1** `shared/src/commonMain/kotlin/com/mskd/flux/core/model/files/UserFile.kt`
  - Add `val tmdbId: Long? = null` as the last parameter of `FileProperties` (default keeps all existing call sites compiling).
- [x] **A.2** `shared/src/commonMain/kotlin/com/mskd/flux/core/model/catalog/CatalogFolder.kt`
  - Add `val tmdbId: Long? = null` to `CatalogFolder`.
- [x] **A.3** `shared/src/commonMain/kotlin/com/mskd/flux/core/network/tmdb/domain/repository/ApiRepository.kt`
  - Declare `suspend fun getArtworkById(id: Long, type: ContentType): Artwork?`
  - In `ApiRepositoryImpl`, add a temporary stub `= null` (`// TODO Step C`) so the project compiles for Step B.
- [x] **A.4** `./gradlew test` compiles and existing tests are still green. *(run with `./gradlew :app:testFossDebugUnitTest`: the `playstore` flavor needs a missing `google-services.json`)*

## Step B — Unit tests (Kotest FunSpec + MockK, Turbine where flows are involved)

- [x] **B.1** `app/src/test/java/com/mskd/flux/model/UserFileTest.kt` (+ `UserFileTestCases.kt`: add `expectedTmdbId: Long? = null`)
  - ID in file name, movie: `Dune {tmdb-438631} (2021).mkv` → `tmdbId = 438631`, `title = "dune"`, `year = 2021`.
  - ID at the start / end of the name: `{tmdb-438631} Dune.mkv`, `Dune (2021) {tmdb-438631}.mkv` → `tmdbId = 438631`, `title = "dune"`.
  - ID in file name, episode: `Game of Thrones {tmdb-1399} S01E02.mkv` → `tmdbId = 1399`, season 1, episode 2, title without the token.
  - Case-insensitive: `Dune {TMDB-438631}.mkv` → `tmdbId = 438631`.
  - Other notations are **not** an ID: `Movie 438631.mkv`, `Movie <438631>.mkv`, `Movie {438631}.mkv`, `Movie {tmdb-}.mkv` → `tmdbId = null`.
  - ID in folder name, movie: `/movies/Dune {tmdb-438631}/Dune.mkv` → `tmdbId = 438631`, title `dune`.
  - Folder made only of the token: `/movies/{tmdb-438631}/Dune.mkv` → `tmdbId = 438631`.
  - ID in folder, show with season folder: `/shows/Game of Thrones {tmdb-1399}/1/02.mkv` → `tmdbId = 1399`, season 1, episode 2, title `game of thrones`.
  - ID in folder, `SxxEyy` file name: `/shows/Game of Thrones {tmdb-1399}/S01E02.mkv` → `tmdbId = 1399`.
  - **Season guard**: `/shows/show name/1/02.mkv` → `tmdbId = null`, season 1, episode 2 *(acceptance criterion)*.
  - **Season guard**: `/shows/show name/2/S02E05.mkv` → `tmdbId = null`.
  - Bare numeric folders are not IDs: `/movies/2001/Film.mkv`, `/shows/1399/1/02.mkv` → `tmdbId = null`.
  - Priority: `/shows/Show {tmdb-1399}/Other {tmdb-42} S01E01.mkv` → `tmdbId = 42`.
  - Non-regression: all existing `UserFileTest` cases still pass with `tmdbId = null`.
- [x] **B.2** Grouping test — new `app/src/test/java/com/mskd/flux/utils/GroupInFoldersTest.kt`
  - Two files with the same `tmdbId` but different parsed titles → one `CatalogFolder` with `tmdbId` set.
  - Files without `tmdbId` → grouping by `(title, year)` unchanged.
- [x] **B.3** New `app/src/test/java/com/mskd/flux/features/catalog/fetcher/ArtworkMetadataFetcherImplTest.kt` (MockK `ApiRepository`, `StandardTestDispatcher`)
  - **"prioritizes the TMDB ID if it exists, before the file name"** *(acceptance criterion)*: folder with `tmdbId` → `coVerify { api.getArtworkById(id, MOVIE) }` and `coVerify(exactly = 0) { api.getArtwork(any()) }`.
  - Episode folder with `tmdbId` → `getArtworkById(id, ContentType.SHOW)`.
  - `getArtworkById` returns `null` → falls back to `api.getArtwork(file)`.
  - `getArtworkById` throws → falls back to `api.getArtwork(file)`; `onProgress` called exactly once per folder.
  - Folder without `tmdbId` → only `api.getArtwork(file)` is called.
- [x] **B.4** `app/src/test/java/com/mskd/flux/features/catalog/usecase/SyncCatalogUseCaseTest.kt`
  - Device files whose names carry `{tmdb-<id>}` → captured `folders` passed to `CatalogContentFetcher.fetch` contain the expected `tmdbId` (use `slot<List<CatalogFolder>>()`); state observed with Turbine reaches completion.
  - Two files with the same ID in differently named folders → a single folder passed to the fetcher.
- [x] **B.5** Run `./gradlew :app:testFossDebugUnitTest` (playstore flavor needs a missing `google-services.json`): new tests compile and **fail** for the expected reasons (red phase).

## Step C — Implementation (make Step B green)

- [x] **C.1** `FileProperties` parsing (`UserFile.kt`, companion object)
  - Add a single `TMDB_ID_PATTERN = Regex("\\{tmdb-(\\d+)\\}", RegexOption.IGNORE_CASE)` plus two private helpers: `findTmdbId(text): Long?` and `removeTmdbId(text): String` (removes the token and collapses spaces).
  - `extractFromName`: read the ID with `findTmdbId`, run the existing logic on `removeTmdbId(nameWithoutExt)`, set `tmdbId`. A name that is **only** the token (`{tmdb-438631}.mkv`) returns `null` (no title) so path extraction takes over, but its ID must still be kept (see C.2).
  - Add `fun extractTmdbIdFromPath(path: String): Long?`: take the parent folder; if `findSeason(parent) != null` use the grandparent instead; return `findTmdbId(segment)`.
  - `extractFromPath`: apply `removeTmdbId` to every segment used as a title (parent / grandparent) and set `tmdbId` via `extractTmdbIdFromPath`. A folder made only of the token yields a blank title → fall back to the file name as title.
- [x] **C.2** `UserFile.nameProperties`: final `tmdbId` = ID found in `name` ?: `extractTmdbIdFromPath(realPath)`, whichever branch (`extractFromName` / `extractFromPath` / fallback) produced the title.
- [x] **C.3** `utils/extensions` — `List<UserFile>.groupInFolders()`: group by `tmdbId` when present, else `(title, year)`; propagate `tmdbId` into `CatalogFolder`.
- [x] **C.4** Data layer — mappers in `core/network/tmdb/data/mapper/ArtworkApiMapper.kt`
  - `fun MovieDto.toArtwork(): Artwork` (type `MOVIE`, `genreIds = genres.map { it.id }`).
  - `fun ShowDto.toArtwork(): Artwork` (type `SHOW`).
  - Optional: add `@SerialName("adult") val nsfw: Boolean = false` to `MovieDto` / `ShowDto` to keep NSFW detection working for ID-based artworks.
- [x] **C.5** `ApiRepositoryImpl.getArtworkById(id, type)`: `MOVIE → tmdb.getMovie(id)?.toArtwork()`, `SHOW → tmdb.getShow(id)?.toArtwork()`; exceptions already caught in data source → returns `null` (never crashes).
- [x] **C.6** `ArtworkMetadataFetcherImpl.fetch`: per folder,
  `folder.tmdbId?.let { runCatching { api.getArtworkById(it, type) }.getOrNull() }?.takeUnless { it.isUnknown } ?: api.getArtwork(file)` with `type = if (file.isEpisode) SHOW else MOVIE`; trace which strategy was used.
- [x] **C.7** `SyncCatalogUseCase`: season step count → `distinctBy { (it.nameProperties.tmdbId ?: it.nameProperties.title) to it.season }` so progress stays consistent with the new grouping.
- [x] **C.8** Check no `android.*` / `androidx.*` import was added in `shared/src/commonMain` (`grep -rn "import android" shared/src/commonMain`; `androidx.compose.*` in `ui/` pre-exists, no new `android*` import in the diff).
- [x] **C.9** `./gradlew :app:testFossDebugUnitTest` fully green (playstore flavor needs a missing `google-services.json`) (fix failures before checking).

## Step D — Koin (`di/`)

- [x] **D.1** `features/catalog/moduleCatalog.kt` and `core/network/tmdb/data/moduleTmdb.kt`: **no new binding expected** (`ArtworkMetadataFetcherImpl` and `ApiRepositoryImpl` constructors unchanged). Confirm the constructors were not modified; if they were, update the `single { … }` declarations accordingly.
- [x] **D.2** `./gradlew :app:testFossDebugUnitTest` green (no Koin module check exists in the project; playstore flavor needs a missing `google-services.json`).

---

## Acceptance criteria mapping

| Criterion (request) | Covered by |
|---------------------|------------|
| Sync prioritizes TMDB ID before file name | B.3 (+ B.4 for propagation through `SyncCatalogUseCase`) |
| Season number in folder name is not used as TMDB ID | B.1 season-guard + bare numeric folder cases |
| 100 % `commonMain`, zero `android.*` | C.8 |

## Resolved questions
Decision of 2026-10-20: the single notation `{tmdb-<digits>}` (file **and** folder) resolves the former open questions — a bare number (`2001/`, `12/`, `1399/`) is never a TMDB ID, so there is no conflict with years or season folders.
