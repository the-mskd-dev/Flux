## 1. Overview & Tech Stack

- **Project**: Android / KMP (Kotlin Multiplatform) Application
- **Architecture**: MVI (Model-View-Intent) + Clean Architecture
- **UI**: Jetpack Compose, Material 3 Expressive, Coil
- **Data**: Room, Datastore
- **Dependency Injection**: Koin
- **Networking**: Ktor Client + kotlinx.serialization
- **Async & Reactive**: Coroutines & Flow (StateFlow / SharedFlow)
- **Player**: Media3, ExoPlayer
- **Testing**: Kotest (FunSpec), kotlin.test, MockK, Turbine
- **API**: TMDB

## 2. Directory & Package Structure

- Shared logic inside `shared/src/commonMain/kotlin/`:
  - `data/`: DTOs (`remote/dto`), Repository implementations, Data Store implementations, Data Sources.
  - `domain/`: Domain models, Repository interfaces, Use Cases.
  - `presentation/`: MVI Contracts, ViewModels.
  - `di/`: Koin modules.

## 3. Code Conventions & MVI Architecture

### Data Layer & API (TMDB)

- All network DTOs must be annotated with `@Serializable` using `@SerialName("..._path")` to map TMDB API responses correctly.
- Always map DTOs to Domain models using extension functions (`fun Dto.toDomain()`), in a file named `DtoMapper`.
- Catch network exceptions inside Repositories and return a clean `Result<T>` (never allow raw crashes).

### Presentation Layer (MVI)

Each feature must consist of 3 distinct files:

1. `[Feature]Contract.kt`:
   - `[Feature]UiState`: Strictly **immutable** `data class`.
   - `[Feature]Intent`: `sealed class` or `sealed interface` representing user actions.
   - `[Feature]Event`: `sealed class`  or `sealed interface` for side effects (one-off events like navigation or snackbars).
2. `[Feature]ViewModel.kt`:
   - Exposes a `StateFlow<[Feature]UiState>`.
   - Receives actions exclusively through a single function `handleIntent(intent: [Feature]Intent)`.
3. `[Feature]ViewModelTest.kt`:
   - Systematic unit tests using `Kotest (with FunSpec)`, `MockK` and `Turbine`.

### Universal Rules

1. **KMP Isolation Rule 🚨**:
   - When implementing or refactoring the Domain layer (UseCases, Models) or Data layer (Repositories, Data Sources), you are **strictly forbidden from using any** `android.*` **or** `androidx.*` **imports** (such as `Context`, `Bundle`, `Intent`, or `Log`).
     - You MUST write this code in pure Kotlin as if it were already located in the `commonMain` module. If platform-specific Android APIs are absolutely required, define a pure Kotlin interface in the domain layer and implement it in the Android application module, or use the `expect`/`actual` pattern.

### Context & File scanning

- Always strictly respect the exclusion rules defined in `.aiignore` and `.gitignore`.
- Never read, index, or search inside ignored directories (e.g., `build/`, `.gradle/`).
- Always strictly respect instructions in `.clinerules`.

