# 03: Existing Artwork get their Logo (Étape "Logos")

**What to build:** A normal (light) Synchro gives their **Logo** to the Artwork already in the Catalogue, without a Synchro complète. A new Étape de synchro "Logos" runs in every Synchro, after the Étape catalogue and before the Finalisation. It processes every Artwork whose Logo path is null (never searched), except the Unknown Artwork, with one standalone images call per Artwork, using the shared Logo choice from ticket 01, and persists each result immediately.

An Artwork with no Logo on TMDB is stored as "none" (empty) and is never searched again. A lookup failure (network, API, missing token) leaves it null so the next Synchro retries it; it is never counted as a Fichier en échec and never stops the Synchro.

This ticket introduces the "fetch the Logo of one Artwork" lookup, which the language change (ticket 05) reuses.

**Blocked by:** 01: A new Artwork gets its Logo during the Synchro

**Status:** ready-for-agent

- [ ] A reusable lookup fetches the images of one Artwork and returns the chosen Logo, "none", or a failure; a failure is distinguished from "none".
- [ ] The Étape runs in light and complete Synchros, after the Étape catalogue and before the Finalisation.
- [ ] It processes Artwork with a null Logo path, excluding the Unknown Artwork, and persists each Logo path as soon as it is known (work survives an interruption).
- [ ] "No Logo" is stored as empty and not searched again by the next Synchro.
- [ ] A network/API failure leaves the Logo path null and the Artwork is retried at the next Synchro; a missing token makes the Étape do nothing.
- [ ] A failure on one Artwork does not stop the Synchro or the other Artwork, and is not reported as a Fichier en échec.
- [ ] The Étape declares its units from the number of Artwork to process; the Progression de synchro never goes backwards and reaches 100 % only at the end.
- [ ] It relies on the existing TMDB client call limit; it adds no limit of its own.
- [ ] Private Artwork are processed like the others.
- [ ] Synchro behaviour tests cover: an old Artwork gets its Logo in a light Synchro; empty is not retried; failure then retry; missing token; Unknown Artwork never searched; one failure does not affect the others; progression.
- [ ] `./gradlew :app:testFossDebugUnitTest` passes.
