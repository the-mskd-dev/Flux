# L'Identification manuelle est retrouvée via le titre de fichier des Média connus

Une Identification manuelle doit s'appliquer aux nouveaux fichiers d'une Synchro légère (un nouvel épisode `Foo S01E05` ne doit pas retourner vers l'Artwork trouvé par la recherche par nom). Nous marquons l'Artwork par un simple champ booléen sur `ArtworkEntity` et, pendant l'identification, un fichier dont le (titre, année) extrait du nom correspond à celui d'un Média connu d'un Artwork identifié manuellement est rattaché à cet Artwork, sans recherche par nom. Ordre de priorité : TMDB ID dans le nom du fichier, puis Identification manuelle, puis recherche par nom. Une Synchro complète et la Réinitialisation effacent le marqueur et relancent la recherche par nom.

## Considered Options

- **Renommer les UserFile pour y injecter `{tmdb-ID}`** : réutilise le mécanisme existant, mais modifie les fichiers de l'utilisateur et se heurte aux permissions SAF.
- **Table de correspondances (titre, année) → TMDB ID** : survit à la disparition de tous les fichiers, mais ajoute une table pour une information déductible des Média connus.
- **Mémoriser l'ID de l'Artwork précédent (`overriddenArtworkId`)** pour permettre la Réinitialisation : faux dès qu'une Identification manuelle fusionne deux Artwork (les fichiers correctement identifiés seraient aussi renvoyés vers l'ancien ID) ou qu'elle est enchaînée. La Réinitialisation relance donc la recherche par nom.

## Consequences

- Si tous les fichiers d'un Artwork identifié manuellement disparaissent, le choix est oublié.
- Un fichier dont le nom donne un autre (titre, année) que les Média connus n'hérite pas de l'Identification manuelle.
