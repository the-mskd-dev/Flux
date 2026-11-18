# Picture-in-Picture and track preference

## Problem Statement

While I watch, two behaviours depend on my settings and on what the phone allows:

- **Picture-in-Picture (PiP)**: leaving the app while a Media plays should shrink the video into a floating window, or, when PiP is not available or turned off, pause playback and save my Progress.
- **Track preference**: Flux remembers the audio language, the subtitle language and whether subtitles are on, and applies them to each new Media.

Each of these concepts is spread over several places that each hold part of the truth:

- "Is PiP on?" is answered twice, differently. The activity reads the PiP setting directly when I leave the app. The player's view model asks another module, which reads the setting, then checks that the device supports PiP and that the system allows it for Flux. When the two disagree (setting on, but the system refuses), the activity may ask for PiP while the view model decides to pause and save as if PiP were off.
- The PiP window itself (aspect ratio, entering PiP, watching for the PiP state, closing it) is split between the activity and a screen-level side-effect, which has to downcast to the main activity to register a callback.
- The track preference is applied in two places: the view model selects the preferred tracks when the tracks are known, and the player adapter also applies the same preference when it starts a Media. The preference module mixes one real operation (save the track I picked) with three plain getters that only read settings.
- The player's interface exposes the Media3 player type to the screen through a generic parameter, so an Android type leaks past the seam.

As the maintainer I cannot tell where a PiP or track bug lives, and the player's view model test (1,300+ lines) carries much of the cost.

## Solution

Two decisions, each made in exactly one module, behind the existing **player manager** seam (the interface with the Media3 adapter and a fake):

1. **PiP policy**: one answer to "may this Media go to PiP now?", used by both the moment I leave the app and the pause-and-save decision.
2. **Track preference**: one place that applies my saved preference to a Media (at start and when tracks become known) and saves the track I pick. The Media3 adapter applies it; the view model only asks for it.

For the user nothing changes visibly, except that the two PiP paths can no longer disagree.

## User Stories

1. As a viewer, I want the video to go to Picture-in-Picture when I leave the app while it plays, so that I keep watching.
2. As a viewer, I want PiP to apply only when I turned it on in the settings, so that I stay in control.
3. As a viewer on a device without PiP, I want Flux to behave as if PiP were off, so that nothing fails.
4. As a viewer who revoked PiP permission for Flux in the system settings, I want Flux to behave as if PiP were off, so that the system and Flux agree.
5. As a viewer with PiP off, I want playback to pause and my Progress to be saved when I leave the app, so that I resume where I stopped.
6. As a viewer with PiP off, I want playback to resume when I come back if it was playing before, so that I continue seamlessly.
7. As a viewer, I want the decision to enter PiP and the decision to pause on leaving to always agree, so that I never get a PiP window and a paused video, or a running video that is not in PiP.
8. As a viewer, I want PiP not to start when the Media is paused, so that I do not get a window with a frozen frame.
9. As a viewer, I want the PiP window to keep a 16:9 shape, so that the video looks right.
10. As a viewer, I want the Interface and the overlays hidden in PiP, so that the small window shows only the video.
11. As a viewer, I want Boost speed to end when I enter PiP and not to be available there, so that the window does not stay at 2x.
12. As a viewer, I want closing the PiP window to pause playback and save my Progress, so that dismissing it is safe.
13. As a viewer, I want returning from PiP to the app to restore the normal Interface, so that I can use the controls.
14. As a viewer, I want the system bars not to be touched while I am in PiP, so that the window is not disturbed.
15. As a viewer, I want my saved audio language applied to every new Media, so that I do not choose it again.
16. As a viewer, I want my saved subtitle language applied to every new Media, so that I do not choose it again.
17. As a viewer, I want subtitles to stay off for new Media when I turned them off, so that my choice persists.
18. As a viewer, I want turning subtitles off to keep my saved subtitle language, so that turning them on again restores it.
19. As a viewer, I want picking an audio track to save its language, so that the next Media uses it.
20. As a viewer, I want picking a subtitle track to turn subtitles on and save its language, so that the next Media uses it.
21. As a viewer, I want picking a track with no language to change nothing in my saved preference, so that an unlabelled track does not erase my choice.
22. As a viewer, I want the preferred tracks selected when the Media starts, so that the right audio and subtitles play from the first second.
23. As a viewer, I want the preference re-applied once the Media's tracks are known, so that a Media whose tracks appear late still gets my choice.
24. As a viewer, I want a Media that has no track in my preferred language to fall back to the file's default, so that something always plays.
25. As a viewer, I want the preference applied only once per Media, not twice in a row, so that tracks do not flicker.
26. As a viewer, I want the track choice to survive a configuration change (rotation, folding), so that playback is not disturbed.
27. As a viewer, I want the track sheets to show which tracks are selected, so that I know what plays.
28. As a viewer using an external player, I want none of this to apply, so that the external player keeps its own behaviour.
29. As a maintainer, I want one module to answer "is PiP available now", so that the activity and the view model use the same answer.
30. As a maintainer, I want the PiP window handling (enter, observe state, aspect ratio) to live with the player adapter, so that the screen does not downcast to the main activity.
31. As a maintainer, I want the player's interface not to expose the Media3 player type, so that the view model and the screen are platform-neutral.
32. As a maintainer, I want the track preference applied by one module, so that I cannot fix one place and forget the other.
33. As a maintainer, I want plain "read the setting" getters removed from the preference module, so that its interface is only what it does.
34. As a maintainer, I want the player view model tests to use a simple fake manager and shrink, so that the 1,300-line test becomes readable.
35. As a maintainer, I want the PiP policy and the track preference tested without a device, so that CI covers them.

