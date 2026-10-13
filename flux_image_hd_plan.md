# Plan — Affichage HD Instantané dans FluxImage

## Contexte & Diagnostic

### Comportement attendu
- L'image SD s'affiche en placeholder immédiat.
- L'image HD s'affiche **instantanément depuis le cache** si elle a été préchargée.

### Comportement actuel
- L'image HD met du temps à apparaître, **même après prefetch**, car :
  1. `AndroidImageRequestFactory` configure `memoryCachePolicy(CachePolicy.DISABLED)` → le prefetch écrit uniquement sur disque, jamais en mémoire.
  2. `FluxImage` utilise `rememberAsyncImagePainter(urlHigh)` sans `placeholderMemoryCacheKey` → Coil relit le disque (décodage lent) au lieu de trouver l'image déjà décodée en mémoire.
  3. L'image SD (`AsyncImage`) et l'image HD (`rememberAsyncImagePainter`) font deux requêtes Coil indépendantes et séquentielles, au lieu que la HD soit déjà prête.

### Schéma du flux actuel (problématique)

```
Prefetch          ──→ DiskCache uniquement (memory disabled)
FluxImage affiche ──→ SD (depuis disk/network) → fade-in
                  ──→ HD (depuis disk → décodage lent) → fade-in tardif
```

### Schéma du flux cible (correct)

```
Prefetch          ──→ DiskCache ✅ + MemoryCache ✅ (mémoire activée)
FluxImage affiche ──→ SD (depuis memory ou disk)
                  ──→ HD depuis MemoryCache (quasi-instantané) ✅
```

---

## User Review Required

> [!IMPORTANT]
> La clé principale du fix est d'activer le **memory cache dans le prefetch** (`AndroidImageRequestFactory`). Cela augmentera la consommation mémoire puisque les images HD y seront stockées. Le `MemoryCache` est déjà configuré à 25% de la RAM disponible via `maxSizePercent(0.25)` dans `moduleImagesAndroid.kt`. C'est un trade-off acceptable pour une meilleure UX.

> [!IMPORTANT]
> Le `crossfade` sur l'image HD dans `FluxImage` est actuellement appliqué **toujours**, même quand l'image est déjà en mémoire (auquel cas il est invisible). Pour les images déjà en memory cache, `AsyncImagePainter` passera directement en état `Success` sans transition visible. Le `AnimatedVisibility(fadeIn)` qui entoure l'image HD restera, mais sera presque imperceptible si l'image est déjà prête.

> [!NOTE]
> Le `semaphore(2)` qui limite le prefetch à 2 requêtes simultanées reste inchangé. Ceci est volontaire pour ne pas saturer le réseau lors du prefetch en arrière-plan.

---

## Open Questions

> [!IMPORTANT]
> **Q1 — Ordre du prefetch SD vs HD** : Actuellement, `ImagesPrefetchManagerImpl` précharge d'abord **toutes les SD**, puis toutes les **HD** (si activé). Veux-tu conserver cet ordre, ou préférer un prefetch par paire (SD+HD pour chaque image avant de passer à la suivante) ?
>
> → Le plan propose de **conserver l'ordre actuel** (toutes SD d'abord) car les SD sont plus légères et s'affichent en premier dans l'UI.

> [!NOTE]
> **Q2 — `crossfade` sur la SD** : L'image SD dans `FluxImage` a `crossfade(true)`. Si elle est déjà en mémoire, ce crossfade sera aussi quasi-invisible. Ce comportement est correct et aucun changement n'est prévu de ce côté.

---

## Proposed Changes

### 1. `AndroidImageRequestFactory` — Fix principal

**Problème** : `memoryCachePolicy(CachePolicy.DISABLED)` empêche le prefetch de peupler la mémoire.

**Fix** : Activer `memoryCachePolicy(CachePolicy.ENABLED)`.

#### [MODIFY] AndroidImageRequestFactory.kt

```diff
 class AndroidImageRequestFactory(private val context: Context) : ImageRequestFactory {
     override fun build(url: String, onEnd: (String) -> Unit): ImageRequest {
         return ImageRequest.Builder(context = context)
             .data(url)
-            .memoryCachePolicy(CachePolicy.DISABLED)
+            .memoryCachePolicy(CachePolicy.ENABLED)
             .diskCachePolicy(CachePolicy.ENABLED)
             .listener(
                 onSuccess = { _, _ -> onEnd(url) },
                 onError   = { _, _ -> onEnd(url) },
                 onCancel  = { _    -> onEnd(url) },
             )
             .build()
     }
 }
```

**Pourquoi** : Avec `ENABLED`, après le prefetch, l'image sera présente dans le `MemoryCache`. Quand `FluxImage` demandera la même URL, Coil la trouvera instantanément en mémoire.

---

### 2. `FluxImage.kt` — Optimisation du chargement HD

**Problème** : `rememberAsyncImagePainter(urlHigh)` est une API simplifiée qui ne permet pas de configurer finement la requête. En particulier, elle ne définit pas de `placeholderMemoryCacheKey`, ce qui peut causer une re-requête inutile.

**Fix** : Remplacer `rememberAsyncImagePainter(urlHigh)` par un `rememberAsyncImagePainter` avec un `ImageRequest.Builder` complet, en ajoutant `memoryCacheKey(urlHigh)` explicitement et `crossfade(false)` (le crossfade est géré par `AnimatedVisibility`).

#### [MODIFY] FluxImage.kt

