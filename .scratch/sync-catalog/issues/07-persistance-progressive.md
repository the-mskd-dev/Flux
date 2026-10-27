# 07: Persistance progressive par Artwork

**What to build:** chaque dossier est persisté (Artwork, Saisons, Médias) dans une transaction dès que ses Enrichissements sont terminés, au lieu d'une sauvegarde unique en fin de Synchro. Le Catalogue se remplit au fil de la Synchro et le travail fait reste acquis si elle est interrompue. Les règles de concurrence sont inchangées : une Synchro complète annule une Synchro légère en cours, une Synchro légère demandée pendant une Synchro complète est ignorée. Voir `docs/specs/sync-catalog.md`.

**Blocked by:** 04 (État utilisateur rattaché au chemin du fichier), 06 (Pipeline d'Étapes et Progression de synchro correcte)

**Status:** ready-for-agent

- [ ] Un Artwork est visible en base avant la fin de la Synchro
- [ ] Une Synchro légère annulée par une Synchro complète conserve les dossiers déjà persistés
- [ ] Une Synchro légère demandée pendant une Synchro complète est ignorée
- [ ] Un dossier n'est jamais persisté à moitié (transaction)
- [ ] L'état utilisateur et le statut privé restent conservés avec la persistance progressive
