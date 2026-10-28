# 03: Synchro complète sans effacement global

**What to build:** la Synchro complète n'efface plus le Catalogue. Comme la Synchro légère, elle met à jour par upsert les données TMDB de ses fichiers puis retire les Fichiers manquants ; la seule différence entre les deux modes est l'ensemble des fichiers envoyés à TMDB. Les données d'une Source indisponible survivent donc à une Synchro complète, et le statut privé des Artworks est conservé par l'upsert, sans sauvegarde ni restauration. Voir `docs/specs/sync-catalog.md`.

**Blocked by:** 02 (Fichiers manquants calculés depuis le scan)

**Status:** ready-for-agent

- [ ] Après une Synchro complète, les Médias, Saisons et Artworks d'une Source indisponible sont toujours présents
- [ ] Un Artwork privé reste privé après une Synchro complète
- [ ] Le Catalogue reste lisible pendant la Synchro complète (pas de phase vide)
- [ ] Le mécanisme de capture/restauration des Artworks privés est retiré
- [ ] Les genres ne sont rafraîchis qu'en Synchro complète