```diff
 @Composable
 fun FluxImage(
     modifier: Modifier = Modifier,
     path: String,
     contentDescription: String,
     contentScale: ContentScale = ContentScale.Crop,
     onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null
 ) {
     val context = LocalPlatformContext.current
     val urlLow = path.tmdbImage
     val urlHigh = path.tmdbImageLarge

-    val hdPainter = rememberAsyncImagePainter(urlHigh)
+    val hdPainter = rememberAsyncImagePainter(
+        model = ImageRequest.Builder(context)
+            .data(urlHigh)
+            .memoryCacheKey(urlHigh)
+            .diskCacheKey(urlHigh)
+            .crossfade(false)
+            .build()
+    )

     val hdState by hdPainter.state.collectAsState()

     Box(modifier = modifier) {

         AsyncImage(
             modifier = Modifier.matchParentSize(),
             model = ImageRequest.Builder(context)
                 .data(urlLow)
                 .crossfade(true)
                 .build(),
             placeholder = Image.placeholder,
             error = Image.error,
             contentDescription = contentDescription,
             contentScale = contentScale,
             onSuccess = onSuccess
         )

         AnimatedVisibility(
             modifier = Modifier.matchParentSize(),
             visible = hdState is AsyncImagePainter.State.Success,
             enter = fadeIn()
         ) {
             Image(
                 modifier = Modifier.fillMaxSize(),
                 painter = hdPainter,
                 contentDescription = contentDescription,
                 contentScale = contentScale
             )
         }

     }

 }
```

**Pourquoi** :
- `memoryCacheKey(urlHigh)` : garantit que Coil cherche dans le `MemoryCache` avec exactement cette clé (la même clé utilisée lors du prefetch).
- `diskCacheKey(urlHigh)` : même cohérence pour le disk cache.
- `crossfade(false)` sur le painter HD : le crossfade est déjà géré par `AnimatedVisibility(fadeIn())`. Activer les deux causerait une double animation.

---

### 3. `moduleImagesAndroid.kt` — Vérification des ImageLoaders

> [!NOTE]
> Les deux `ImageLoader` (`uiImageLoader` et `cacheImageLoader`) utilisent déjà `get<MemoryCache>()` et `get<DiskCache>()`, ce qui signifie qu'ils **partagent le même cache**. C'est correct et **aucune modification n'est nécessaire ici**.

Vérification de la cohérence actuelle :
- `uiImageLoader` : utilisé par `FluxApp.newImageLoader` → c'est le Singleton Coil
- `cacheImageLoader` : utilisé par `ImagesPrefetchManagerImpl` → prefetch en background
- Les deux partagent les mêmes instances `MemoryCache` et `DiskCache` ✅

---

### 4. `Constants.kt` — Aucune modification nécessaire

Les URLs TMDB sont correctement définies. `IMAGE = w300` pour la SD et `IMAGE_LARGE = original` pour la HD. Pas de changement.

---

### 5. `ImagesPrefetchManagerImpl.kt` — Aucune modification nécessaire

La logique de prefetch est correcte. Le seul problème était la politique de cache mémoire dans `AndroidImageRequestFactory`.

---

### 6. `NetworkImageInterceptor.kt` — Aucune modification nécessaire

Le comportement offline est correct.

---

### 7. `ImagesPrefetchManagerTest.kt` — Mise à jour des tests

Les tests existants doivent être revérifiés car la configuration du `AndroidImageRequestFactory` change. Les tests sont mockés et ne dépendent pas directement de `CachePolicy`, donc **aucun test n'est cassé**.

Cependant, une mise à jour du test pour valider `memoryCachePolicy(ENABLED)` n'est pas nécessaire car ce détail est dans l'implémentation Android (non testable en JVM sans Robolectric).

---

## Résumé des fichiers modifiés

| Fichier | Changement | Priorité |
|---|---|---|
| `AndroidImageRequestFactory.kt` | `DISABLED` → `ENABLED` pour `memoryCachePolicy` | 🔴 Critique |
| `FluxImage.kt` | Utiliser `ImageRequest.Builder` complet avec `memoryCacheKey` explicite | 🟡 Important |
| `moduleImagesAndroid.kt` | Aucune modification | ✅ |
| `ImagesPrefetchManagerImpl.kt` | Aucune modification | ✅ |
| `Constants.kt` | Aucune modification | ✅ |
| `FluxApp.kt` | Aucune modification | ✅ |

---

## Verification Plan

### Automated Tests

```bash
./gradlew test
```

Les tests existants dans `ImagesPrefetchManagerTest` ne sont pas impactés (les mocks sont indépendants de `CachePolicy`).

### Manual Verification

1. **Lancer l'app** avec une connexion réseau active.
2. **Naviguer sur l'écran principal** pour déclencher le prefetch.
3. **Attendre quelques secondes** que le prefetch HD se termine.
4. **Quitter et revenir** sur un écran avec des `FluxImage`.
5. **Résultat attendu** : Les images HD apparaissent instantanément ou avec un délai < 100ms, sans fade-in notable.
6. **Test offline** : Couper le réseau, naviguer → les images HD déjà cachées doivent toujours s'afficher instantanément.

### Indicateurs de succès
- `hdState` passe à `Success` immédiatement (avant même le 1er frame rendu) quand l'image est en MemoryCache.
- Aucun "flash" SD→HD visible pour les images préchargées.
- Le comportement de fallback SD→HD progressif fonctionne toujours pour les images non préchargées.

---

## Checklist d'implémentation

- [x] **Step A** — `AndroidImageRequestFactory.kt` : `memoryCachePolicy(CachePolicy.ENABLED)`
- [x] **Step B** — `FluxImage.kt` : `rememberAsyncImagePainter` avec `ImageRequest.Builder` complet
- [x] **Step C** — Vérification build + `./gradlew test`
