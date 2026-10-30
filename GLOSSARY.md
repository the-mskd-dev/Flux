# Flux

Flux organise les UserFile de l'utilisateur en Catalog (enrichi via TMDB) et les lit en mémorisant leur Progress.

## Modèles

**Artwork**:
Film ou série tel qu'identifié sur TMDB, porteur des métadonnées, indépendamment des Media qui y sont rattachés.
_Avoid_: Œuvre, Content, Title, Media

**ContentType**:
Nature d'un Artwork : MOVIE (film) ou SHOW (série).
_Avoid_: Media type, catégorie

**Show**:
Artwork de type SHOW.
_Avoid_: Série, TV show

**Unknown Artwork**:
Show spécial qui regroupe, sous forme d'Episode, les Media dont le UserFile n'a pu être associé à aucun Artwork TMDB, y compris les films.
_Avoid_: Others, Autres, Inconnu

**Media**:
Élément lisible associé à un UserFile : un Movie ou un Episode.
_Avoid_: Vidéo, fichier

**Movie**:
Media d'un Artwork de type MOVIE : l'élément lisible, pas l'œuvre. Un Artwork MOVIE a exactement un Movie.
_Avoid_: Film

**Episode**:
Media d'un Show, identifié par son numéro de saison et son numéro d'épisode. Un Show peut avoir plusieurs Episode.

**Season**:
Regroupement des Episode d'un Show par numéro de saison. Un Episode peut exister sans Season correspondante.

**Unavailable**:
Se dit d'un Media dont le UserFile est momentanément inaccessible (UserFolder déconnecté, permission révoquée) : il reste dans le Catalog mais ne peut pas être lu.
_Avoid_: Supprimé, manquant, offline

**Status**:
État de visionnage d'un Media : à voir, en cours ou vu. Le statut d'un Artwork n'est que la synthèse de celui de ses Media.
_Avoid_: État, vu/non vu

**Progress**:
Position de lecture d'un Media, mise à jour pendant la lecture et mémorisée pour la reprise.
_Avoid_: Historique, currentTime, Position

**Genre**:
Genre de l'Artwork.
_Avoid_: Type, catégorie

**Backdrop**:
Image TMDB 16:9 d'un Artwork, en paysage.
_Avoid_: Fanart, banner, bannière

**Poster**:
Image TMDB 2:3 d'un Artwork, en portrait.
_Avoid_: Affiche, cover, jaquette

**Logo**:
Image TMDB d'un Artwork représentant son titre stylisé, sur fond transparent. Elle remplace le titre texte sur la page de l'Artwork lorsqu'elle est disponible. Un Artwork a au plus un Logo.
_Avoid_: Title image, Wordmark, Titre graphique

**Artwork image**:
Image affichée en haut de la page d'un Artwork. Un tap l'ouvre dans la Gallery lorsqu'un token TMDB est configuré.
_Avoid_: Header image, banner, bannière

**Gallery**:
Page listant toutes les images TMDB d'un Artwork (Backdrop et Poster, toutes langues) dans une seule grille triée par note décroissante, avec un visualiseur plein écran zoomable. Pour un Show, uniquement les images du Show lui-même, pas celles des Season ni des Episode. Rien n'est stocké localement.
_Avoid_: Galerie d'images, Images, Photos

**Image Download**:
Copie, dans le stockage d'images de l'appareil, d'une image de la Gallery en résolution originale. Elle sort de Flux : le Catalog ne la connaît pas.
_Avoid_: Download, Save, Export, Sauvegarde

**Spoiler**:
Description ou image d'un Episode à voir, ou description d'une Season dont tous les Episode sont à voir. Les Movie, le Show lui-même et l'Unknown Artwork n'ont pas de Spoiler.
_Avoid_: Spoil

**Masquage des spoilers**:
Option facultative qui rend les Spoiler illisibles sur la page d'un Artwork, jusqu'à ce que l'utilisateur les révèle.
_Avoid_: Flou, blur, anti-spoiler

## Personnes

**Person**:
Individu, identifié par TMDB, ayant participé à un ou plusieurs Artwork.
_Avoid_: People, Credit, intervenant

**Cast**:
Ensemble des Person qui interprètent un personnage dans un Artwork.
_Avoid_: Acteurs, casting, distribution

**Crew**:
Ensemble des Person ayant un poste technique sur un Artwork (réalisation, scénario…).
_Avoid_: Équipe, staff

**Role**:
Personnage (pour le Cast) ou poste (pour le Crew) d'une Person dans un Artwork.
_Avoid_: Job, character, fonction

## Catalog

**Catalog**:
Ensemble des Artwork et Media connus de Flux pour l'utilisateur.
_Avoid_: Bibliothèque, Library, Collection

**UserFile**:
Fichier vidéo présent sur l'appareil de l'utilisateur, à l'origine d'un Media.
_Avoid_: Fichier local, vidéo

**UserFolder**:
Dossier de l'appareil que l'utilisateur a confié à Flux pour y chercher des UserFile.
_Avoid_: Source, répertoire

