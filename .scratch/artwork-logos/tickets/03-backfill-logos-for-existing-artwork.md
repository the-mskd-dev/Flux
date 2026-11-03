# 03: Existing Artwork get their Logo (Step "Logos")

**What to build:** A normal (light) Sync gives their **Logo** to the Artwork already in the Catalog, without a Full sync. A new Sync step "Logos" runs in every Sync, after the Catalog step and before the Finalization step. It processes every Artwork whose Logo path is null (never searched), except the Unknown Artwork, with one standalone images call per Artwork, using the shared Logo choice from ticket 01, and persists each result immediately.

An Artwork with no Logo on TMDB is stored as "none" (empty) and is never searched again. A lookup failure (network, API, missing token) leaves it null so the next Sync retries it; it is never counted as a Failed file and never stops the Sync.

This ticket introduces the "fetch the Logo of one Artwork" lookup, which the language change (ticket 05) reuses.

**Blocked by:** 01: A new Artwork gets its Logo during the Sync

**Status:** done

- [ ] A reusable lookup fetches the images of one Artwork and returns the chosen Logo, "none", or a failure; a failure is distinguished from "none".
- [ ] The Step runs in light and complete Syncs, after the Catalog step and before the Finalization step.
- [ ] It processes Artwork with a null Logo path, excluding the Unknown Artwork, and persists each Logo path as soon as it is known (work survives an interruption).
- [ ] "No Logo" is stored as empty and not searched again by the next Sync.
- [ ] A network/API failure leaves the Logo path null and the Artwork is retried at the next Sync; a missing token makes the Step do nothing.
- [ ] A failure on one Artwork does not stop the Sync or the other Artwork, and is not reported as a Failed file.
- [ ] The Step declares its units from the number of Artwork to process; the Sync progress never goes backwards and reaches 100 % only at the end.
- [ ] It relies on the existing TMDB client call limit; it adds no limit of its own.
- [ ] Private Artwork are processed like the others.
- [ ] Sync behaviour tests cover: an old Artwork gets its Logo in a light Sync; empty is not retried; failure then retry; missing token; Unknown Artwork never searched; one failure does not affect the others; progression.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
