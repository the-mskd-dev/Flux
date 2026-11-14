# 02: Custom logo exists and is shown

**What to build:** An Artwork can have a custom Logo (Custom image), stored apart from the automatic Logo. The Artwork page shows the custom Logo if there is one, otherwise the automatic one. It follows the lifecycle of the custom Backdrop and Poster: kept by the Light sync, cleared by the Full sync and the Identification reset, not carried over by a Manual identification. Both Logos are prefetched. Existing databases migrate (version 15 to 16). Not visible to the user until ticket 03. Parent: GitHub issue #420.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] Database migration 15 to 16 adds the custom Logo, existing data preserved
- [ ] Artwork page shows custom Logo, falling back to the automatic one
- [ ] Light sync keeps it; Full sync and Identification reset clear it; Manual identification does not carry it over
- [ ] Prefetch includes the custom Logo in addition to the automatic one
- [ ] Tests: migration 15 to 16, existing Sync tests extended (Light and Full sync)
