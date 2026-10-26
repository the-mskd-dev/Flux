# 01: Harnais de tests de comportement de la Synchro

**What to build:** un harnais de tests qui pilote la Synchro par son seul point d'entrée, le use case de Synchro (lancer une Synchro légère ou complète, observer `state`), et vérifie le Catalogue en base, les valeurs successives de `state` et le nombre d'appels TMDB par endpoint. Les dépendances extérieures sont remplacées au plus bas : fausses Sources en mémoire (fichiers et disponibilité), TMDB simulé au niveau HTTP par Ktor `MockEngine` avec fixtures JSON, Room en mémoire avec `BundledSQLiteDriver`, `FakeCatalogSyncCoordinator` existant et datastores en mémoire. En préparation, chaque Source indique elle-même si elle est disponible. Voir `docs/specs/sync-catalog.md`.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [x] Chaque Source expose sa disponibilité (les Sources custom selon l'accès au dossier, le stockage de l'appareil toujours disponible)
- [x] `ktor-client-mock` est ajouté en dépendance de test ; le faux TMDB compte les appels par endpoint
- [x] Room en mémoire tourne dans `./gradlew :app:testFossDebugUnitTest` ; sinon le repli (faux dépôt de base de données en mémoire + tests DAO en `androidTest`) est mis en place et documenté dans le ticket
- [x] Les comportements actuels corrects sont couverts et verts : première Synchro sur tous les fichiers, Synchro légère limitée aux nouveaux fichiers, Synchro légère ignorée pendant une Synchro complète, Fichier non reconnu persisté
- [x] Les tests vérifient uniquement le comportement observable, jamais les Étapes ou appels internes

**Notes:**

- Room en mémoire tourne dans `testFossDebugUnitTest` : aucun repli nécessaire. Le `BundledSQLiteDriver` Android n'embarque pas de binaire pour la machine hôte ; `app/build.gradle.kts` extrait celui de `sqlite-bundled-jvm` et le place sur `java.library.path`. Ce binaire est pris en 2.6.2, dernière version qui fournit encore `osx_x64` (Mac Intel) ; il fonctionne avec le driver 2.7.0.
- Chaque dossier custom est désormais une Source à part entière (`SafFilesDataSource` par dossier). Les sous-titres sont cherchés par la Source qui contient le fichier.
- Harnais : `app/src/test/.../features/catalog/sync/` (`SyncHarness`, `FakeTmdb`, fixtures dans `app/src/test/resources/tmdb/`). Il remplace l'ancien `SyncCatalogUseCaseTest`.
- Le harnais remplace le fournisseur de Sources par un fournisseur en mémoire (stockage de l'appareil + Sources custom ajoutées par le test). `AndroidSourcesProvider` construit des Sources SAF qui demandent un `Context` Android ; il garde son propre test unitaire.
- `FakeCatalogSyncCoordinator` annule désormais la Synchro en cours, comme le vrai coordinator, pour que la règle « une Synchro complète annule une Synchro légère » soit observable. Il n'expose pas la Progression dans `state` : le ticket 06 devra l'enrichir ou utiliser `CatalogSyncCoordinatorImpl`.
