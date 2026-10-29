# 01: Zoom à deux doigts fluide (C3)

Spec : `../spec.md` (lot A)

**What to build:** Quand l'utilisateur passe en plein écran (ou en revient) avec un pinch, l'animation d'échelle de la vidéo reste fluide pendant toute sa durée. La valeur animée n'est lue qu'en phase de dessin, si bien que le contenu du lecteur n'est plus recomposé à chaque frame de l'animation.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] L'échelle animée de la vidéo est appliquée dans un bloc `graphicsLayer { }` (lecture en phase de dessin, pas en composition)
- [ ] Le rendu et la durée de l'animation de zoom sont inchangés
- [ ] Le pinch pour agrandir ou réduire fonctionne toujours en portrait et en paysage
- [ ] `./gradlew :app:testFossDebugUnitTest` passe