## Implementation Decisions

- The **player manager** interface is the one seam for both concepts. It has two adapters today (the Media3 adapter and a fake in tests), so the seam is real. It grows to own: the PiP policy (whether PiP is currently available and applicable) and the application of the saved track preference.
- **PiP policy** becomes one decision: PiP is available only when the setting is on, the device supports PiP, and the system allows it for Flux. The app's activity and the player's pause-and-save decision both use this answer, so the activity no longer reads the raw setting. The decision is exposed as a plain boolean (or small state) through the player manager seam; the Android permission and system-feature checks stay in the Android adapter.
- The one-method "PiP is enabled" interface and its Android implementation are absorbed into the player manager adapter (deletion test: the interface was as small as its implementation and its answer was needed in three places).
- The **PiP window mechanics** (entering PiP when playing and on user leave, aspect ratio, observing PiP state changes) move behind the Android adapter and a small platform hook, so the screen's side-effects no longer reach into the main activity by downcast. The view model keeps only the user-visible rules: end Boost speed on entering PiP, pause and save when PiP closes, and the Interface state in PiP.
- **Track preference** is applied by the player adapter (at Media start, with the saved audio and subtitle languages and the subtitles-enabled flag, and again when tracks are known). The view model no longer selects preferred tracks itself; it forwards the user's track pick and the adapter saves it through the preference module.
- The preference module keeps its one real operation (save the track I picked, with the existing rules: "no subtitles" turns them off but keeps the language; a track with no language saves nothing; a subtitle pick turns subtitles on). Its three read-only getters are removed; readers use the settings store directly.
- The player manager interface stops exposing the platform player type to the view model and the screen: the screen obtains the surface it needs through a platform-neutral handle or a screen-level adapter. This is the largest change in the spec and may be split into its own ticket.
- Dependency lifetime: the "decoder manager holder" singleton that links the service-scoped decoder manager to the singleton player manager is kept; changing it is out of scope.
- Settings: the PiP setting keeps its name and default (on).
- No ADR is affected. The earlier spec for remembering "subtitles off" (`.scratch/remember-subtitles-off`) defines the saved-preference rules that this spec preserves.
- Vocabulary: use Interface, Seek overlay, Ambient overlay, Boost speed, Track, Settings sheet as in GLOSSARY.md. Add **Picture-in-Picture** to GLOSSARY.md (the glossary mentions it only inside Boost speed), with the rule that it is available only when the setting, the device and the system all allow it.

## Testing Decisions

- A good test crosses the player manager seam and asserts what the viewer observes: which tracks are selected, what is saved as the preference, whether the player is paused and the Progress saved on leaving, whether PiP is entered, the Interface state in PiP. It never asserts which setting reads happened.
- Tested modules: the player view model (through its intents, with the fake player manager), and the Android adapter's PiP policy and track-preference application (JVM tests with fakes for the system-feature check, the permission check, the settings store and the Media3 track-selection parameters where feasible).
- Cases for PiP policy: setting off, device unsupported, permission refused, all allowed; leave-app with PiP available (no pause), leave-app without (pause, save, resume on return if it was playing); close PiP (pause and save); Boost speed ended on entering PiP.
- Cases for track preference: saved audio/subtitle language applied at start; subtitles disabled applied; no preferred language falls back; picking audio, subtitles, "no subtitles", a track without language; saved once per Media.
- Prior art: `PlayerViewModelTest` (fake manager and Turbine), `AndroidPlayerManagerTest`, `SaveTrackLanguageUseCase` tests if present, and the settings store test.
- The player view model test is expected to shrink: tests that only re-prove the duplicated track selection and the PiP-enabled check are removed.
- All existing tests live in the Android module's unit tests, including those of shared code; new tests follow that convention.

## Out of Scope

- Changes to PiP actions (play/pause buttons in the window), custom aspect ratios, or auto-PiP on gesture navigation.
- The external player and the Playback launch spec.
- Chapter buttons, Playback speed, subtitle delay, Media info.
- The decoder recovery ladder and the decoder manager holder.
- The Settings screen layout.
- Moving the player screen to shared (it stays Android: it renders Media3).

## Further Notes

- Evidence from the architecture review: the activity reads `pipIsEnabled` straight from the settings while the view model's module also checks the system feature and the AppOps permission; the same preferred-language calls appear in the view model's track update and in the adapter's start of a Media; the preference module's getters are plain pass-throughs to the settings flow.
- The generic parameter on the view model and the player manager is the root of the leak; if it proves too large for this change, ship PiP and track preference first and the generic removal as a follow-up ticket.
- Related specs: `.scratch/playback-launch/`, `.scratch/progress-rules/`, `.scratch/remember-subtitles-off/`.
