# 08: Gestes de volume et de luminosité sans latence (C2)

Spec : `../spec.md` (lot C)

**What to build:** Un glissement vertical à gauche (luminosité) ou à droite (volume) suit le doigt sans latence, et l'Ambient overlay affiche la valeur en temps réel avant de disparaître 1 seconde après la fin du geste. La luminosité est appliquée entièrement côté UI. Le volume passe toujours par le VM, puis sa valeur revient à l'écran. L'Ambient overlay devient un état local de l'écran. Les déplacements du doigt sont cumulés et appliqués au plus une fois par frame.

**Blocked by:** 07

**Status:** ready-for-agent

- [ ] La luminosité ne passe plus par le VM ; `PlayerEvent.ChangeBrightness` et `PlayerIntent.UpdateAmbientOverlay` sont supprimés
- [ ] L'Ambient overlay ne fait plus partie de l'état du VM ; il est géré localement par l'écran, avec masquage automatique après 1 seconde d'inactivité
- [ ] Au plus une application de luminosité et un intent de volume par frame pendant un geste
- [ ] La valeur de volume affichée correspond au volume réellement appliqué au lecteur
- [ ] La luminosité est toujours réinitialisée en quittant le lecteur
- [ ] `PlayerViewModelTest` est adapté (volume) ; `./gradlew :app:testFossDebugUnitTest` passe
