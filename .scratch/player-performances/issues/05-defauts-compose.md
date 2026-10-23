# 05: Petits défauts Compose du lecteur (C7)

Spec : `../spec.md` (lot A)

**What to build:** Suppression des recompositions et allocations inutiles dans les sous-composants du lecteur, sans changement visible.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] L'Ambient overlay n'écrit plus d'état pendant la composition
- [ ] Les deux dégradés du Seek overlay sont mémorisés au lieu d'être alloués à chaque recomposition
- [ ] La barre du haut n'applique plus le modifier parent (et son `layoutId`) à son contenu, et n'applique plus le padding de barre de statut en double
- [ ] Les détecteurs de gestes de l'écran utilisent toujours la dernière fonction d'envoi d'intent (`rememberUpdatedState`)
- [ ] Rendu identique de l'Ambient overlay, du Seek overlay et de la barre du haut (vérification manuelle)
- [ ] `./gradlew :app:testFossDebugUnitTest` passe
