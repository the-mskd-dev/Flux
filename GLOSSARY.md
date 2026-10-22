# Flux

Flux organise les fichiers vidéo locaux de l'utilisateur en bibliothèque (enrichie via TMDB) et les lit avec un historique de progression.

## Modèles

**Artwork**:
Représente une œuvre sans les Media associés.
_Avoid_: Film, Série, Saison, Episode, Genre

**Media**:

**Movie**:
Media de type film. Associé à un fichier local, c'est ce que l'on va utiliser pour lire via le lecteur. Il ne doit y avoir qu'un seul Movie pour un Artwork de type MOVIE.

**Episode**:
Media de type série. Associé à un fichier local, c'est ce que l'on va utiliser pour lire via le lecteur.Il peut y avoir plusieurs Episode pour un Artwork de type SHOW.

**Season**:
Représente une saison pour les oeuvres de type SHOW. Il peut y avoir plusieurs Season pour un Artwork de type SHOW. Une Season peut contenir plusieurs Episode.

**Genre**:
Genre de l'Artwork.
_Avoid_: Type, catégorie

**FullArtwork**:
Regroupe un Artwork avec son ou ses Media, ainsi que ses Genre

## Lecteur

**Media**:
Élément lisible associé à un fichier local : un film ou un épisode.
_Avoid_: Vidéo, fichier

**Interface**:
Couche de contrôles du lecteur (barre du haut, boutons de lecture, barre de progression) affichée au tap et masquée automatiquement.
_Avoid_: Contrôles, UI, overlay

**Seek overlay**:
Indication éphémère du saut effectué (retour ou avance rapide) et de sa durée cumulée.
_Avoid_: Double-tap overlay

**Ambient overlay**:
Indication éphémère de la valeur de volume ou de luminosité pendant un geste vertical.
_Avoid_: Volume overlay, brightness overlay

**Chapter button**:
Action contextuelle proposée pendant la lecture : passer un chapitre (générique) ou lancer l'épisode suivant.
_Avoid_: Skip button

**Settings sheet**:
Feuille de réglages du lecteur, soit générale, soit dédiée au choix d'une piste audio ou de sous-titres.
_Avoid_: Bottom sheet, menu

**Track**:
Piste audio ou de sous-titres sélectionnable d'un Media.
_Avoid_: Stream, langue
