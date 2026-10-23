# 05: Condition de clic (token TMDB) et variante floue

**What to build:** sans token TMDB, l'Artwork image n'est pas cliquable, et elle le devient dès qu'un token est saisi, sans redémarrage. La variante floue (`ArtworkImageBlurred`) gère aussi le tap. `ArtworkImage` expose un callback `onClick` optionnel utilisé par tous les écrans.

**Blocked by:** 01, 04

**Status:** ready-for-agent

- [ ] Sans token, l'Artwork image n'est pas cliquable (aucun effet visuel ni snackbar)
- [ ] Après saisie d'un token, elle devient cliquable sans redémarrage
- [ ] Hors connexion avec token, elle reste cliquable et la Gallery affiche l'état indisponible
- [ ] La variante floue ouvre aussi la Gallery
- [ ] Le comportement est identique sur les écrans Artwork (Movie) et Show
