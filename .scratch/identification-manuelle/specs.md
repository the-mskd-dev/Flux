# Spec : Identification manuelle

## Problem Statement

La Synchro identifie chaque Artwork en cherchant sur TMDB le titre et l'année extraits du nom des fichiers. Quand le nom est ambigu (remake, homonyme, version US/UK d'une série), le mauvais Artwork est choisi. Aujourd'hui, la seule façon de corriger est de renommer les fichiers pour y ajouter un TMDB ID. De plus, tout nouvel épisode ajouté plus tard serait de nouveau rattaché au mauvais Artwork.

## Solution

Une option **« Sélectionner les données »** (icône `ic_search`) dans le `FluxPopupMenu` de la page d'un Show et de la page d'un Movie ouvre une `FluxDialog`. Celle-ci liste les résultats de recherche TMDB (`searchShow` pour un Show, `searchMovie` pour un Movie). En choisir un réalise une **Identification manuelle** (voir `GLOSSARY.md`) : tous les Média de l'Artwork courant sont rattachés à l'Artwork choisi, et les nouveaux fichiers de même titre le suivent lors des Synchro légères.

Une option **« Réinitialiser »** permet de revenir à l'identification automatique. La Synchro complète efface aussi toutes les Identifications manuelles.

Décision d'architecture : `docs/adr/0001-identification-manuelle-par-titre-de-fichier.md`.

## Règles

### Disponibilité de l'option
- Présente sur la page d'un Show et d'un Movie, **absente** pour l'Unknown Artwork.
- **Masquée** sans token TMDB.
- **Masquée** si au moins un fichier de l'Artwork porte un TMDB ID dans son nom ou son dossier.
- **Désactivée** pendant une Synchro.

### Dialog
- Champ de recherche modifiable, prérempli avec le titre extrait du nom du premier fichier (pas le titre TMDB), recherche sans filtre d'année.
- Chaque résultat : Poster (2:3) à gauche avec un placeholder s'il est absent ; titre et année de parution à droite.
- L'Artwork actuel est marqué « actuel » et n'est pas sélectionnable.
- États : chargement, aucun résultat, erreur avec « Réessayer ».
- Pendant l'Identification manuelle, la dialog affiche un chargement.

### Identification manuelle
- Tous les Média de l'Artwork courant sont ré-identifiés vers l'Artwork choisi. Tous les Enrichissements sont rejoués : détails, saisons et épisodes, crédits, durées, Logo.
- L'Artwork obtenu est marqué comme identifié manuellement : un booléen sur `ArtworkEntity`, sans nouvelle table.
- **Conservé** : le Status et la Progress de chaque Média (par chemin de fichier) et l'historique (migré vers le nouvel Artwork, sinon perdu par la cascade de la FK `history`).
- **Private** : si l'Artwork source ou l'Artwork cible est Private, le résultat est Private.
- **Fusion** : si l'Artwork choisi existe déjà dans le Catalogue, les Média y sont fusionnés. Pour l'historique, l'entrée la plus récente est gardée.
- **Pas d'atomicité** :
  - une erreur transitoire (réseau, API, token) produit un Fichier en échec, retenté à la Synchro suivante ;
  - un épisode ou une saison **inexistant** dans l'Artwork choisi produit un Fichier non reconnu, qui part dans l'Unknown Artwork.
- L'ancien Artwork, devenu vide, est supprimé.
- La page se recharge sur le nouvel Artwork en remplaçant la destination de navigation : le retour ne ramène pas vers l'ancien ID.

### Synchro légère
- Ordre d'identification d'un fichier :
  1. TMDB ID dans le nom du fichier ou du dossier ;
  2. Identification manuelle : un Média connu de même (titre, année) de nom de fichier appartient à un Artwork identifié manuellement ;
  3. recherche par nom.
- Si tous les fichiers d'un Artwork identifié manuellement disparaissent, le choix est oublié.

### Synchro complète
- Efface toutes les Identifications manuelles et relance la recherche par nom. Le Status et la Progress sont conservés. L'historique garde le comportement existant (hors périmètre).

### Réinitialisation
- Option « Réinitialiser » dans le même menu, **uniquement** sur un Artwork identifié manuellement, avec les mêmes conditions de disponibilité. Elle demande une confirmation.
- Le marqueur est effacé et la recherche par nom est relancée pour chaque fichier. Les règles de Fichier en échec et de Fichier non reconnu s'appliquent ; Status, Progress et historique sont conservés.
- La page se recharge sur l'Artwork obtenu. Si les fichiers se répartissent sur plusieurs Artwork, l'utilisateur retourne au Catalogue.

## User Stories

1. As a user, I want to pick the right Movie from TMDB results when Flux identified the wrong one, so that my Catalogue shows the correct data.
2. As a user, I want to pick the right Show from TMDB results, so that all my episodes get the correct titles, seasons and images.
3. As a user, I want to edit the search text, so that I can find the Artwork even when my file is badly named.
4. As a user, I want to see each result's Poster, title and year, so that I can tell homonyms and remakes apart.
5. As a user, I want the current Artwork to be marked in the list, so that I know which one is already selected.
6. As a user, I want my statuses, progress and history to survive the change, so that I lose nothing.
7. As a user, I want a Private Artwork to stay private after the change, so that hidden content never appears in the public Catalogue.
8. As a user, I want my episodes to be merged into the chosen Show when it is already in my Catalogue, so that I do not get duplicates.
9. As a user, I want episodes that do not exist in the chosen Show to go to the Unknown Artwork, so that they stay visible and playable.
10. As a user, I want the page to show the new Artwork right after my choice, so that I see the result immediately.
11. As a user adding new episodes later, I want them to join the Artwork I chose, so that I do not have to fix them again.
12. As a user who wrote a TMDB ID in a file name, I want that ID to stay authoritative, so that the option is not offered and cannot contradict it.
13. As a user, I want to reset a manual choice, so that I can go back to the automatic identification.
14. As a user launching a Synchro complète, I want the whole Catalogue to be identified again from scratch, while keeping my progress.
15. As a user without a TMDB token, I want the option to be hidden, so that I am not offered something that cannot work.
16. As a user, I want the option to be disabled during a Synchro, so that the Synchro cannot overwrite my choice.
