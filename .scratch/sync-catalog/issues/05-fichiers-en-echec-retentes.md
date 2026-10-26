# 05: Fichiers en échec retentés

**What to build:** chaque dossier traité par la Synchro aboutit à un résultat typé : trouvé, introuvable (Fichier non reconnu) ou échec (Fichier en échec). Un échec (réseau, API, token TMDB absent) n'est pas persisté : le fichier n'entre pas dans le Catalogue et il est retenté à la Synchro légère suivante, sans backoff ni compteur. Seul « introuvable » produit un Média sans Artwork connu. Une erreur sur un dossier ou un Média (traduction, durée) n'interrompt plus la Synchro. Voir `docs/specs/sync-catalog.md`.

**Blocked by:** 01 (Harnais de tests de comportement)

**Status:** ready-for-agent

- [ ] Une erreur réseau sur la recherche d'un fichier ne le persiste pas ; la Synchro légère suivante le retente et le persiste s'il est trouvé
- [ ] L'absence de token TMDB est traitée comme un échec, pas comme un Fichier non reconnu
- [ ] Un fichier introuvable sur TMDB est persisté comme Fichier non reconnu et n'est pas retenté
- [ ] Une exception sur un Média (traduction, durée) ou un dossier n'empêche pas les autres d'être persistés
- [ ] Le nombre de Fichiers en échec de la dernière Synchro est disponible dans `SyncState`
