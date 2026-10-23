# Spec : Mémoriser la désactivation des sous-titres

Status: ready-for-agent

Source : https://github.com/the-mskd-dev/Flux/issues/379 (triage du 2026-10-23, périmètre global validé par le mainteneur).

## Agent Brief

**Category:** enhancement
**Summary:** Quand l'utilisateur désactive les sous-titres, ce choix est mémorisé globalement et réappliqué à chaque lecture suivante, jusqu'à ce qu'il choisisse à nouveau une Track de sous-titres.

**Current behavior:**
La langue de la Track de sous-titres choisie est mémorisée dans `SettingsDataStore` (`subtitlesLanguage`, valeur par défaut : langue du système) via `SaveTrackLanguageUseCase`. Choisir « aucun sous-titre » (`PlayerTrack.NO_SUBTITLES`, dont `language` vaut `null`) désactive le type de Track texte dans le player, mais `SaveTrackLanguageUseCase` ne sauvegarde rien dans ce cas. Au lancement d'un Media, le player applique la langue de sous-titres préférée et `PlayerViewModel` resélectionne cette langue quand les Tracks sont disponibles. Résultat : les sous-titres réapparaissent à chaque nouvelle lecture, même si l'utilisateur les avait désactivés.

**Desired behavior:**
- Un nouveau réglage global indique si les sous-titres sont activés. Valeur par défaut : activés, pour que le comportement actuel ne change pas pour les utilisateurs existants ni pour les nouvelles installations.
- Sélectionner `PlayerTrack.NO_SUBTITLES` pendant la lecture sauvegarde « sous-titres désactivés ». La langue de sous-titres déjà sauvegardée est conservée.
- Sélectionner une vraie Track de sous-titres sauvegarde « sous-titres activés » et met à jour la langue comme aujourd'hui.
- Au lancement d'un Media (ouverture depuis la bibliothèque, reprise, épisode suivant) avec les sous-titres désactivés :
  - aucun sous-titre ne s'affiche, y compris pendant les premières images (le type de Track texte est désactivé avant `prepare`, au même endroit où les langues préférées sont appliquées) ;
  - la resélection des Tracks préférées une fois les Tracks disponibles ne réactive pas les sous-titres ;
  - la Track de sous-titres sélectionnée exposée à l'UI est `PlayerTrack.NO_SUBTITLES`, pour que les feuilles de réglages du lecteur affichent « aucun sous-titre » comme sélectionné ;
  - un fichier de sous-titres externe trouvé à côté du fichier vidéo ne réactive pas non plus les sous-titres.
- Avec les sous-titres activés, le comportement est identique à celui d'aujourd'hui.
- La gestion de la Track audio ne change pas.

**Key interfaces:**
- `SettingsDataStore` : nouveau setter (par ex. `setSubtitlesEnabled(enabled: Boolean)`) et nouveau champ `SettingsDataStore.State.subtitlesEnabled: Boolean = true`, stocké sous une nouvelle clé de préférence booléenne dans `SettingsDataStoreImpl`. Clé absente = activés.
- `SaveTrackLanguageUseCase` : l'invocation avec une Track de sous-titres doit aussi enregistrer l'état activé/désactivé (`NO_SUBTITLES` donne désactivé, une Track avec langue donne activé). Il faut aussi un moyen de lire ce réglage (par ex. `subtitlesEnabled(): Boolean`), à côté de `getSubtitlesLanguage()`. Le use case peut être renommé s'il ne fait plus seulement de la langue ; dans ce cas, mettre à jour les bindings Koin.
- La configuration des `trackSelectionParameters` de `AndroidPlayerManager` au lancement d'un Media : désactiver le type de Track texte quand le réglage est à « désactivés ».
- `PlayerViewModel.updateTracks()` : sélectionner `PlayerTrack.NO_SUBTITLES` au lieu de la langue préférée quand le réglage est à « désactivés ».

**Acceptance criteria:**
- [ ] `SettingsDataStore.State.subtitlesEnabled` vaut `true` quand rien n'a été sauvegardé, et le setter met à jour le flow (nouveau cas dans `SettingsDataStoreImplTest`)
- [ ] Invoquer `SaveTrackLanguageUseCase` avec `PlayerTrack.NO_SUBTITLES` sauvegarde « désactivés » et ne touche pas à la langue de sous-titres (remplace le test actuel « no subtitles track does not save anything »)
- [ ] L'invoquer avec une Track de sous-titres qui a une langue sauvegarde « activés » et la langue
- [ ] L'invoquer avec une Track audio ne change pas le réglage d'activation des sous-titres
- [ ] Nouveau cas dans `PlayerViewModelTest` : avec les sous-titres désactivés, la mise à jour des Tracks transmet `PlayerTrack.NO_SUBTITLES` au `PlayerManager`, et jamais la langue préférée
- [ ] Vérification manuelle sur appareil : désactiver les sous-titres, quitter le lecteur, rouvrir le même Episode puis un autre Episode : aucun sous-titre ne s'affiche et la feuille Sous-titres montre « aucun sous-titre » sélectionné ; resélectionner une Track de sous-titres, quitter, rouvrir : les sous-titres sont de retour
- [ ] `./gradlew :app:testFossDebugUnitTest` passe

**Out of scope:**
- Préférences de sous-titres par série ou par Media (demandées en option dans l'issue ; à traiter dans une issue séparée si besoin)
- Ajouter un interrupteur « sous-titres » dans l'écran Réglages
- Une option « désactiver l'audio » ou tout autre changement sur la sélection de la Track audio
- Le style ou le rendu des sous-titres
- Le lecteur externe

## Comments
