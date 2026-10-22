# 10: Interface toujours composée, mêmes animations (C4)

Spec : `../spec.md` (lot C)

**What to build:** Un tap sur la vidéo affiche ou masque l'Interface sans pic de frame, avec exactement les mêmes animations qu'aujourd'hui. L'Interface n'est plus détruite puis recréée à chaque tap : elle reste composée, et son apparition et sa disparition sont rejouées par une transition sur la visibilité, appliquée en phase de dessin.

**Blocked by:** 07

**Status:** done

- [ ] L'Interface reste composée quand elle est masquée ; l'apparition et la disparition passent par une transition sur la visibilité et sont appliquées dans `graphicsLayer`
- [ ] Animations strictement identiques à aujourd'hui :
  - [ ] fondu global en spring de faible raideur, à l'apparition comme à la disparition
  - [ ] apparition des boutons de lecture par agrandissement à rebond moyen, disparition par réduction
  - [ ] glissement animé de la marge du Chapter button
- [ ] Si une animation ne peut pas être reproduite à l'identique, l'Interface garde son comportement actuel et le ticket le signale
- [ ] Quand l'Interface est masquée, ses éléments ne reçoivent ni clic ni focus
- [ ] La barre de progression ondulée n'est composée que lorsque l'Interface est visible ou en train d'apparaître, et elle ne s'anime plus en arrière-plan
- [ ] La hauteur de la barre de progression n'est plus mesurée à chaque apparition
- [ ] Le masquage automatique et l'affichage simultané des barres système fonctionnent comme avant
- [ ] `./gradlew :app:testFossDebugUnitTest` passe

## Comments

- Implémenté via `updateTransition` (alpha, échelle des boutons, glissement des FAB) lu dans `graphicsLayer`. Specs reprises des defaults d'`AnimatedVisibility` : `scaleOut()`/`slide*()` = spring `StiffnessMediumLow` (à vérifier visuellement sur appareil).
- Éléments désactivés/non focusables une fois l'Interface totalement masquée (pas pendant le fondu, comme avant). Barre ondulée composée seulement tant que l'Interface est visible ou en transition.
- Hauteur de la barre : `onSizeChanged` au lieu de `onGloballyPositioned`.
