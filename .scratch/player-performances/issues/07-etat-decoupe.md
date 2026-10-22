# 07: État du lecteur découpé par fréquence de changement (C1)

Spec : `../spec.md` (lot B)

**What to build:** Un changement d'overlay, de lecture/pause ou de visibilité de l'Interface ne recompose plus que les composables concernés. L'état monolithique est remplacé par plusieurs flux d'état exposés par le VM, chacun collecté le plus bas possible dans l'arbre (ou transmis en lambda) :
- **session** : statut chargement/erreur, Media, player, Track disponibles et sélectionnées, durée, valeurs de saut ;
- **isPlaying** : lecture ou pause ;
- **interface** : visibilité de l'Interface, Settings sheet, Picture-in-Picture ;
- **overlays** : Seek overlay, Ambient overlay (jusqu'au ticket 08), Chapter button ;
- **progress** et **subtitles** : inchangés.

**Blocked by:** 06

**Status:** done

- [ ] Les flux ci-dessus remplacent l'état monolithique ; les types intermédiaires `PlayerDataState` et `PlayerUserState`, et leur fusion, sont supprimés
- [ ] Un changement d'overlay n'émet ni sur `session` ni sur `interface` (nouveau cas de test)
- [ ] L'écran et ses sous-composants ne lisent que les flux dont ils ont besoin
- [ ] Chargement, erreur, passage à l'Episode suivant, masquage automatique de l'Interface, Picture-in-Picture et Settings sheet fonctionnent comme avant
- [ ] `PlayerViewModelTest` est réécrit pour le nouveau contrat et couvre tous les comportements existants
- [ ] `./gradlew :app:testFossDebugUnitTest` passe
