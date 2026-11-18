# Hygiene: shallow pass-throughs, KMP rule drift and settings reads

## Problem Statement

A few small things make the shared code harder to read and break the project's own rules, without any user-visible bug today:

- Two use cases do nothing but forward a call to a repository (list the user's folders as a flow; delete a folder). Readers must open them to learn that nothing happens inside.
- The KMP isolation rule says shared code must be platform-neutral, yet two data implementations in `commonMain` use JVM-only types: one catches `java.io.IOException` for its settings store, and one computes the Private Folder PIN hash with `java.security.MessageDigest`. They compile today because only Android is built, but the shared module also declares iOS targets, so the code is one step from not compiling there.
- The settings store's interface has plain "read one value" helpers (is the external player on; what is the data language) next to its flow, and some callers read `flow.first()` themselves instead. Two ways to read the same value.
- Several interfaces that are real seams (one implementation per platform) carry a `UseCase` suffix, which hides that they are adapters (for example, the PiP check, the chapters reader, the device files reader).
- A comment in the player's DI module explains that a holder singleton exists to work around a lifetime mismatch that once caused a crash. The workaround is documented only in a comment.

As the maintainer I want these cleaned so the rules the project states are the rules the code follows.

## Solution

Four small, independent clean-ups, each safe to ship alone:

1. Delete the two pass-through use cases; callers use the repository interface directly.
2. Make the two `commonMain` data stores platform-neutral.
3. Give the settings store one way to read a value, and remove the redundant helpers.
4. Rename the adapter-shaped `*UseCase` interfaces so their names say what they are.

Nothing changes for the user.

## User Stories

1. As a maintainer, I want the sources view model, the Artwork observer and the history reader to call the sources repository directly, so that no one-line forwarding use case remains.
2. As a maintainer, I want folder deletion to call the sources repository directly from the sources view model, so that there is no one-line forwarding use case.
3. As a maintainer, I want the deletion test to hold for every remaining use case (each one holds a rule), so that use cases are never mere forwards.
4. As a maintainer, I want the customization store not to import `java.io.IOException`, so that it compiles for every shared target.
5. As a maintainer, I want the Private Folder PIN hash computed without a JVM-only class, so that the store compiles for every shared target.
6. As a viewer, I want my Private Folder PIN to keep working after the change, so that I am not locked out.
7. As a viewer, I want PINs created before the change to still verify, so that no migration is needed.
8. As a maintainer, I want the hashing and the catching of storage errors tested, so that the change is safe.
9. As a maintainer, I want one way to read a settings value, so that I do not wonder which one to use.
10. As a maintainer, I want the settings store's "is the external player on" and "data language" helpers removed (or the flow reads removed in favour of them, whichever is chosen), so that there is no duplicate.
11. As a maintainer, I want every reader of the data language to get the same value in the same format, so that Gallery, the API and the language update agree.
12. As a maintainer, I want adapter-shaped interfaces to be named for their role (for example a PiP availability adapter, a chapters reader, a device files reader, a subtitles reader), so that I see the seam in the name.
13. As a maintainer, I want the PiP interface not to live in a `data` package while named a use case, so that package and name agree.
14. As a maintainer, I want the Koin module check to keep passing after the renames, so that wiring is verified.
15. As a maintainer, I want the unit test suite to pass unchanged in behaviour after each clean-up, so that each step is safely mergeable.
16. As a maintainer, I want the lifetime-mismatch workaround for the decoder manager written down as a decision (why a holder exists), so that no one removes it by accident.
17. As a maintainer, I want a check (test or build rule) that fails when a `java.*` import enters `commonMain`, so that the drift cannot come back.

## Implementation Decisions

- **Pass-through use cases.** Delete the "flow sources" use case and the "delete source" use case. Their callers take the sources repository interface instead. Both pass the deletion test: deleting them moves no logic. The other use cases flagged in the review (genre sync, gallery images) hold a small rule and are kept.
- **KMP rule drift.**
  - The customization store replaces the JVM IOException with the multiplatform datastore IOException (the Private Folder store already uses it).
  - The Private Folder PIN hash needs SHA-256, which is not available in common Kotlin without a dependency. Two options for the implementer, to be chosen with the owner: (a) a small hashing interface in the domain, with an Android adapter (JVM `MessageDigest`) and later an iOS adapter, or (b) a multiplatform hashing library. The hash output (salted SHA-256, lowercase hex) must stay byte-for-byte identical so existing PINs keep verifying. Default: option (a), which needs no new dependency and keeps the existing Android code.
- **Settings reads.** Keep the settings flow as the single read path; remove the two read helpers on the settings store interface and update their callers (the data-language readers and the external-player check; the latter disappears into Playback launch). Where a value must be read once, callers use the flow's first value as the code already does in most places. If a derived read is needed more than twice, add one named accessor on a module that owns the concept, not on the store.
- **Naming.** Rename interfaces that are adapters but carry `UseCase`: the PiP availability check (also moved out of `data` or renamed, coordinated with the PiP spec), the chapters reader, the device files reader, the subtitles reader, the sources scan, "save token and sync", and the Artwork observer interface. Implementations keep a matching prefix (Android…). Use cases that hold logic keep their `UseCase` name.
- **Guard against drift.** Add a check that fails when a `java.*` or `javax.*` import appears in `commonMain` source. It can be a lint-style test over source files or a Gradle check. The existing `android.*` rule already holds.
- **Decoder manager holder.** No code change in this spec; record the reason for the holder (service-scoped decoder manager published into a singleton manager; a past crash) as an ADR in `docs/adr/`, so it is not removed or "cleaned up" by mistake.
- **Koin**: the removed use cases are unregistered; renamed interfaces keep their registration. The existing Koin module verification test must keep passing and covers the wiring change.
- No schema or user-visible change. No existing ADR is affected (ADR-0001 concerns Manual identification).
- Do these as four separate small changes, in any order.

## Testing Decisions

- A good test checks external behaviour: the PIN still verifies for a PIN hashed before the change; a storage read error yields default preferences; a repository call is reachable from the view model. Tests never assert that a deleted class is absent.
- Modules tested: the Private Folder store (PIN set, verify, wrong PIN, pre-change hash fixture), the customization store (IOException yields defaults), the sources view model (folder flow and delete through the repository fake), the settings readers.
- The "no `java.*` in `commonMain`" check is itself a test and runs in the unit test task.
- Koin: `KoinModulesTest` already verifies the module graph; it must stay green and is the safety net for renames and removed registrations.
- Prior art: `SettingsDataStoreImplTest` (store tests with a test datastore), `SourcesViewModelTest`, `KoinModulesTest`.
- All existing tests live in the Android module's unit tests, including those of shared code; new tests follow that convention.

## Out of Scope

- Pass-through use cases other than the two named (a case-by-case review can follow).
- Replacing Koin, restructuring modules, or moving the decoder manager out of its holder.
- The `PipIsEnabledUseCase` rework itself (see the PiP and track preference spec; only the rename is coordinated here).
- Adding iOS adapters or building for iOS.
- Any change to the Private Folder PIN policy, salt or hash algorithm.
- Fixing the French wording in KDoc (handled with the Language update spec where files overlap).

## Further Notes

- Corrections to the architecture review: `KoinModulesTest` exists (the review wrongly said nothing verifies the Koin graph); only the customization store imports `java.io.IOException` (the Private Folder store already uses the multiplatform one), and the "flow sources" use case has three callers (sources view model, Artwork observer, history reader), all of which can take the repository instead.
- Evidence: the data-language helper has four callers; the external-player helper has one (Playback launch removes it); about seven places read `flow.first()` directly.
- Related specs: `.scratch/pip-and-track-preference/`, `.scratch/playback-launch/`, `.scratch/language-update/`.
