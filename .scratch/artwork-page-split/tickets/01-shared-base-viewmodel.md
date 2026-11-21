# 01: Artwork and Show pages share one base behaviour

**What to build:** The behaviour the Artwork page and the Show page both implement today (observing the Artwork, identification, reset of progression, external player result handling, and the intent dispatching shape) lives in one open base ViewModel. The existing Artwork and Show ViewModels adopt it and keep only what is specific to their content. This is a prefactor: the user sees no change.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] The shared behaviour is written once; the Artwork and Show ViewModels no longer duplicate it.
- [ ] The public contract (state, intents, events) of both ViewModels is unchanged.
- [ ] Existing Artwork and Show ViewModel tests pass without behavioural edits.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
