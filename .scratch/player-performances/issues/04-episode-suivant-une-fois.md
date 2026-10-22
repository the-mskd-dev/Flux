# 04: Épisode suivant recherché une seule fois par Media (C11)

Spec : `../spec.md` (lot A)

**What to build:** Le Chapter button « épisode suivant » apparaît toujours en fin d'Episode (dernier chapitre de fin ou 15 dernières secondes). La recherche de l'Episode suivant n'est plus relancée à chaque tick de Progress : son résultat est mis en cache pour le Media en cours et réinitialisé quand le Media change.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] La recherche de l'Episode suivant est faite au plus une fois par Media
- [ ] Le cache est réinitialisé au changement de Media (y compris après un tap sur « épisode suivant »)
- [ ] Le Chapter button apparaît dans les mêmes conditions qu'avant
- [ ] Nouveau cas dans `PlayerViewModelTest` : plusieurs ticks de Progress en fin de Media donnent un seul appel à la recherche de l'Episode suivant
- [ ] `./gradlew :app:testFossDebugUnitTest` passe
