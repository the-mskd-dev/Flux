# 01: Gallery minimale, ouverture et grille de la liste mixte

**What to build:** un tap sur l'Artwork image, dans la page Artwork en mode Movie, ouvre la page Gallery. Elle charge les images du film depuis TMDB (backdrops et posters, logos ignorés) et les affiche dans une grille décalée (2 colonnes sur mobile, 3 sur tablette), triée par note décroissante, avec le titre de l'Artwork. La grille charge `w342` pour les posters et `w780` pour les backdrops. Rien n'est stocké en base. Voir `docs/specs/gallery.md`.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] Le tap sur l'Artwork image d'un film ouvre la Gallery via `Route.Gallery(artworkId, contentType)`
- [ ] Les images du film sont lues depuis l'API TMDB et mappées vers un modèle de domaine
- [ ] La grille mélange backdrops et posters, triés par `vote_average` décroissant, sans rognage
- [ ] Le `GalleryViewModel` est testé en JVM avec des fakes (chargement puis liste triée)
- [ ] Le service TMDB est testé : backdrops et posters mappés, logos ignorés
