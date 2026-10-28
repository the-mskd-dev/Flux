# 02: Masquage des spoilers des Episode (réglage + flou)

**What to build:** L'utilisateur peut activer le Masquage des spoilers dans les Settings (désactivé par défaut). Une fois activé, sur la page d'un Artwork, la description et la vignette de chaque Episode à voir sont floutées ; titre, numéro, durée, Status et progression restent lisibles. Dès qu'un Episode passe en cours ou vu (lecture, « Marquer comme vu »), il redevient lisible avec une transition animée ; il est de nouveau flouté si sa Progress est réinitialisée. Le changement de réglage se reflète immédiatement sur une page ouverte.

À l'issue de ce ticket, les Spoiler d'Episode sont masqués mais ne peuvent pas encore être révélés (ticket 03).

Voir la spec `docs/specs/masquage-des-spoilers.md` (Règle « est-ce un Spoiler ? », Rendu, Settings).

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] Nouvelle préférence booléenne dans le DataStore des Settings (clé `blur_spoilers`, défaut `false`), exposée dans l'état des Settings, avec un setter.
- [ ] Switch « Masquer les spoilers » (titre + sous-titre explicatif, localisés) dans la section Personnalisation de l'écran Settings, branché sur un intent du ViewModel des Settings.
- [ ] L'`ArtworkViewModel` combine la préférence et expose, pour chaque Episode, s'il est masqué.
- [ ] Règle : un Episode est masqué si l'option est active, que son Status est `TO_WATCH` et qu'il n'appartient pas à l'Unknown Artwork. `IS_WATCHING` et `WATCHED` ne sont jamais masqués ; les Movie ne sont pas concernés.
- [ ] Nouveau modifier de flou de contenu, à côté des extensions de flou Haze existantes, basé sur l'entrée « contenu » de Haze, avec le flou forcé actif (délégué RenderScript sur API 29-30).
- [ ] Le flou s'applique à la description et à la vignette dans `EpisodeItemLarge` et `EpisodeItemSmall` ; le grisage des Episode Unavailable est conservé.
- [ ] Le rayon de flou est animé (~300 ms) entre masqué et lisible, y compris lors d'un changement de Status.
- [ ] Tests : DataStore des Settings (persistance, défaut `false`) ; ViewModel des Settings (l'intent met à jour la préférence) ; `ArtworkViewModel` (option désactivée → rien de masqué ; `TO_WATCH` masqué ; `IS_WATCHING` / `WATCHED` lisibles ; Unknown Artwork jamais masqué ; passage d'un Episode à `IS_WATCHING` via le flux observé → lisible ; bascule de la préférence page ouverte → état mis à jour).
- [ ] Vérification manuelle sur émulateur API 29 (flou RenderScript, ou à défaut voile Haze acceptable) et API 31+.
- [ ] `./gradlew :app:testFossDebugUnitTest` passe.
