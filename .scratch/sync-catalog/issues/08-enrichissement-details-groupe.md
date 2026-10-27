# 08: Enrichissement Détails groupé (traductions et credits)

**What to build:** l'Enrichissement Détails fait un seul appel de détails par Artwork, films et séries, avec `append_to_response=translations,credits`. Les traductions récupérées remplacent les appels de traduction séparés pour les Artworks et les films (langue de l'utilisateur, repli sur l'anglais). Les credits sont enregistrés avec l'Artwork, ce qui supprime le second passage de synchro des people pour les Artworks traités ; le rafraîchissement périodique des people reste inchangé. Voir `docs/specs/sync-catalog.md`.

**Blocked by:** 06 (Pipeline d'Étapes et Progression de synchro correcte)

**Status:** ready-for-agent

- [ ] Pour un nouveau film, la Synchro fait exactement une recherche et un appel de détails (compté par le faux TMDB)
- [ ] Pour une nouvelle série, un seul appel de détails de série, sans appel de traduction séparé
- [ ] Le titre et la description vides dans la langue de l'utilisateur sont complétés par l'anglais
- [ ] Les credits d'un nouvel Artwork sont en base dès la fin de la Synchro, sans appel de credits séparé
- [ ] Le rafraîchissement périodique des people n'est pas modifié
