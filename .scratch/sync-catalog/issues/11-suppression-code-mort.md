# 11: Suppression du code mort

**What to build:** retrait de tout ce que le nouveau pipeline rend inutile : le use case de nettoyage du Catalogue (jamais appelé), le use case de filtrage des fichiers existants et la méthode correspondante des deux sources de fichiers (dont la requête MediaStore `IN (…)` qui dépasse la limite SQLite au-delà de 999 fichiers), les anciens fetchers d'Artworks, de films et de saisons, le resolver de médias et l'agrégateur de contenu. L'ancien test du use case de Synchro est remplacé par les tests de comportement. Voir `docs/specs/sync-catalog.md`.

**Blocked by:** 02 (Fichiers manquants calculés depuis le scan), 07 (Persistance progressive par Artwork), 08 (Enrichissement Détails groupé), 09 (Enrichissement Saisons en langue de l'utilisateur et en anglais)

**Status:** ready-for-agent

- [ ] Aucune référence aux éléments retirés ne subsiste (code et modules Koin)
- [ ] L'ancien test du use case de Synchro est supprimé ; les tests de comportement couvrent ses cas encore pertinents
- [ ] `./gradlew :app:testFossDebugUnitTest` passe
- [ ] L'app se build (`./gradlew installFossDebug` ou équivalent assemble)
