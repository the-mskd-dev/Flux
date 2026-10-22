# Spec : Performances du lecteur

Status: ready-for-agent

Source : `docs/performances_report.md` (causes C1 à C9) + session de grilling du 2026-10-22 (causes C10, C11 ajoutées).

## Problem Statement

Pendant la lecture, l'écran du lecteur saccade ponctuellement et répond mal aux gestes :

- l'apparition de l'Interface au tap provoque des pics de frame très lourds (P99 ≈ 150 ms) ;
- les gestes verticaux de volume et de luminosité saturent la file d'input (1812 événements à haute latence pour 1391 frames) ;
- le pinch-to-zoom et le déplacement du slider de Progress saccadent ;
- l'écran entier se recompose à chaque micro-changement (overlay, Progress, sauvegarde en base), alors que 100 % des frames janky sont dues au thread UI, et non au GPU.

Par ailleurs, `PlayerViewModel` et `PlayerContract` sont difficiles à lire : un état monolithique fusionné à partir de deux états intermédiaires, des intents morts et des allers-retours UI ↔ VM inutiles.

## Solution

Rendre l'écran du lecteur fluide sans changer son apparence ni son comportement fonctionnel :

- découper l'état exposé à l'écran par fréquence de changement, pour que chaque changement ne recompose que la partie concernée ;
- traiter les gestes (luminosité, volume, scrubbing) avec le moins d'allers-retours possible, au plus une fois par frame ;
- garder l'Interface composée au lieu de la recréer à chaque tap, en conservant exactement les mêmes animations ;
- supprimer les blocages du main thread et les états incohérents de `AndroidPlayerManager` ;
- simplifier le contrat du lecteur (moins de types, moins d'intents, noms alignés sur le glossaire).

Les modifications restent ciblées : on ne touche que ce qui sert la performance de l'interface et la lisibilité de `PlayerViewModel` et `PlayerContract`.

## User Stories

1. En tant qu'utilisateur, je veux que l'Interface apparaisse instantanément quand je tape sur la vidéo, afin de ne pas percevoir de saccade.
2. En tant qu'utilisateur, je veux que l'Interface disparaisse avec la même animation de fondu qu'aujourd'hui, afin que l'expérience visuelle ne change pas.
3. En tant qu'utilisateur, je veux que les boutons de lecture apparaissent avec le même effet de rebond qu'aujourd'hui, afin de retrouver l'identité visuelle actuelle.
4. En tant qu'utilisateur, je veux que le Chapter button glisse vers le haut quand l'Interface apparaît et redescende quand elle disparaît, comme aujourd'hui.
5. En tant qu'utilisateur, je veux qu'aucun bouton de l'Interface masquée ne réagisse à mes taps, afin de ne pas déclencher d'action invisible.
6. En tant qu'utilisateur, je veux que la barre de progression ondulée garde le même rendu à chaque apparition de l'Interface.
7. En tant qu'utilisateur, je veux que la barre ondulée ne consomme rien quand l'Interface est masquée, afin de préserver la batterie pendant la lecture.
8. En tant qu'utilisateur, je veux régler la luminosité en glissant verticalement à gauche sans latence perceptible.
9. En tant qu'utilisateur, je veux régler le volume en glissant verticalement à droite sans latence perceptible.
10. En tant qu'utilisateur, je veux voir l'Ambient overlay suivre mon doigt en temps réel pendant un geste de volume ou de luminosité.
11. En tant qu'utilisateur, je veux que l'Ambient overlay disparaisse automatiquement 1 seconde après la fin de mon geste, comme aujourd'hui.
12. En tant qu'utilisateur, je veux voir l'image de la vidéo bouger pendant que je fais glisser le slider de Progress, afin de trouver le passage que je cherche.
13. En tant qu'utilisateur, je veux que le curseur et le temps affiché suivent exactement mon doigt pendant le glissement, sans revenir en arrière.
14. En tant qu'utilisateur, je veux que la lecture reprenne exactement à la position où j'ai relâché le slider.
15. En tant qu'utilisateur dont la vidéo est décodée en logiciel (FFmpeg), je veux que le scrubbing reste réactif, sans geler le décodeur.
16. En tant qu'utilisateur, je veux que le zoom à deux doigts (passer en plein écran ou revenir) soit fluide pendant toute l'animation.
17. En tant qu'utilisateur, je veux que le lancement d'un Media et le changement de Track ne figent pas l'écran.
18. En tant qu'utilisateur, je veux que l'état lecture/pause affiché corresponde toujours à l'état réel du lecteur.
19. En tant qu'utilisateur, je veux que la Track audio ou de sous-titres sélectionnée affichée dans la Settings sheet corresponde toujours à la Track réellement jouée.
20. En tant qu'utilisateur, je veux que la sauvegarde périodique de ma Progress ne provoque aucune saccade.
21. En tant qu'utilisateur, je veux que le Seek overlay continue de cumuler les sauts successifs (double-tap répété), comme aujourd'hui.
22. En tant qu'utilisateur, je veux que le Chapter button « épisode suivant » apparaisse toujours à la fin d'un Episode, sans que l'écran ralentisse dans les dernières secondes.
23. En tant qu'utilisateur, je veux que le passage à l'Episode suivant ne coupe jamais la lecture du nouvel Episode.
24. En tant qu'utilisateur, je veux qu'en revenant sur le lecteur depuis un autre lecteur, la lecture ne soit pas interrompue par le nettoyage de l'ancien écran.
25. En tant qu'utilisateur, je veux que le passage en Picture-in-Picture, l'arrière-plan et le retour au premier plan se comportent exactement comme aujourd'hui.
26. En tant qu'utilisateur, je veux que l'Interface se masque automatiquement après quelques secondes d'inactivité, comme aujourd'hui.
27. En tant qu'utilisateur clavier ou télécommande, je veux que espace, gauche/droite et les touches média fonctionnent comme aujourd'hui.
28. En tant que développeur, je veux que `PlayerContract` n'expose que des états et des intents réellement utilisés, afin de comprendre le lecteur rapidement.
29. En tant que développeur, je veux que l'état du lecteur soit découpé par responsabilité, afin de savoir quel composable dépend de quoi.
30. En tant que développeur, je veux que le vocabulaire du contrat suive le glossaire (Progress, Interface, Seek overlay, Ambient overlay, Chapter button, Settings sheet, Track).
31. En tant que développeur, je veux une seule instance de `AndroidPlayerManager` dans l'application, afin d'éviter plusieurs `MediaController` concurrents sur le même `PlayerService`.
32. En tant que développeur, je veux que `PlayerViewModelTest` décrive le nouveau contrat, afin que les régressions de comportement soient détectées.

## Implementation Decisions

La livraison est découpée en **trois lots successifs**, une PR par lot, dans cet ordre. Le développeur mesure les performances entre chaque lot.

### Lot A : corrections sûres, sans changement du contrat

- **C3 (zoom)** : l'échelle animée de la vidéo est lue dans la phase de dessin (bloc `graphicsLayer { }`), pas en composition. L'animation ne recompose plus le contenu du lecteur.
- **C5 (main thread)** : le libellé par défaut des Track (« Piste #n ») n'est plus lu de façon synchrone dans `onTracksChanged`. Il est résolu une fois, de façon asynchrone, lors du lancement du Media (`playMedia` est déjà suspendante), puis réutilisé.
- **C8 (cohérence d'état)** : toutes les mises à jour d'état de `AndroidPlayerManager` partent de la valeur courante de l'état, et non d'un instantané capturé avant. Dans `onEvents`, la mise à jour de lecture/pause et celle de la durée sont fusionnées en une seule émission. Même règle pour `selectTrack` et `onTracksChanged`.
- **C9 + C10 (cycle de vie)** : `AndroidPlayerManager` devient une instance unique (`single` Koin au lieu de `factory`). Le contrôle par identifiant de session existant empêche le nettoyage d'un ancien écran de couper la lecture du nouveau. L'état `Connecting` est réellement positionné pendant la connexion au `MediaController`, pour qu'un double `connect` soit ignoré. `disconnect` ramène le manager à un état propre (jobs annulés, état `Idle`).
- **C11 (épisode suivant)** : la recherche de l'Episode suivant n'est plus relancée à chaque tick de Progress dans les 15 dernières secondes. Le résultat est mis en cache pour le Media en cours.
- **C7 (petits défauts Compose)** :
  - l'Ambient overlay n'écrit plus d'état pendant la composition ;
  - les dégradés du Seek overlay sont mémorisés ;
  - la barre du haut n'applique plus le modifier parent (ni le padding de barre de statut) une seconde fois ;
  - les détecteurs de gestes utilisent la dernière version de la fonction d'envoi d'intent (`rememberUpdatedState`) au lieu de capturer la première.

### Lot B : découpage de l'état et simplification du contrat (C1)

- L'état monolithique (`PlayerUiState` / `PlayerUiContent`) est remplacé par **plusieurs flux d'état exposés par le VM**, groupés par fréquence de changement :
  - **session** : Media, player, Track disponibles, Track audio et sous-titres sélectionnées, durée, valeurs de saut arrière/avant. Change rarement. Porte aussi le statut de chargement/erreur.
  - **isPlaying** : lecture ou pause.
  - **interface** : visibilité de l'Interface, Settings sheet ouverte, mode Picture-in-Picture.
  - **overlays** : Seek overlay, Chapter button. Change souvent.
  - **progress** et **subtitles** : déjà séparés, inchangés.
- Chaque flux est collecté le plus bas possible dans l'arbre de composables, ou transmis sous forme de lambda, pour qu'un changement ne recompose que ses lecteurs.
- L'Artwork complet (`FullArtwork`) **sort de l'état exposé**. Le VM ne s'en sert qu'en interne pour résoudre le Media et ne réagit qu'à un changement de Media. Une sauvegarde de Progress en base ne provoque donc plus aucune émission vers l'écran.
- Les types intermédiaires `PlayerDataState` et `PlayerUserState`, ainsi que la fusion qui les combinait, sont supprimés.
- **Intents supprimés** :
  - `PlayMedia` : le VM lance déjà la lecture tout seul à chaque changement de Media ;
  - `SaveTime` : jamais envoyé ; la sauvegarde reste interne ;
  - `UpdateAmbientOverlay` (voir lot C).
- **Intent renommé** : `UpdateProgress` devient `SeekTo(progress)`.
- **Glossaire** : Progress désigne à la fois la position de lecture en cours et la position mémorisée pour la reprise. Le terme « Position » n'est pas utilisé. La colonne Room `currentTime` n'est pas renommée.

### Lot C : interactions

- **C2 (gestes verticaux)** :
  - la **luminosité** est appliquée entièrement côté UI (c'est un attribut de fenêtre, pas du lecteur), sans passer par le VM. L'événement `ChangeBrightness` est supprimé ;
  - le **volume** passe toujours par le VM, car il agit sur le `MediaController`. La valeur résultante revient à l'UI pour l'affichage ;
  - l'**Ambient overlay** devient un état local de l'écran, alimenté par les deux gestes, avec masquage automatique après 1 seconde d'inactivité. Il sort des overlays du VM ;
  - les déplacements du doigt sont **cumulés et appliqués au plus une fois par frame**, au lieu d'une fois par événement pointer.
- **C6 (scrubbing)** :
  - pendant le glissement, le curseur et le temps affiché suivent l'état local du doigt, pas la Progress du lecteur ;
  - un seek est envoyé **au plus toutes les ~150 ms**, en mode rapide (image clé la plus proche, `CLOSEST_SYNC`) ;
  - au relâchement, un seek **exact** est envoyé sur la position finale ;
  - `PlayerManager.seekTo` reçoit donc une notion de précision (rapide ou exacte). La limitation de fréquence est faite côté UI, puisque c'est une contrainte du geste ;
  - la plage de valeurs du slider n'est plus réallouée à chaque recomposition.
- **C4 (apparition de l'Interface)** :
  - l'Interface **reste composée** en permanence. L'apparition et la disparition sont rejouées par une transition sur la visibilité, appliquée en phase de dessin (alpha, échelle) ;
  - les specs d'animation sont **strictement identiques** à celles d'aujourd'hui : fondu en spring de faible raideur, apparition à rebond moyen des boutons de lecture, disparition par réduction, glissement animé de la marge du Chapter button. Si une animation ne peut pas être reproduite à l'identique, l'Interface concernée garde son comportement actuel ;
  - quand l'Interface est masquée, ses éléments ne reçoivent plus les clics ni le focus ;
  - seule la barre de progression ondulée n'est composée que lorsque l'Interface est visible ou en train d'apparaître. Elle est donc recréée à chaque apparition, comme aujourd'hui, et ne s'anime plus en arrière-plan ;
  - la mesure de la hauteur de la barre de progression, qui sert au positionnement du Chapter button, n'a plus lieu qu'au premier layout, puisque l'Interface n'est plus recréée.

## Testing Decisions

- **Une seule interface de test** : l'API publique de `PlayerViewModel` (envoi d'intents et flux d'état exposés), avec `PlayerManager` simulé, comme le fait aujourd'hui `PlayerViewModelTest` (Kotest `FunSpec`, MockK, Turbine, `FakeObserveArtworkUseCase`, mockups de Media). Aucune nouvelle interface de test n'est créée.
- Un bon test vérifie le **comportement observable** : l'état émis après un intent, les appels faits au `PlayerManager` et les sauvegardes de Progress. Il ne vérifie pas la structure interne du VM.
- `PlayerViewModelTest` est réécrit pour le nouveau contrat. Les comportements existants restent couverts : visibilité de l'Interface, Settings sheet, saut arrière/avant et cumul du Seek overlay, sélection de Track, Chapter button (passer un chapitre, épisode suivant), retour, arrière-plan, premier plan et Picture-in-Picture, sauvegarde de Progress.
- Nouveaux cas au niveau du VM :
  - une ré-émission de l'Artwork sans changement de Media ne produit **aucune** nouvelle émission de l'état session ;
  - un changement d'overlay ne produit pas d'émission de l'état session ni de l'état interface ;
  - la recherche de l'Episode suivant n'est faite qu'une fois par Media, malgré plusieurs ticks de Progress en fin de Media ;
  - `SeekTo` transmet la précision demandée au `PlayerManager`.
- `KoinModulesTest` doit continuer à passer après le passage de `AndroidPlayerManager` en `single`.
- `AndroidPlayerManager` (C5, C8, C9, C10) et les composables (C2 côté UI, C3, C4, C6, C7) n'ont **pas** de nouveau test automatisé. Ils sont vérifiés à la main sur appareil et par les mesures du développeur.
- Commande de vérification à la fin de chaque lot : `./gradlew :app:testFossDebugUnitTest`.

## Out of Scope

- La mesure des performances (`dumpsys gfxinfo`, Perfetto, build release ou benchmark) et la définition d'objectifs chiffrés : le développeur s'en charge.
- La mémoire graphique (Graphics PSS de 208 MB) : elle relève des buffers de surface et du codec, pas de l'UI.
- Les baseline profiles et la configuration R8.
- Le renommage de la colonne Room `currentTime` et toute migration de base.
- Les miniatures de prévisualisation pendant le scrubbing.
- Toute refonte des composables du lecteur au-delà de ce qui est nécessaire à la performance.
- Les écrans hors lecteur.

## Further Notes

- Le rapport d'origine a été mesuré sur un build debug, donc les chiffres absolus sont gonflés. Les causes restent valables en release.
- Deux causes ont été ajoutées pendant le grilling : C10 (état `Connecting` jamais positionné) et C11 (recherche de l'Episode suivant répétée à chaque tick de Progress).
- Avec le `factory` actuel, l'ancien écran pouvait arrêter la lecture du nouveau en se nettoyant, puisque plusieurs `MediaController` pilotent le même `PlayerService`. C'est la raison principale du passage en `single`, au-delà de la fuite du scope.
- Le glossaire (`GLOSSARY.md`) a été mis à jour pendant la session pour la définition de Progress.
