# 05: Remove the old playback code

**What to build:** Nothing user-visible. Once every page plays through Playback launch, delete what it replaced so the four copies cannot return.

**Blocked by:** 02, 03, 04, 06

**Status:** done

- [x] `ResolvePlaybackActionUseCase`, `PlaybackAction` and `ResolvePlaybackActionUseCaseTest` deleted (cases already covered by the module test)
- [x] The nullable external-result hook in `ArtworkViewModel` and the unused progress-saving injection and Koin arguments in the page modules deleted; `PlayerViewModel` untouched
- [x] `rememberExternalPlayerAction` and the old external-player utility deleted
- [x] No remaining reference to the removed code; `./gradlew :app:testFossDebugUnitTest` passes
