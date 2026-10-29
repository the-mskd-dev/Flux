# 06: Préparation du contrat du lecteur

Spec : `../spec.md` (lot B)

**What to build:** Le contrat du lecteur ne contient plus que ce qui est utilisé, et une sauvegarde de Progress en base ne recompose plus l'écran. Les intents morts sont supprimés, l'intent de déplacement est renommé selon le glossaire, et l'Artwork complet sort de l'état exposé : le VM ne s'en sert qu'en interne pour résoudre le Media et ne réagit qu'à un changement de Media.

**Blocked by:** 01, 02, 03, 04, 05 (barrière de lot : le lot A est mesuré seul avant de commencer le lot B)

**Status:** ready-for-agent

- [ ] Suppression des intents `PlayMedia` et `SaveTime` ; la sauvegarde de Progress reste interne au VM
- [ ] `UpdateProgress` est renommé `SeekTo(progress)`, à tous les endroits qui l'envoient
- [ ] L'Artwork complet n'est plus présent dans l'état exposé à l'écran
- [ ] Une ré-émission de l'Artwork sans changement de Media ne produit aucune nouvelle émission d'état
- [ ] Nouveau cas dans `PlayerViewModelTest` couvrant le point précédent ; les tests existants sont adaptés
- [ ] `./gradlew :app:testFossDebugUnitTest` passe
