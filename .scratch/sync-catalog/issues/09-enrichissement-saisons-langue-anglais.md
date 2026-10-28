# 09: Enrichissement Saisons en langue de l'utilisateur et en anglais

**What to build:** l'Enrichissement Saisons fait deux appels de détails par saison : un dans la langue de l'utilisateur, un en anglais. Les titres et descriptions d'épisodes vides dans la langue de l'utilisateur sont complétés par l'anglais. Plus aucun appel de traduction n'est fait par épisode, ce qui rend la Synchro des longues saisons rapide. Voir `docs/specs/sync-catalog.md`.

**Blocked by:** 06 (Pipeline d'Étapes et Progression de synchro correcte)

**Status:** ready-for-agent

- [ ] Pour une saison de N épisodes, la Synchro fait exactement deux appels de saison et aucun appel de traduction d'épisode
- [ ] Un épisode sans titre ou description dans la langue de l'utilisateur reçoit ceux de l'anglais
- [ ] Un épisode traduit dans la langue de l'utilisateur garde sa traduction
- [ ] L'échec de l'appel en anglais ne fait pas échouer la saison (les données dans la langue de l'utilisateur sont gardées)
