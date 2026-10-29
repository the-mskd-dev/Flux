# 04: Gallery pour les séries

**What to build:** le tap sur l'Artwork image des écrans Show (regular et large) ouvre la Gallery de la série. Les images viennent de l'endpoint TMDB des séries, et seules celles de la série sont affichées, sans saisons ni épisodes. Le tap ouvre la Gallery de la série même quand la bannière affiche l'image d'un épisode.

**Blocked by:** 01

**Status:** ready-for-agent

- [ ] Le tap sur l'Artwork image d'une série (regular et large) ouvre la Gallery
- [ ] Seules les images de la série sont chargées (endpoint TMDB `tv`)
- [ ] Le comportement est identique quand la bannière affiche un épisode
- [ ] Le service TMDB est testé pour les séries (backdrops, posters, logos ignorés)
- [ ] Le `GalleryViewModel` choisit la bonne source selon le type de contenu
