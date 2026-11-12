# The Manual identification is found again through the file name title of known Media

A Manual identification must apply to the new files of a Light sync (a new episode `Foo S01E05` must not go back to the Artwork found by the name search). We mark the Artwork with a simple boolean field on `ArtworkEntity` and, during the identification, a file whose (title, year) parsed from its name matches that of a known Media of a manually identified Artwork is attached to that Artwork, without a name search. Priority order: Manual identification, then TMDB ID in the name (file name or folder path), then name search. A TMDB ID in the name does not prevent a Manual identification. A Full sync and the Identification reset clear the marker and identify the files again, by the TMDB ID in their name or else by name search.

## Considered Options

- **Rename the UserFile to inject `{tmdb-ID}`**: reuses the existing mechanism, but modifies the user's files and runs into SAF permissions.
- **A (title, year) → TMDB ID mapping table**: survives the disappearance of all the files, but adds a table for information that can be deduced from the known Media.
- **Remember the ID of the previous Artwork (`overriddenArtworkId`)** to allow the Identification reset: wrong as soon as a Manual identification merges two Artwork (the correctly identified files would also be sent back to the old ID) or is chained. The Identification reset therefore runs the name search again.

## Consequences

- If all the files of a manually identified Artwork disappear, the choice is forgotten.
- A file whose name gives another (title, year) than the known Media does not inherit the Manual identification.
- A file with a TMDB ID in its name whose (title, year) matches a known Media of a manually identified Artwork follows that Artwork: its TMDB ID is ignored until a Full sync or an Identification reset.
