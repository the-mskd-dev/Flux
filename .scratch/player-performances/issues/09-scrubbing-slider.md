# 09: Scrubbing du slider de Progress (C6)

Spec : `../spec.md` (lot C)

**What to build:** Pendant que l'utilisateur fait glisser le slider de Progress, le curseur et le temps affiché suivent exactement le doigt, et l'image de la vidéo se met à jour sans saturer le décodeur. Au relâchement, la lecture reprend exactement à la position choisie.

**Blocked by:** 07

**Status:** done

- [ ] Pendant le glissement, le curseur et le temps affiché viennent de l'état local du doigt, pas de la Progress du lecteur (pas de retour en arrière du curseur)
- [ ] Pendant le glissement, au plus un seek toutes les ~150 ms, en mode rapide (image clé la plus proche, `CLOSEST_SYNC`) ; la limitation est faite côté UI
- [ ] Au relâchement, un seek exact est envoyé sur la position finale
- [ ] `SeekTo` porte la précision demandée (rapide ou exacte) jusqu'à `PlayerManager.seekTo`, qui l'applique
- [ ] La plage de valeurs du slider n'est plus réallouée à chaque recomposition
- [ ] Nouveau cas dans `PlayerViewModelTest` : `SeekTo` transmet la précision demandée au `PlayerManager`
- [ ] `./gradlew :app:testFossDebugUnitTest` passe