**Sync**:
Opération qui parcourt les UserFolder et rapproche chaque UserFile d'un Artwork TMDB pour mettre à jour le Catalog.
_Avoid_: Scan, refresh, import, matching

**Private Artwork**:
Artwork masqué du Catalog, accessible uniquement dans le Private Folder.

**Private Folder**:
Espace protégé par code PIN regroupant les Private Artwork.
_Avoid_: Coffre, dossier caché

## Lecteur

**Interface**:
Couche de contrôles du lecteur (barre du haut, boutons de lecture, barre de progression) affichée au tap et masquée automatiquement.
_Avoid_: Contrôles, UI, overlay

**Seek overlay**:
Indication éphémère du saut effectué (retour ou avance rapide) et de sa durée cumulée.
_Avoid_: Double-tap overlay

**Ambient overlay**:
Indication éphémère de la valeur de volume ou de luminosité pendant un geste vertical.
_Avoid_: Volume overlay, brightness overlay

**Chapter**:
Section nommée ou numérotée d'un Media, telle que décrite dans son UserFile. Un Chapter est soit un Opening, soit un Ending, soit un Chapter ordinaire.
_Avoid_: Segment, marqueur, partie

**Opening**:
Chapter correspondant au générique de début d'un Media.
_Avoid_: Intro, OP, générique

**Ending**:
Chapter correspondant à un générique de fin d'un Media. Un Media peut en avoir plusieurs (générique, scène post-générique, second générique).
_Avoid_: Outro, ED, Credits, générique

**Chapter button**:
Action contextuelle proposée pendant la lecture : passer un Opening ou un Ending, ou lancer le Next Episode.
_Avoid_: Skip button

**Next Episode**:
Episode qui suit, dans le même Show, celui en cours de lecture (par saison puis numéro). Un Movie, le dernier Episode d'un Show et les Episode de l'Unknown Artwork n'en ont pas.
_Avoid_: Prochain épisode, Up next

**Settings sheet**:
Feuille de réglages du lecteur, soit générale, soit dédiée au choix d'une piste audio, de sous-titres ou d'un Chapter.
_Avoid_: Bottom sheet, menu

**Track**:
Piste audio ou de sous-titres sélectionnable d'un Media.
_Avoid_: Stream, langue

## Language

**Catalogue**:
L'ensemble des Artworks, Saisons et Médias connus de l'application pour les fichiers de l'utilisateur.
_Avoid_: Bibliothèque, library

**Source**:
Un emplacement d'où proviennent les fichiers de l'utilisateur (stockage de l'appareil ou dossier personnalisé).
_Avoid_: Provider, dossier racine

**Source indisponible**:
Une Source custom qui ne peut pas être lue pour l'instant ; ses données sont conservées.
_Avoid_: Source supprimée

**Fichier manquant**:
Un fichier connu du Catalogue qui n'existe plus alors que sa Source est disponible ; ses données sont supprimées.
_Avoid_: Fichier perdu, fichier orphelin

**Artwork**:
La fiche TMDB d'un film ou d'une série à laquelle des fichiers sont rattachés.
_Avoid_: Affiche, poster, titre

**Artwork privé**:
Un Artwork masqué du Catalogue public et placé dans le dossier privé ; il le reste tant qu'il existe.
_Avoid_: Artwork caché, hidden

**Média**:
Un fichier lisible rattaché à un Artwork : un film ou un épisode.

**Synchro**:
Le processus qui aligne le Catalogue sur les fichiers des Sources et les données TMDB.
_Avoid_: Sync, rafraîchissement

**Synchro légère**:
Une Synchro qui ne traite que les nouveaux fichiers et retire les Fichiers manquants.
_Avoid_: Synchro incrémentale, onlyNew

**Synchro complète**:
Une Synchro demandée explicitement qui récupère à nouveau les données TMDB de tous les fichiers.
_Avoid_: Full sync, resynchronisation

**Progression de synchro**:
Le pourcentage d'avancement d'une Synchro, calculé sur les unités de travail déclarées par ses Étapes et Enrichissements ; elle ne recule jamais.

**Étape de synchro**:
Un traitement global de la Synchro (scan, suppression des Fichiers manquants, genres, catalogue, finalisation…) qui déclare son travail, l'exécute et signale sa progression.
_Avoid_: Fetcher, resolver, step

**Enrichissement**:
Une donnée récupérée pour un Artwork pendant l'Étape catalogue (identification, détails, saisons, traductions, credits, durées) ; ajouter une donnée TMDB revient presque toujours à ajouter un Enrichissement.
_Avoid_: Resolver, metadata fetcher

**Fichier en échec**:
Un fichier dont la récupération TMDB a échoué (réseau, API, token absent) ; rien n'est enregistré et il est retenté à la Synchro suivante.
_Avoid_: Fichier inconnu, UNKNOWN

**Fichier non reconnu**:
Un fichier que TMDB ne permet pas d'identifier ; il est enregistré comme Média sans Artwork connu.
_Avoid_: Fichier en échec
