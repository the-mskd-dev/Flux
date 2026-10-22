# Project Directives & Architecture (Android / KMP)

## 1. Context
- **Description** : KMP (Android Only) application. This app allows users to retrieve their video files information through the API TMDB, and then play their files within the application.
- **Goals** : Retrieve information, organize files in a modern library, play files and keep a progress history.

## 2. Overview & Tech Stack
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

## 3. Directory & Package Structure
- Shared logic inside `shared/src/commonMain/kotlin/`:
  - `data/`: DTOs (`remote/dto`), Repository implementations, Data Store implementations, Data Sources.
  - `domain/`: Domain models, Repository interfaces, Use Cases.
  - `presentation/`: MVI Contracts, ViewModels.
  - `di/`: Koin modules.

## 4. Code Conventions & MVI Architecture

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

## 5. Agent Execution Rules (STRICT)

### When acting as an ARCHITECT (Planning Phase)
1. **No Code Modification**: Do not modify or create any source code files.
2. **Spec Generation**: Generate a strict Markdown plan (e.g., `.agent/plans/feature_name_plan.md`) containing a checklist (`- [ ]`).
3. **Step Breakdown**: The plan MUST strictly follow this exact sequence:
    - **Step A**: Create/Update the MVI Contract (`Contract.kt`). Define immutable `UiState`, `Intent`, and `Event`.
    - **Step B**: Write the ViewModel Unit Tests (`ViewModelTest.kt`) using Kotest, MockK, and Turbine.
    - **Step C**: Write the implementation (`ViewModel.kt`) to make all tests pass.
    - **Step D**: Declare the dependency in the Koin module (`di/`).

### When acting as an EXECUTOR (Coding Phase)
1. **Spec-Driven Execution**: If a `_plan.md` file is provided, you MUST read it before acting.
2. **Strict Step-by-Step**: Execute ONLY the specific step requested by the user. DO NOT anticipate or execute the next steps.
3. **Update State**: Once a step is fully implemented, check the corresponding box in the `_plan.md` file (`- [x]`) and stop.
4. **Verification**: After generating or modifying code for a step (especially Step C), execute `./gradlew testFossDebugUnitTest` in the terminal and fix any failures before checking the box.
5. **Scope Restriction**: Modify ONLY files related to the current step. NEVER touch Gradle files (`build.gradle.kts`) unless explicitly requested.
6. **Additional actions**: Don't forget to correctly add/remove imports.
7. **Changelog**: At the end, add a single line at the end of `./CHANGELOG.md` to indicate the new feature/fix/update (e.g., `feature: Add new images`).

### Universal Rules
1. **KMP Isolation Rule 🚨**:
    - When implementing or refactoring the Domain layer (UseCases, Models) or Data layer (Repositories, Data Sources), you are **strictly forbidden from using any `android.*` or `androidx.*` imports** (such as `Context`, `Bundle`, `Intent`, or `Log`).
    - You MUST write this code in pure Kotlin as if it were already located in the `commonMain` module. If platform-specific Android APIs are absolutely required, define a pure Kotlin interface in the domain layer and implement it in the Android application module, or use the `expect`/`actual` pattern.

### Context & File scanning
- Always strictly respect the exclusion rules defined in `.aiignore` and `.gitignore`.
- Never read, index, or search inside ignored directories (e.g., `build/`, `.gradle/`).
- Always strictly respect instructions in `.clinerules`.