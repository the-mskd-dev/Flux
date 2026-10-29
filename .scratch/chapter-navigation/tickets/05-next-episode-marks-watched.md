# 05: "Next episode" on the Chapter button marks the current Episode WATCHED

Spec: `.scratch/chapter-navigation/specs/spec.md` (user stories 42, 43)

**What to build:** Tapping "Next episode" on the Chapter button always marks the current Episode WATCHED (Progress reset to 0, history recorded as usual) before launching the Next Episode, whatever the Chapters say. This fixes Episodes whose last Chapter is not an Ending (e.g. "Preview" after the Ending): today, tapping "Next episode" in their last 15 seconds leaves them "en cours".

Moving to the Next Episode becomes one routine in the player, taking whether to force WATCHED, so ticket 06 (MediaNext) can reuse it.

Demo: play an Episode whose Chapters end with "ED" then "Preview", wait for "Next episode" in the last seconds, tap it, go back to the Show: the Episode is WATCHED.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] Saving Progress accepts an explicit "force WATCHED" input, defaulting to not forced
- [ ] When forced, the Media is saved WATCHED with Progress 0, regardless of Chapters or the progress threshold, and the history entry is recorded
- [ ] When not forced, every existing Status rule is unchanged for all callers
- [ ] The Chapter button's "Next episode" tap saves with WATCHED forced, clears the Chapter button and switches to the Next Episode
- [ ] Moving to the Next Episode is a single routine in the player parameterised by "force WATCHED"
- [ ] Save Progress tests cover forced WATCHED (with chapters whose last is not an Ending, and without chapters below threshold) and unchanged unforced behaviour
- [ ] ViewModel "chapter button tap" test asserts the save is forced
- [ ] `./gradlew :app:testFossDebugUnitTest` passes
