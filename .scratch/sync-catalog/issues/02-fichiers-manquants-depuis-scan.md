# 02: Fichiers manquants calculés depuis le scan

**What to build:** dans les deux modes, les fichiers existants sont les fichiers vus au scan des Sources plus les fichiers du Catalogue appartenant à une Source indisponible. Tout autre fichier du Catalogue est un Fichier manquant : ses données sont supprimées juste après le scan, avec les Saisons et Artworks devenus vides. Aucune requête n'est faite fichier par fichier pour vérifier l'existence. Voir `docs/specs/sync-catalog.md`.

**Blocked by:** 01 (Harnais de tests de comportement)

**Status:** ready-for-agent

- [ ] Un fichier absent d'une Source disponible est supprimé du Catalogue, en Synchro légère comme complète
- [ ] Les Saisons et Artworks sans Média restant sont supprimés
- [ ] Les fichiers d'une Source indisponible sont conservés en Synchro légère
- [ ] La suppression a lieu même si la suite de la Synchro échoue ou est annulée
- [ ] La Synchro n'utilise plus le filtrage des fichiers existants (plus de requête par fichier)
