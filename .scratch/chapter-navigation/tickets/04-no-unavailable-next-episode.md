# 04: Never offer an Unavailable Next Episode

Spec: `.scratch/chapter-navigation/specs/spec.md` (user stories 34, 35, 44)

**What to build:** When the Next Episode (next by season then number in the same Show) is Unavailable, because its UserFolder is disconnected or its permission revoked, Flux behaves as if there were no Next Episode: the Chapter button never offers "Next episode". Flux does not skip ahead to a later available Episode, so the viewer never silently misses one. Movies, the last Episode of a Show and Episodes of the Unknown Artwork keep having no Next Episode.

Demo: disconnect the UserFolder holding S01E02, play S01E01 to its end: no "Next episode" button.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] The Next Episode lookup returns nothing when the next Episode is Unavailable
- [ ] It does not return a later available Episode in that case
- [ ] It still returns the next Episode when it is available
- [ ] Movie, last Episode and Unknown Artwork cases are unchanged
- [ ] The database query is unchanged; the availability check happens in the use case on the domain model
- [ ] Use case tests cover Unavailable next → nothing, available next → returned
- [ ] `./gradlew :app:testFossDebugUnitTest` passes
