# 06: Pipeline d'Étapes et Progression de synchro correcte

**What to build:** la Synchro devient un pipeline d'Étapes de synchro : Scan → Fichiers manquants → Genres (Synchro complète uniquement) → Catalogue → NSFW privé → Finalisation. L'Étape catalogue regroupe les fichiers par dossier et applique à chacun une chaîne d'Enrichissements (Identification, Détails, Saisons, Durées). Dès la fin du scan, chaque Étape et chaque Enrichissement déclare un nombre estimé d'unités, déduit des noms de fichiers ; la Progression de synchro vaut unités faites / unités déclarées, peut être réajustée en cours de route, ne recule jamais et n'atteint 100 % qu'à la fin. La Finalisation enregistre l'heure de Synchro et le code de version et lance le préchargement des images en arrière-plan. Voir `docs/specs/sync-catalog.md`.

**Blocked by:** 03 (Synchro complète sans effacement global), 05 (Fichiers en échec retentés)

**Status:** ready-for-agent

- [ ] La Progression observée dans `state` est croissante, sans jamais reculer, sur un catalogue mêlant films, séries et Fichiers non reconnus
- [ ] Elle n'atteint 100 % qu'après la dernière Étape
- [ ] Les Artworks NSFW passent en privé quand le dossier privé et l'option NSFW sont activés
- [ ] L'heure de Synchro et le code de version sont enregistrés à la fin, y compris quand il n'y a aucun nouveau fichier
- [ ] Le préchargement des images ne retarde pas la fin de la Synchro
- [ ] Ajouter un Enrichissement ne demande de modifier ni le use case ni le calcul de progression
- [ ] Tous les tests de comportement des tickets précédents restent verts
