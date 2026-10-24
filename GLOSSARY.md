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
Action contextuelle proposée pendant la lecture : passer un Opening ou un Ending, ou lancer l'épisode suivant.
_Avoid_: Skip button

**Settings sheet**:
Feuille de réglages du lecteur, soit générale, soit dédiée au choix d'une piste audio ou de sous-titres.
_Avoid_: Bottom sheet, menu

**Track**:
Piste audio ou de sous-titres sélectionnable d'un Media.
_Avoid_: Stream, langue
