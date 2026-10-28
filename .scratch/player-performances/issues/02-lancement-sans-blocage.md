# 02: Lancement d'un Media et changement de Track sans blocage du main thread (C5)

Spec : `../spec.md` (lot A)

**What to build:** Lancer un Media ou changer de Track ne fige plus l'écran. Le libellé par défaut des Track (« Piste #n », pour les Track sans label ni langue) n'est plus lu de façon synchrone sur le main thread quand les Track changent : il est résolu une fois, de façon asynchrone, au lancement du Media, puis réutilisé.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] Plus aucun `runBlocking` dans `AndroidPlayerManager`
- [ ] Le libellé par défaut est résolu dans le chemin suspendant du lancement d'un Media et réutilisé lors des changements de Track
- [ ] Les Track sans label ni langue affichent toujours « Piste #n » dans la Settings sheet
- [ ] `./gradlew :app:testFossDebugUnitTest` passe
