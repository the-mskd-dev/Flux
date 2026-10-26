# Spec : Synchro du Catalogue

## Problem Statement

La **Synchro** aligne le **Catalogue** sur les fichiers des **Sources** et les données TMDB. Elle fonctionne aujourd'hui, mais elle est difficile à lire et à faire évoluer, et elle a plusieurs défauts visibles par l'utilisateur :

- Une **Synchro complète** efface tout le Catalogue avant de le reconstruire. Les données des fichiers d'une **Source indisponible** sont donc perdues, ce qui contredit la règle selon laquelle elles doivent être conservées.
- La reprise de la progression de lecture après une Synchro complète repose sur le nom du fichier. Un fichier rattaché à un autre Artwork ou portant le même nom qu'un autre peut perdre ou voler la progression.
- Un échec réseau sur un fichier le transforme en **Fichier non reconnu** définitif : la **Synchro légère** suivante le considère connu et ne le retente jamais.
- Une seule erreur pendant la récupération des durées ou des traductions peut arrêter toute la Synchro.
- La **Progression de synchro** est fausse : certains travaux ne sont pas comptés, elle atteint 100 % bien avant la fin.
- La Synchro fait beaucoup d'appels TMDB inutiles (une traduction par épisode, un second passage pour les credits) et une requête par fichier pour vérifier les **Fichiers manquants** sur les Sources custom, ce qui la rend lente sur les gros catalogues.
- Ajouter une nouvelle donnée TMDB demande aujourd'hui de toucher le use case, plusieurs fetchers et le calcul de progression à la main.

## Solution

La Synchro devient un pipeline d'**Étapes de synchro** globales, dont l'Étape catalogue applique à chaque Artwork une chaîne d'**Enrichissements**. Ajouter une donnée TMDB revient presque toujours à ajouter un Enrichissement.

Pour l'utilisateur :

- La Synchro légère ne traite que les nouveaux fichiers et les **Fichiers en échec** ; la Synchro complète récupère à nouveau les données TMDB de tous les fichiers. Aucune des deux n'efface le Catalogue : elles mettent à jour, puis retirent les Fichiers manquants.
- Les données d'une Source indisponible sont toujours conservées ; celles d'un fichier absent d'une Source disponible sont supprimées.
- La progression de lecture, le statut vu/à voir et le statut privé sont conservés quelle que soit la Synchro.
- Le Catalogue se remplit au fur et à mesure de la Synchro, Artwork par Artwork, et ce qui a été récupéré reste acquis si la Synchro est interrompue.
- La Progression de synchro avance régulièrement, ne recule jamais et atteint 100 % à la fin.
- La Synchro est plus rapide : moins d'appels TMDB, appels groupés, concurrence bornée, aucune requête par fichier pour détecter les Fichiers manquants.

## User Stories

