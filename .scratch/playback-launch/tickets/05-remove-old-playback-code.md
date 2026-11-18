# 05: Remove the old playback code

**What to build:** Nothing user-visible. Once every page plays through Playback launch, delete what it replaced so the four copies cannot return.

**Blocked by:** 02, 03, 04

**Status:** ready-for-agent

- [ ] `ResolvePlaybackActionUseCase`, `PlaybackAction` and `ResolvePlaybackActionUseCaseTest` deleted (cases already covered by the module test)
- [ ] The nullable external-result hook in `ArtworkViewModel` and the unused progress-saving injection and Koin arguments in the page modules deleted; `PlayerViewModel` untouched
- [ ] `rememberExternalPlayerAction` and the old external-player utility deleted
- [ ] No remaining reference to the removed code; `./gradlew :app:testFossDebugUnitTest` passes
