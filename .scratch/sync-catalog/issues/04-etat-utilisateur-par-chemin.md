# 04: État utilisateur rattaché au chemin du fichier

**What to build:** l'upsert des Médias n'écrit que les colonnes issues de TMDB. Le temps de lecture courant et le statut vu/à voir sont rattachés au chemin du fichier : lors de l'upsert, ils sont repris depuis la ligne existante de même chemin, même si l'identité TMDB du Média a changé (fichier rattaché à un autre Artwork). La reprise de progression par nom de fichier disparaît. Voir `docs/specs/sync-catalog.md`.

**Blocked by:** 03 (Synchro complète sans effacement global)

**Status:** ready-for-agent

- [x] Le temps de lecture et le statut d'un Média sont conservés après une Synchro légère et une Synchro complète
- [x] Ils sont conservés quand une Synchro complète rattache le fichier à un autre Artwork
- [x] Deux fichiers de même nom dans des dossiers différents gardent chacun leur propre progression
- [x] La fonction de reprise de progression par nom de fichier est supprimée
- [x] L'upsert partiel est couvert par un test sur la base (Room en mémoire ou `androidTest` selon le repli du ticket 01)

**Notes:**

- `saveMedias` reste l'écriture complète (utilisée par la progression, le statut, la langue). La Synchro passe par `saveSyncedMedias` → `MediasDao.upsertFromSync`, qui reprend `currentTime` et `status` de la ligne de même chemin.
- Le cas « deux fichiers de même nom, dossiers différents » est couvert au niveau DAO (`MediasDaoTest`, `androidTest`, compilé mais non exécuté ici faute d'appareil) : le harnais ne peut pas le produire car TMDB identifie deux fichiers de même nom comme le même Média.