1. As a user, I want the first Synchro to fetch TMDB data for all my files, so that my Catalogue is complete from the start.
2. As a user, I want later Synchros to only fetch data for new files, so that they are fast.
3. As a user, I want to request a Synchro complète, so that I can refresh all TMDB data on demand.
4. As a user, I want a Synchro complète to keep my Catalogue visible while it runs, so that I can keep browsing.
5. As a user, I want the data of a Source indisponible to be kept, so that unplugging an SD card or losing access to a folder does not erase my Catalogue.
6. As a user, I want the data of a Source indisponible to be kept during a Synchro complète too, so that a refresh never erases files I cannot currently reach.
7. As a user, I want the data of a file removed from an available Source to be deleted, so that my Catalogue only shows files I can play.
8. As a user, I want Seasons and Artworks left without any Média to be removed, so that I do not see empty entries.
9. As a user, I want Fichiers manquants to disappear as soon as the Sources are scanned, so that the Catalogue is cleaned even if the rest of the Synchro is interrupted.
10. As a user, I want my playback progress to be kept after any Synchro, so that I can resume where I stopped.
11. As a user, I want my watched / to-watch status to be kept after any Synchro, so that my history stays correct.
12. As a user, I want my playback progress to follow the file even if TMDB now identifies it as another Artwork, so that a re-identification does not lose my progress.
13. As a user, I want two files with the same name in different folders to keep their own progress, so that one does not overwrite the other.
14. As a user, I want my Artworks privés to stay private after any Synchro, so that the private folder is never exposed by a refresh.
15. As a user, I want NSFW Artworks to be moved to the private folder after a Synchro when that option is enabled, so that new NSFW content is hidden too.
16. As a user, I want a file that failed because of the network to be retried at the next Synchro, so that a temporary outage does not leave it unidentified forever.
17. As a user, I want a file that failed because no TMDB token is available to be retried once a token is set, so that it gets identified later.
18. As a user, I want a file that TMDB cannot identify to still appear in my Catalogue as a Fichier non reconnu, so that I can still play it.
19. As a user, I want a failure on one file or one Artwork not to stop the Synchro, so that the rest of my Catalogue is still updated.
20. As a user, I want Artworks to appear in the Catalogue as soon as they are fetched, so that I do not wait for the whole Synchro to see new content.
21. As a user, I want the work already done to be kept when a Synchro is interrupted, so that the next Synchro has less to do.
22. As a user, I want to see the Progression de synchro as a percentage, so that I know how long it will take.
23. As a user, I want the Progression de synchro to never go backwards, so that it feels reliable.
24. As a user, I want the Progression de synchro to reach 100 % only when the Synchro ends, so that it is truthful.
25. As a user, I want the Synchro to be as fast as possible, so that my new files are available quickly.
26. As a user, I want the Synchro to use as little data as possible, so that it does not drain my mobile plan.
27. As a user, I want titles and descriptions in my language, with an English fallback, so that I always have readable information.
28. As a user, I want episode titles and descriptions in my language without the Synchro becoming slow on long seasons, so that big shows sync quickly.
29. As a user, I want the cast of new Artworks to be available right after the Synchro, so that I can browse it without waiting for a separate refresh.
30. As a user, I want the duration of each Média to be known, so that progress bars are correct.
31. As a user, I want a light Synchro requested during a Synchro complète to be ignored, so that the complete one is not interrupted.
32. As a user, I want a Synchro complète requested during a light Synchro to replace it, so that I get the full refresh I asked for.
33. As a user, I want genres to be refreshed during a Synchro complète, so that genre filters stay up to date.
34. As a user, I want images to be prefetched after the Synchro without delaying its end, so that browsing is smooth.
35. As a user, I want the app to remember when the last Synchro happened and with which app version, so that a Synchro complète is triggered when an update requires it.
36. As a user with thousands of files, I want the Synchro not to crash or stall, so that large libraries are supported.
37. As a user with a large library, I want the Synchro not to flood TMDB with simultaneous calls, so that I am not rate-limited and calls do not time out.
38. As a developer, I want to add a new TMDB data by adding one Enrichissement, so that the Synchro stays easy to extend.
39. As a developer, I want each Étape and Enrichissement to declare its own work for the Progression, so that the percentage stays correct when new ones are added.
40. As a developer, I want the difference between Synchro légère and complète to be limited to the set of files to fetch, so that both modes share the same code path.
41. As a developer, I want behaviour tests that encode these rules, so that I can refactor the Synchro without regressions.

## Implementation Decisions

**Modes**
- Deux modes : Synchro légère et Synchro complète. La seule différence est l'ensemble des fichiers envoyés à l'Étape catalogue : nouveaux fichiers et Fichiers en échec pour la légère, tous les fichiers pour la complète. L'Étape genres ne s'exécute qu'en Synchro complète.
- Plus aucun effacement global du Catalogue : toutes les Synchros mettent à jour par upsert puis retirent les Fichiers manquants.
- Règles de concurrence inchangées : une Synchro complète annule une Synchro légère en cours ; une Synchro légère demandée pendant une Synchro complète est ignorée.

