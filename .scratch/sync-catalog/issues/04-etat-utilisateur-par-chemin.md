# 04: État utilisateur rattaché au chemin du fichier

**What to build:** l'upsert des Médias n'écrit que les colonnes issues de TMDB. Le temps de lecture courant et le statut vu/à voir sont rattachés au chemin du fichier : lors de l'upsert, ils sont repris depuis la ligne existante de même chemin, même si l'identité TMDB du Média a changé (fichier rattaché à un autre Artwork). La reprise de progression par nom de fichier disparaît. Voir `docs/specs/sync-catalog.md`.

**Blocked by:** 03 (Synchro complète sans effacement global)

**Status:** ready-for-agent

- [ ] Le temps de lecture et le statut d'un Média sont conservés après une Synchro légère et une Synchro complète
- [ ] Ils sont conservés quand une Synchro complète rattache le fichier à un autre Artwork
- [ ] Deux fichiers de même nom dans des dossiers différents gardent chacun leur propre progression
- [ ] La fonction de reprise de progression par nom de fichier est supprimée
- [ ] L'upsert partiel est couvert par un test sur la base (Room en mémoire ou `androidTest` selon le repli du ticket 01)
