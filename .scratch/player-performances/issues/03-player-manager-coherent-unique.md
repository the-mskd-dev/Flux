# 03: AndroidPlayerManager cohérent et unique (C8, C9, C10)

Spec : `../spec.md` (lot A)

**What to build:** L'état lecture/pause, la durée et les Track sélectionnées affichés correspondent toujours à l'état réel du lecteur. Quitter un ancien écran de lecteur ne coupe plus la lecture d'un nouveau. Toutes les mises à jour d'état du manager partent de la valeur courante, et l'application n'utilise qu'une seule instance du manager, donc un seul `MediaController` sur le `PlayerService`.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] Dans `onEvents`, lecture/pause et durée produisent une seule émission, calculée à partir de l'état courant ; une mise à jour n'écrase plus l'autre
- [ ] `selectTrack` et `onTracksChanged` mettent l'état à jour à partir de sa valeur courante, pas d'un instantané capturé avant
- [ ] Le manager est déclaré en `single` Koin au lieu de `factory`
- [ ] L'état `Connecting` est positionné pendant la connexion au `MediaController` ; un second `connect` pendant la connexion est ignoré
- [ ] `disconnect`, pour la session courante, annule les jobs du manager et repasse en `Idle` ; le `disconnect` d'une session obsolète reste sans effet
- [ ] Le passage à l'Episode suivant, le Picture-in-Picture, l'arrière-plan et le retour au premier plan fonctionnent comme avant (vérification manuelle)
- [ ] `KoinModulesTest` et `./gradlew :app:testFossDebugUnitTest` passent
