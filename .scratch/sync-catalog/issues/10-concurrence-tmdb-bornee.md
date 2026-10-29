# 10: Concurrence TMDB bornée

**What to build:** un sémaphore unique, partagé par tous les Enrichissements, borne le nombre d'appels TMDB simultanés (8 à 10). Il remplace le dispatcher à parallélisme limité, qui bornait les threads et non les requêtes en vol. Les gros catalogues ne provoquent plus de rafales d'appels ni de timeouts. Voir `docs/specs/sync-catalog.md`.

**Blocked by:** 06 (Pipeline d'Étapes et Progression de synchro correcte)

**Status:** ready-for-agent

- [ ] Sur un catalogue de plusieurs centaines de fichiers, le faux TMDB n'observe jamais plus d'appels en vol que la limite
- [ ] La limite est définie à un seul endroit
- [ ] Le dispatcher à parallélisme limité de la Synchro n'est plus utilisé pour borner les appels
