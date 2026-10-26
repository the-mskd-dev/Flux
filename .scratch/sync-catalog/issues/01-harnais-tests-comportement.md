# 01: Harnais de tests de comportement de la Synchro

**What to build:** un harnais de tests qui pilote la Synchro par son seul point d'entrée, le use case de Synchro (lancer une Synchro légère ou complète, observer `state`), et vérifie le Catalogue en base, les valeurs successives de `state` et le nombre d'appels TMDB par endpoint. Les dépendances extérieures sont remplacées au plus bas : fausses Sources en mémoire (fichiers et disponibilité), TMDB simulé au niveau HTTP par Ktor `MockEngine` avec fixtures JSON, Room en mémoire avec `BundledSQLiteDriver`, `FakeCatalogSyncCoordinator` existant et datastores en mémoire. En préparation, chaque Source indique elle-même si elle est disponible. Voir `docs/specs/sync-catalog.md`.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] Chaque Source expose sa disponibilité (les Sources custom selon l'accès au dossier, le stockage de l'appareil toujours disponible)
- [ ] `ktor-client-mock` est ajouté en dépendance de test ; le faux TMDB compte les appels par endpoint
- [ ] Room en mémoire tourne dans `./gradlew :app:testFossDebugUnitTest` ; sinon le repli (faux dépôt de base de données en mémoire + tests DAO en `androidTest`) est mis en place et documenté dans le ticket
- [ ] Les comportements actuels corrects sont couverts et verts : première Synchro sur tous les fichiers, Synchro légère limitée aux nouveaux fichiers, Synchro légère ignorée pendant une Synchro complète, Fichier non reconnu persisté
- [ ] Les tests vérifient uniquement le comportement observable, jamais les Étapes ou appels internes