**Étapes de synchro (dans l'ordre)**
1. **Scan** : interroge chaque Source et retourne les fichiers trouvés et la disponibilité de chaque Source. Chaque Source indique elle-même si elle est disponible.
2. **Fichiers manquants** : les fichiers existants sont les fichiers vus au scan plus les fichiers du Catalogue appartenant à une Source indisponible. Tout fichier du Catalogue hors de cet ensemble est un Fichier manquant et ses données sont supprimées (avec les Saisons et Artworks devenus vides). Aucune requête par fichier.
3. **Genres** (Synchro complète uniquement).
4. **Catalogue** : regroupe les fichiers à traiter par dossier (titre + année) et applique à chaque dossier, en parallèle, la chaîne d'Enrichissements, puis persiste l'Artwork, ses Saisons et ses Médias dans une transaction.
5. **NSFW privé** : si le dossier privé est activé avec l'option NSFW, place les Artworks NSFW en privé.
6. **Finalisation** : enregistre l'heure de Synchro et le code de version ; lance le préchargement des images en arrière-plan sans bloquer la fin.

**Enrichissements (par dossier, dans l'Étape catalogue)**
- **Identification** : recherche TMDB (film ou série selon le fichier).
- **Détails** : un seul appel de détails par Artwork avec `append_to_response=translations,credits`, pour films et séries. Les traductions remplacent les appels de traduction séparés ; les credits sont enregistrés avec l'Artwork, sans second passage.
- **Saisons** : pour chaque saison, deux appels de détails de saison, dans la langue de l'utilisateur et en anglais. Les titres et descriptions d'épisodes vides dans la langue de l'utilisateur sont complétés par l'anglais. Plus aucun appel de traduction par épisode.
- **Durées** : durée lue sur le fichier quand TMDB ne la fournit pas.
- Un Enrichissement en échec n'interrompt jamais les autres dossiers.

**Résultat de récupération**
- Chaque dossier aboutit à un résultat typé : trouvé, introuvable (Fichier non reconnu) ou échec (Fichier en échec).
- Un échec (réseau, API, token TMDB absent) n'est pas persisté : le fichier n'est pas dans le Catalogue et sera retenté à la Synchro suivante, sans backoff ni compteur.
- Seul « introuvable » produit un Média sans Artwork connu.

**Persistance**
- Persistance progressive : chaque dossier est enregistré dans une transaction dès qu'il est récupéré.
- L'upsert n'écrit que les colonnes issues de TMDB. L'état utilisateur (temps de lecture courant, statut) est rattaché au chemin du fichier : lors de l'upsert, il est repris depuis la ligne existante de même chemin, même si l'identité TMDB du Média a changé.
- Le statut privé d'un Artwork est conservé par l'upsert.
- Le mécanisme actuel de sauvegarde/restauration des Artworks privés et de reprise de progression par nom de fichier disparaît.

**Progression de synchro**
- Dès la fin du scan, chaque Étape et chaque Enrichissement déclare un nombre estimé d'unités, déduit des noms de fichiers (dossiers, couples titre/saison, fichiers).
- Progression = unités faites / unités déclarées au total ; toutes les unités ont le même poids (≈ un appel).
- Une Étape peut réajuster son estimation en cours de route ; la Progression exposée ne recule jamais et n'atteint 100 % qu'à la fin.
- `SyncState` peut exposer le nombre de Fichiers en échec de la dernière Synchro (pour un affichage futur).

**Concurrence**
- Un sémaphore unique, partagé par tous les Enrichissements, borne les appels TMDB simultanés (8 à 10). Il remplace le dispatcher à parallélisme limité, qui borne les threads et non les requêtes en vol.

**People**
- Les credits des Artworks traités arrivent avec l'Enrichissement Détails. Le rafraîchissement périodique des people (tous les 7 jours) reste séparé de la Synchro.

**Code supprimé**
- Le use case de nettoyage du Catalogue (non appelé, et ne respectant pas la règle des Sources indisponibles).
- Le use case de filtrage des fichiers existants et la méthode correspondante des deux sources de fichiers (dont la requête MediaStore `IN (…)` qui dépasse la limite SQLite au-delà de 999 fichiers).
- Les fetchers d'Artworks, de films et de saisons, le resolver de médias, l'agrégateur de contenu et la fonction de reprise de progression : remplacés par les Étapes et Enrichissements.

**Livraison** (commits successifs sur la branche, chacun laissant l'app fonctionnelle)
1. Tests de comportement
2. Persistance : plus d'effacement global, état utilisateur par chemin, Fichiers manquants calculés depuis le scan, Fichiers en échec non persistés
3. Pipeline Étapes / Enrichissements et Progression de synchro
4. Appels groupés (`append_to_response`, saison langue + anglais), sémaphore, credits intégrés
5. Suppression du code mort

## Testing Decisions

- **Un bon test** vérifie le comportement observable d'une Synchro : contenu du Catalogue en base, valeurs successives de `state`, nombre d'appels TMDB par endpoint. Il ne vérifie jamais quelle Étape ou quel Enrichissement a été appelé, ni dans quel ordre interne.
- **Seam unique** : le use case de Synchro (lancer une Synchro légère ou complète, observer `state`). Étapes et Enrichissements ne sont pas testés individuellement.
- **Dépendances remplacées** :
  - **Sources** : fausse Source en mémoire (fichiers + disponibilité), via le fournisseur de Sources existant.
  - **TMDB** : Ktor `MockEngine` au niveau HTTP, qui compte les appels par endpoint et renvoie des fixtures JSON. Nécessite d'ajouter `ktor-client-mock` en dépendance de test.
  - **Base** : Room en mémoire avec `BundledSQLiteDriver` (déjà au catalogue de versions) dans les tests JVM. À valider dès le premier commit dans `testFossDebugUnitTest` ; à défaut, repli sur un faux dépôt de base de données en mémoire et tests DAO de l'upsert partiel en `androidTest`.
  - **Coordinator** : `FakeCatalogSyncCoordinator` existant ; datastores en mémoire.
- **Cas couverts** : Synchro légère vs complète ; Source indisponible conservée (y compris en complète) ; Fichier manquant supprimé avec Saisons/Artworks vides ; Fichier en échec non persisté puis retenté ; token absent traité comme échec ; Fichier non reconnu persisté ; état utilisateur conservé par chemin, y compris après ré-identification ; statut privé conservé ; NSFW placé en privé ; échec d'un dossier sans impact sur les autres ; Progression croissante jusqu'à 100 % à la fin ; nombre d'appels TMDB attendu (détails groupés, deux appels par saison, aucun appel de traduction par épisode) ; règles de concurrence entre Synchros.
- **Prior art** : le test actuel du use case de Synchro (kotest `FunSpec`, Turbine pour `state`, `StandardTestDispatcher`, `FakeCatalogSyncCoordinator`), qui sera remplacé par les tests de comportement. Les tests DAO existants en `androidTest` servent de référence pour Room.
- Plusieurs tests seront rouges au premier commit (bugs actuels) et passeront au vert au fil des commits suivants.

## Out of Scope

- Le changement de langue du Catalogue reste dans son use case actuel. La conception des Enrichissements doit permettre d'en faire plus tard un troisième mode de Synchro (tous les fichiers, Enrichissements de traduction uniquement).
- Le rafraîchissement périodique des people.
- Tout changement d'UI : l'affichage de la Progression de synchro et du nombre de Fichiers en échec reste tel quel.
- L'algorithme de regroupement des fichiers en dossiers et l'analyse des noms de fichiers.
- Le backoff ou la limitation des retries des Fichiers en échec.

## Further Notes

- Vocabulaire : voir `GLOSSARY.md` (Synchro légère / complète, Source indisponible, Fichier manquant, Fichier en échec, Fichier non reconnu, Étape de synchro, Enrichissement, Progression de synchro).
- Deux décisions difficiles à défaire pourraient faire l'objet d'un ADR : l'abandon de l'effacement global en Synchro complète et le rattachement de l'état utilisateur au chemin du fichier.
- La taille exacte du sémaphore (8 à 10) est à ajuster après mesure sur un gros catalogue.
