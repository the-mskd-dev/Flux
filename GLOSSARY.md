# Flux

Flux organise les fichiers vidéo locaux de l'utilisateur en bibliothèque (enrichie via TMDB) et les lit avec un historique de progression.

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
