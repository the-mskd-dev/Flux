## Important

- Je souhaite améliorer les performances pour PlayerScreen (et ses sous composants), PlayerViewModel, PlayerContract et AndroidPlayerManager

- Je ne veux pas modifier les fichiers en profondeur mais seulement ce qu'il est nécessaire pour améliorer les performances de l'interface (ex: recomposition, thread, rafraichissement des données).

- Je voudrais également simplifier PlayerViewModel et PlayerContract pour que cela soit plus lisible

 

# 0. Notes à lire avant planification

## 0.1 Constat (mesures)

| Métrique                  | Valeur                            | Lecture                                                 |

| ------------------------- | --------------------------------- | ------------------------------------------------------- |

| Frames                    | 1391                              | —                                                       |

| Janky                     | 3.74 % (legacy 14.23 %)           | modéré                                                  |

| P50 / P90 / \*\*P95 / P99\*\* | 6 / 13 / \*\*21 / 150 ms\*\*          | pics ponctuels très lourds                              |

| \*\*Slow UI thread\*\*        | \*\*52\*\* (= 100 % des janky frames) | coût CPU côté composition/layout, pas GPU               |

| GPU P99                   | 5 ms                              | le GPU n'est \*\*pas\*\* en cause                           |

| \*\*High input latency\*\*    | \*\*1812\*\* (&amp;gt; nb de frames)      | file d'input saturée (gestes drag / slider)             |

| Graphics PSS              | 208 MB (EGL 106 + GL 101)         | élevé pour un écran sans image                          |

| AppContexts / Activities  | 7 / 1                             | à vérifier, pas forcément une fuite                     |

&gt; ⚠️ Build \*\*debug\*\* (pas de R8, pas de baseline profile) : les chiffres absolus sont gonflés. Les causes ci‑dessous restent valables en release.

## 0.2 Causes identifiées (par ordre d'impact estimé)

\*\*C1 — État UI monolithique → recomposition globale à chaque micro‑changement\*\* *(Slow UI thread, input latency)*

- `PlayerViewModel.uiState` combine artwork + settings + player + `_userState` en un seul `PlayerUiContent`. Tout changement (overlay, `seekOverlay`, `ambientOverlay`, `chapterButton`, `isPlaying`, ré‑émission Room de `FullArtwork`) produit une nouvelle instance.

- `PlayerScreen` lit `uiState` au niveau racine → recompose `AnimatedContent`, `PlayerSideEffects` (lambda `isPlayingContent` recréée car elle capture `content`), `PlayerContent`, `PlayerInterface`, `PlayerSettings`, et force la re‑mesure des \*\*deux `ConstraintLayout`\*\* (`PlayerScreen.kt:214`, `PlayerInterface.kt:54`).

- `fullArtwork` est dans `PlayerUiContent` alors qu'aucun composable du player ne le lit : chaque écriture Room (ex. `saveTime`) recompose tout l'écran.

\*\*C2 — Gestes verticaux (volume/luminosité) : 1 événement pointer = plusieurs allers‑retours\*\* *(input latency 1812)*

- `detectVerticalDragGestures` (`PlayerScreen.kt:262`) envoie un intent \*\*par événement pointer\*\* (60–120 Hz).

- Luminosité : `OnBrightnessChange` → `PlayerEvent.ChangeBrightness` → `window.attributes = …` (relayout de fenêtre, `Activity+Extensions.kt:52`) → `UpdateAmbientOverlay` → `_userState.update` → recomposition globale (C1). \*\*3 sauts par mouvement de doigt\*\*.

- Volume : `player.volume = …` sur le `MediaController` à chaque événement.

- `updateAmbientOverlay` / `updateSeekOverlay` font `cancel()` + `viewModelScope.launch` à chaque événement.

\*\*C3 — `graphicsLayer(scaleX = animatedScale)` lu en composition\*\* *(jank pendant le pinch‑to‑zoom)*

- `PlayerScreen.kt:283` : la valeur animée est lue dans la phase de composition → `PlayerContent` entier (et le `ConstraintLayout`) recompose \*\*à chaque frame\*\* du spring `StiffnessLow` (\~1 s).

\*\*C4 — Apparition de l'interface coûteuse\*\* *(pics P99 ≈ 150 ms)*

- `AnimatedVisibility` (`PlayerInterface.kt:48`) détruit/recrée tout le sous‑arbre à chaque tap : `ConstraintLayout` imbriqué, 3 FAB M3 Expressive avec ombres, `Slider` + `LinearWavyProgressIndicator`, 5 animations simultanées (fade, scale bouncy, 2× slide, animateDp).

- `onGloballyPositioned` → `seekBarHeight` (state) → \*\*2ᵉ passe de recomposition\*\* juste après l'apparition, puis `animateDpAsState` du `PlayerChapterButton`.

- `LinearWavyProgressIndicator` anime en continu tant que l'interface est visible (frames permanentes).

\*\*C5 — Blocage du main thread dans `AndroidPlayerManager`\*\* *(pics au lancement d'un média / changement de piste)*

- `onTracksChanged` : `runBlocking { getString(Res.string.track) }` (`AndroidPlayerManager.kt:407`) → lecture de ressource Compose \*\*synchrone sur le main thread\*\*.

\*\*C6 — Slider de progression : seek à chaque mouvement\*\*

- `PlayerSeekBar.kt:67` : `onValueChange` envoie `UpdateProgress` → `player.seekTo` à chaque événement de drag (flush décodeur en rafale + intents en masse).

- `LaunchedEffect(currentProgress)` relancé chaque seconde ; `valueRange` réalloué à chaque recomposition.

\*\*C7 — Petits anti‑patterns Compose\*\*

- `PlayerAmbientOverlay.kt:47` : \*\*écriture d'état pendant la composition\*\* (`value = overlay.value`) → recomposition supplémentaire.

- `PlayerSeekOverlay.kt:48` : 2 `Brush` alloués à chaque recomposition.

- `PlayerTobBar.kt:49` : le `modifier` parent (avec `layoutId`) est réappliqué au `Box` enfant + `statusBarsPadding` en double.

- `pointerInput(Unit)` capture le premier `sendIntent` (pas de `rememberUpdatedState`).

\*\*C8 — Bug de cohérence d'état (effet de bord perf)\*\*

- `AndroidPlayerManager.onEvents` (`:284` et `:293`) : deux `_state.update { currentState.copy(...) }` sur le \*\*même snapshot\*\* → la 2ᵉ écrase `isPlaying` de la 1ʳᵉ ; idem `selectTrack`/`onTracksChanged` qui ignorent la valeur courante de `update`. Provoque des émissions parasites et des états incohérents.

\*\*C9 — Ressources du `AndroidPlayerManager`\*\*

- `factory` Koin → une instance par `PlayerViewModel`, chacune avec un `CoroutineScope(SupervisorJob() + Main)` \*\*jamais annulé\*\* dans `disconnect`.

