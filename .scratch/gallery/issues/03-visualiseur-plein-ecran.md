# 03: Visualiseur plein écran avec zoom et swipe

**What to build:** un tap sur une image de la grille ouvre un overlay plein écran dans la Gallery, qui charge l'image en `original` avec `ContentScale.Fit`. Le swipe latéral passe d'une image à l'autre dans l'ordre de la grille. Le pinch et le double-tap (1× ↔ 2×) zooment. Le swipe est bloqué tant que le zoom dépasse 1×. Le bouton retour ferme d'abord le visualiseur, puis la page, et la grille garde sa position de scroll.

**Blocked by:** 01

**Status:** ready-for-agent

- [ ] Le tap sur une image ouvre le visualiseur sur cette image
- [ ] Le swipe latéral suit l'ordre de la grille
- [ ] Pinch et double-tap zooment, le swipe est bloqué au-delà de 1×
- [ ] Le retour ferme d'abord le visualiseur, puis la Gallery
- [ ] La grille garde sa position de scroll après fermeture
- [ ] L'ouverture, la fermeture et l'index courant sont couverts par les tests du `GalleryViewModel`
